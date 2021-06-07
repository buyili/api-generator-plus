package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.YApiServerUrlListTableDialog;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

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
        YApiServerUrlEntity yApiServerUrlEntity = new YApiServerUrlEntity();
        yApiServerUrlEntity.setId("1");
        yApiServerUrlEntity.setServerUrl("gawefawefawertawetaewfawef");
        List<YApiServerUrlEntity> items = Arrays.asList(yApiServerUrlEntity);
        YApiServerUrlListTableDialog.showDialog(items);
    }
}
