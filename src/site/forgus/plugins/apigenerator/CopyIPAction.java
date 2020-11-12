package site.forgus.plugins.apigenerator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.curl.CurlUtils;
import site.forgus.plugins.apigenerator.util.NotificationUtil;

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
