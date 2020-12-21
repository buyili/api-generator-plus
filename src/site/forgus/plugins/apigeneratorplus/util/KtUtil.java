package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.kotlin.psi.KtTypeElement;
import org.jetbrains.kotlin.psi.KtTypeReference;

import java.util.List;

/**
 * @author lmx 2020/12/21 15:04
 */

public class KtUtil {

    public static KtTypeReference extractIterableTypeParameter(KtTypeReference ktTypeReference) {
        KtTypeElement typeElement = ktTypeReference.getTypeElement();
        KtTypeReference iterableType = null;
        if (typeElement != null) {
            List<KtTypeReference> typeArgumentsAsTypes = typeElement.getTypeArgumentsAsTypes();
            for (KtTypeReference typeArgumentsAsType : typeArgumentsAsTypes) {
                iterableType = typeArgumentsAsType;
            }
        }
        return iterableType;
    }

    public static String getText(KtTypeReference ktTypeReference) {
        String text = ktTypeReference.getText();
        return text.endsWith("?") ? text.substring(0, text.length() - 1) : text;
    }

}
