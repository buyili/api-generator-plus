package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;

/**
 * @author lmx 2020/11/11 14:19
 */

public class GenerateModuleNamesAction extends AnAction {
    @SneakyThrows
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
            Project project = actionEvent.getProject();
            GlobalVariable.getInstance().setProject(project);
            CurlUtils.findModuleInfoAndSave(actionEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
