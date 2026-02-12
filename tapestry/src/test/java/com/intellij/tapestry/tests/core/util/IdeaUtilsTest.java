package com.intellij.tapestry.tests.core.util;

import com.intellij.openapi.actionSystem.*;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class IdeaUtilsTest extends BaseTestCase {

    @BeforeClass
    public void defaultConstructor() {
        new IdeaUtils();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isModuleNode(IdeaProjectTestFixture fixture) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(CommonDataKeys.PROJECT.getName(), fixture.getProject());
        dataMap.put(LangDataKeys.MODULE_CONTEXT.getName(), fixture.getModule());
        DataContext dataContext = dataId -> dataMap.get(dataId);

        AnActionEvent actionEvent = AnActionEvent.createFromAnAction(new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {}
        }, null, "", dataContext);
        assert IdeaUtils.isModuleNode(actionEvent);

        dataMap.put(CommonDataKeys.PROJECT.getName(), null);
        dataMap.put(LangDataKeys.MODULE_CONTEXT.getName(), null);

        actionEvent = AnActionEvent.createFromAnAction(new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {}
        }, null, "", dataContext);
        assert !IdeaUtils.isModuleNode(actionEvent);
    }

}