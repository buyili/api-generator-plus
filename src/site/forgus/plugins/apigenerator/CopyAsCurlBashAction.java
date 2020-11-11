package site.forgus.plugins.apigenerator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.constant.WebAnnotation;
import site.forgus.plugins.apigenerator.curl.CurlUtils;
import site.forgus.plugins.apigenerator.normal.MethodInfo;
import site.forgus.plugins.apigenerator.util.NotificationUtil;

/**
 * @author lmx 2020/11/11 14:19
 */

public class CopyAsCurlBashAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Project project = actionEvent.getProject();
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        if (selectedClass == null) {
            NotificationUtil.errorNotify("this operate only support in class file", project);
            return;
        }
        CurlUtils.copyAsCUrl(referenceAt, CUrlClientType.BASH);
    }

}
