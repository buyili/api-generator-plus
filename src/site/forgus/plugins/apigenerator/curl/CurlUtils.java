package site.forgus.plugins.apigenerator.curl;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import site.forgus.plugins.apigenerator.constant.CUrlClientType;
import site.forgus.plugins.apigenerator.constant.WebAnnotation;
import site.forgus.plugins.apigenerator.normal.MethodInfo;

/**
 * @author lmx 2020/11/11 15:49
 */

public class CurlUtils {

    public static void copyAsCUrl(PsiElement referenceAt, CUrlClientType cUrlClientType) {
        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
//            try {
            MethodInfo methodInfo = new MethodInfo(selectedMethod);
            StringBuffer stringBuffer = new StringBuffer("curl ");
            stringBuffer.append(methodInfo.getCurlRequestBody(cUrlClientType));
            stringBuffer.append(" 'http://localhost/'");
            stringBuffer.append(" -H \"X-Auth-Token: 7d2661b6-4258-4e80-9ea2-761f9d5cd3c2\"");
            System.out.println(stringBuffer.toString());
//            } catch (IOException e) {
//                NotificationUtil.errorNotify(e.getMessage(), project);
//            }
        }
    }

    private boolean haveControllerAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }
}
