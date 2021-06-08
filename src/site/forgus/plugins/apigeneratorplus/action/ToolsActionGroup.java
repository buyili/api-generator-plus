package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.icons.SdkIcons;

/**
 * @author lmx 2021/6/6 12:26
 **/

public class ToolsActionGroup extends ActionGroup {

    @Override
    public void update(@NotNull AnActionEvent event) {
        // 设置该Action图标
        event.getPresentation().setIcon(SdkIcons.Logo);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        return new AnAction[]{
                new YApiServerUrlDialogAction("Server Urls", "", null),
                new YApiProjectDialogAction("YApi Token", "", null)
        };
    }
}
