package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import site.forgus.plugins.apigeneratorplus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

/**
 * reference:
 * 使用 Fetch: https://developer.mozilla.org/zh-CN/docs/Web/API/Fetch_API/Using_Fetch
 * WorkerOrGlobalScope.fetch(): https://developer.mozilla.org/zh-CN/docs/Web/API/WindowOrWorkerGlobalScope/fetch
 */
public class CopyAsFetchAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
            Project project = actionEvent.getProject();
        try {
//            Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            GlobalVariable.setProject(project);
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsFetch(actionEvent);
        } catch (BizException e) {
            e.printStackTrace();
            if (StringUtils.isNotBlank(e.getMessage())) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
        }
    }

}
