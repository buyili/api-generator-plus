package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.*;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;

/**
 * @author lmx 2020/12/29 17:18
 */

public class ClassUtil {

    public static boolean containRestControllerAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RestController)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containRestControllerAnnotation(PsiClass psiClass) {
        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList != null) {
            return containRestControllerAnnotation(modifierList.getAnnotations());
        }
        return false;
    }

    public static PsiAnnotation[] getAnnotations(PsiClass psiClass) {
        if (psiClass != null) {
            PsiModifierList modifierList = psiClass.getModifierList();
            if (modifierList != null) {
                return modifierList.getAnnotations();
            }
        }
        return new PsiAnnotation[0];
    }

    public static PsiAnnotation[] getAnnotations(PsiParameter psiParameter) {
        PsiModifierList modifierList = psiParameter.getModifierList();
        if (modifierList != null) {
            return modifierList.getAnnotations();
        }
        return new PsiAnnotation[0];
    }

    public static PsiAnnotation[] getAnnotations(PsiField psiField) {
        PsiModifierList modifierList = psiField.getModifierList();
        if (modifierList != null) {
            return modifierList.getAnnotations();
        }
        return new PsiAnnotation[0];
    }

    public static String getPackageName(PsiClass aClass) {
        PsiClass topClass = getTopLevelClass(aClass);
        if (topClass != null) {
            String fqName = topClass.getQualifiedName();
            if (fqName != null) {
                return StringUtil.getPackageName(fqName);
            }
        }

        PsiFile file = aClass.getContainingFile();
        if (file instanceof PsiClassOwner) {
            return ((PsiClassOwner) file).getPackageName();
        }

        return null;
//        String fqName = psiClass.getQualifiedName();
//        if(fqName != null){
//            int lastPointIdx = fqName.lastIndexOf(".");
//            if (lastPointIdx >= 0) {
//                return fqName.substring(0, lastPointIdx);
//            }
//            return "";
//        }
    }

    @Nullable
    public static PsiClass getTopLevelClass(@NotNull PsiElement element) {
        PsiClass topClass = JBIterable.generate(element, PsiElement::getParent).takeWhile(e -> !(e instanceof PsiFile)).filter(PsiClass.class).last();
        return topClass instanceof PsiTypeParameter ? null : topClass;
    }

}
