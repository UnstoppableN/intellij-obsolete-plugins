package com.intellij.tapestry.tests;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Alexey Chmutov
 */
public final class Util {
  static final String DOT_TML = "." + TapestryConstants.TEMPLATE_FILE_EXTENSION;
  static final String DOT_JAVA = ".java";
  static final String DOT_GROOVY = ".groovy";
  static final String AFTER = "_after";
  public static final String DOT_EXPECTED = ".expected";

  private Util() {
  }

  static String getFileText(final String filePath) {
    try {
      return FileUtil.loadFile(new File(filePath));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  static String getCommonTestDataFileText(@NotNull String fileName) {
    return getFileText(getCommonTestDataPath() + "/" + fileName);
  }

  public static IdeaProjectTestFixture getWebModuleFixture(String name) throws Exception {
    IdeaProjectTestFixture fixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(name).getFixture();
    fixture.setUp();
    return fixture;
  }

  static String getCommonTestDataPath() {
    // First try the project-relative path (standalone plugin)
    String projectPath = System.getProperty("user.dir");
    File testDataDir = new File(projectPath, "src/test/testData/");
    if (testDataDir.exists()) {
      return testDataDir.getAbsolutePath().replace(File.separatorChar, '/') + "/";
    }
    // Fallback: try relative to PathManager home (IntelliJ source tree)
    String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    testDataDir = new File(homePath, "contrib/tapestry/tests/testData/");
    if (testDataDir.exists()) {
      return testDataDir.getAbsolutePath().replace(File.separatorChar, '/') + "/";
    }
    // Last resort: try community path
    return homePath + "/contrib/tapestry/tests/testData/";
  }
}
