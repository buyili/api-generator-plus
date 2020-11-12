package site.forgus.plugins.apigeneratorplus;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyIPAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        String baseApi = CurlUtils.getRealIP();
        NotificationUtil.infoNotify("已复制到剪切板", baseApi, project);
        CopyPasteManager.getInstance().setContents(new TextTransferable(baseApi));
    }

}
