package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyAsCurlCMDAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
//        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
//        Project project = actionEvent.getProject();
//        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
//        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
//        if (selectedClass == null) {
//            NotificationUtil.errorNotify("this operate only support in class file", project);
//            return;
//        }
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsCUrl(actionEvent, CUrlClientType.CMD);
        } catch (BizException e) {
            e.printStackTrace();
            if (StringUtils.isNotBlank(e.getMessage())) {
                NotificationUtil.errorNotify(e.getMessage());
            }
        }
    }

}