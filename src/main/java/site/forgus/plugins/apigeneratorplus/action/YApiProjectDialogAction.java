package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState;
import site.forgus.plugins.apigeneratorplus.token.YApiProjectDialog;

import javax.swing.*;

/**
 * @author lmx 2021/6/6 12:27
 **/

public class YApiProjectDialogAction extends AnAction {
    public YApiProjectDialogAction() {
    }

    public YApiProjectDialogAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ApiGeneratorPlusAppState appState = ServiceManager.getService(ApiGeneratorPlusAppState.class);
        Project project = anActionEvent.getProject();
        new YApiProjectDialog(project,appState.projects, appState.urls).show();
    }
}
