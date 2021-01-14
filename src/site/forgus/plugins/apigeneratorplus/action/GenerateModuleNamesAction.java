package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;

/**
 * @author lmx 2020/11/11 14:19
 */

public class GenerateModuleNamesAction extends AnAction {
    @SneakyThrows
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
            CurlUtils.findModuleInfoAndSave(actionEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
