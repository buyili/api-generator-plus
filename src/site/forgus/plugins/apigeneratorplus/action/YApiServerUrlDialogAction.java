package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.serverurl.YApiServerUrlsDialog;
import site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState;

import javax.swing.*;

/**
 * @author lmx 2021/6/6 12:27
 **/

public class YApiServerUrlDialogAction extends AnAction {
    public YApiServerUrlDialogAction() {
    }

    public YApiServerUrlDialogAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ApiGeneratorPlusAppState appState = ServiceManager.getService(ApiGeneratorPlusAppState.class);
        Project project = anActionEvent.getProject();
        new YApiServerUrlsDialog(project, appState.urls).show();
    }
}
