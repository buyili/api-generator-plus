package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiParameter;
import org.jetbrains.kotlin.psi.KtParameter;

/**
 * @author lmx 2020/11/12 17:05
 */

public class StringUtil {

    private static int nameNumber = 0;

    public static String getName() {
        nameNumber = nameNumber + 1;
        return "Unnameed (" + nameNumber + ")";
    }

    public static void showPsiNameValuePair(PsiNameValuePair psiNameValuePair){
        System.out.println(psiNameValuePair.getName());
        System.out.println(psiNameValuePair.getValue());
        System.out.println(psiNameValuePair.getValue().getText());
    }

    public static void showPsiMethod(PsiMethod psiMethod){
//        Gson gson = new Gson();
        System.out.println(psiMethod.getName());
        System.out.println("PsiAnnotation annotation----------------");
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            //结果示例 @RequestMapping(value = "/test", method = RequestMethod.GET)
            System.out.println(annotation.getText());
        }
        System.out.println("PsiAnnotation annotation----------------end");
        System.out.println("PsiParameter parameter----------------");
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            //结果示例： QueryDto queryDto
            System.out.println(parameter.getText());
            for (PsiAnnotation annotation : parameter.getAnnotations()) {
                System.out.println("annotation text:                " + annotation.getText());
            }
        }
        System.out.println("PsiParameter annotation----------------end");
    }

    public static void showKtParameter(KtParameter ktParameter){
        System.out.println("KtParameter");

        System.out.println("ktParameter.getText()");
        System.out.println(ktParameter.getText());
        System.out.println();

        System.out.println("ktParameter.getTypeReference().getText()");
        System.out.println(ktParameter.getTypeReference().getText());
        System.out.println();

        System.out.println("ktParameter.getText()");
        System.out.println(ktParameter.getText());
        System.out.println();

    }



}
