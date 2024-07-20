package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

/**
 * axios中文文档|axios中文网
 * http://www.axios-js.com/zh-cn/docs/
 */
public class CopyAsAxiosAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        try {
//            Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            GlobalVariable.setProject(project);
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsAxios(actionEvent);
        } catch (BizException e) {
            e.printStackTrace();
            if (StringUtils.isNotBlank(e.getMessage())) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
        }
    }

}
