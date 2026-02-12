package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.lang.TmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows navigation from a class to it's corresponding template and vice-versa.
 */
public class ClassTemplateNavigation extends AnAction {

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(@NotNull AnActionEvent event) {
    Presentation presentation = event.getPresentation();

    Module module;
    try {
      module = event.getData(PlatformCoreDataKeys.MODULE);
    }
    catch (Throwable ex) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    if (!TapestryUtils.isTapestryModule(module)) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    PsiFile psiFile = getEventPsiFile(event);

    if (psiFile == null) {
      presentation.setEnabled(false);
      return;
    }

    // "Tapestry Class" action should only be enabled when in a TML file
    if ("Tapestry Class".equals(event.getPresentation().getText())) {
      if (!psiFile.getFileType().equals(TmlFileType.INSTANCE)) {
        presentation.setEnabled(false);
        return;
      }
    }

    presentation.setEnabledAndVisible(true);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getData(CommonDataKeys.PROJECT);

    PsiFile psiFile = getEventPsiFile(event);
    Module module = event.getData(PlatformCoreDataKeys.MODULE);
    
    // Debug logging
    if (psiFile == null) {
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .warn("Navigation failed: psiFile is null");
      return;
    }
    if (module == null) {
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .warn("Navigation failed: module is null for file: " + psiFile.getName());
      return;
    }
    
    String presentationText = event.getPresentation().getText();
    com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
      .info("Navigation attempt - File: " + psiFile.getName() + 
            ", FileType: " + psiFile.getFileType() + 
            ", Action: " + presentationText + 
            ", Module: " + module.getName());
    
    VirtualFile navigationTarget = findNavigationTarget(psiFile, module, presentationText);
    
    if (navigationTarget != null) {
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .info("Navigation target found: " + navigationTarget.getPath());
      FileEditorManager.getInstance(project).openFile(navigationTarget, true);
    }
    else {
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .warn("Navigation target not found for: " + psiFile.getName());
      showCantNavigateMessage();
    }
  }

  @Nullable
  public static VirtualFile findNavigationTarget(@NotNull PsiFile psiFile, @NotNull Module module, String presentationText) {
    final TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(module);
    
    if (project == null) {
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .warn("TapestryProject is null for module: " + module.getName());
      return null;
    }
    
    if (psiFile instanceof PsiClassOwner && presentationText.equals("Class <-> Template Navigation")) {
      PsiClass psiClass = IdeaUtils.findPublicClass(psiFile);
      if (psiClass == null) return null;

      PresentationLibraryElement tapestryElement =
        PresentationLibraryElement.createProjectElementInstance(new IntellijJavaClassType(module, psiClass.getContainingFile()), project);
      if (tapestryElement == null || !tapestryElement.allowsTemplate()) return null;
      IResource[] templates = tapestryElement.getTemplateConsiderSuperClass();
      return templates.length != 0 && templates[0] != null ? ((IntellijResource)templates[0]).getPsiFile().getVirtualFile() : null;
    }

    if (psiFile.getFileType().equals(TmlFileType.INSTANCE) &&
        (presentationText.equals("Class <-> Template Navigation") || presentationText.equals("Tapestry Class"))) {
      
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .info("Looking for template element for: " + psiFile.getName() + " in project");
      
      final PresentationLibraryElement template = project.findElementByTemplate(psiFile);
      
      if (template == null) {
        com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
          .warn("findElementByTemplate returned null for: " + psiFile.getName() + 
                ", path: " + psiFile.getVirtualFile().getPath());
        return null;
      }
      
      com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
        .info("Template element found: " + template);
      
      IJavaClassType elementClass = template.getElementClass();
      
      if (elementClass != null) {
        com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
          .info("Element class found: " + elementClass);
        return ((IntellijJavaClassType)elementClass).getPsiClass().getContainingFile().getVirtualFile();
      } else {
        com.intellij.openapi.diagnostic.Logger.getInstance(ClassTemplateNavigation.class)
          .warn("Element class is null for template: " + template);
      }
    }
    return null;
  }

  /**
   * Finds the PsiFile on which the event occured.
   *
   * @param event the event.
   * @return the PsiFile on which the event occured, or {@code null} if the file couldn't be determined.
   */
  @Nullable
  public static PsiFile getEventPsiFile(AnActionEvent event) {
    Project project = event.getData(CommonDataKeys.PROJECT);
    if (project == null) return null;

    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    if (fileEditorManager == null) return null;

    Editor editor = fileEditorManager.getSelectedTextEditor();
    if (editor == null) return null;

    return PsiManager.getInstance(project).findFile(FileDocumentManager.getInstance().getFile(editor.getDocument()));
  }

  public static void showCantNavigateMessage() {
    Messages.showInfoMessage("Couldn't find a file to navigate to.", "Not Tapestry file");
  }
}