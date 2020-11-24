package site.forgus.plugins.apigeneratorplus;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyAsCurlBashAction extends AnAction {
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
            curlUtils.copyAsCUrl(actionEvent, CUrlClientType.BASH);
        } catch (
                BizException e) {
            e.printStackTrace();
        }
    }

}
