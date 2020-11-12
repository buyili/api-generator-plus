package site.forgus.plugins.apigenerator;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.curl.CurlUtils;
import site.forgus.plugins.apigenerator.setting.CURLSettingState;
import site.forgus.plugins.apigenerator.util.NotificationUtil;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * @author lmx 2020/11/11 14:19
 */

public class GenerateModuleNamesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
//        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
//        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        Project project = actionEvent.getProject();
//        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        CURLSettingState state = ServiceManager.getService(project, CURLSettingState.class);
        Module[] modules = ModuleManager.getInstance(project).getModules();
        ArrayList<String> tempModuleNames = new ArrayList<>();
        for (Module module : modules) {
            tempModuleNames.add(module.getName());
        }
        state.moduleNames = tempModuleNames;
        Gson gson = new Gson();
        String message = MessageFormat.format("Generate project modules success! modules: [{0}]", gson.toJson(tempModuleNames));
        NotificationUtil.infoNotify(message, project);

//        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
//        if (selectedClass == null) {
//            NotificationUtil.errorNotify("this operate only support in class file", project);
//            return;
//        }
//        CurlUtils curlUtils = new CurlUtils();
//        curlUtils.copyAsCUrl(actionEvent, CUrlClientType.BASH);
    }

}
