package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import javax.print.attribute.standard.Media;

/**
 * @author lmx 2020/12/11 11:43
 */
@Data
public class MethodUtil {

    public static void getRequestContentType(PsiMethod psiMethod) {
        // @RequestMapping包含consumes，并且consumes值唯一
        PsiAnnotation mappingAnnotation = getRequestMapping(psiMethod);
        extraContentTypeFromAttribute(mappingAnnotation);


        // @RequestMapping不包含consumes
    }

    @Nullable
    public static PsiAnnotation getRequestMapping(PsiMethod psiMethod) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            String text = annotation.getText();
            if (text.contains("Mapping")) {
                return annotation;
            }
        }
        return null;
    }

    public static void extraContentType(PsiMethod psiMethod) {
        PsiAnnotation mappingAnnotation = getRequestMapping(psiMethod);
        extraContentTypeFromAttribute(mappingAnnotation);
    }

    /**
     * @param annotation
     * @todo
     */
    public static void extraContentTypeFromAttribute(PsiAnnotation annotation) {
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
            if ("consumes".equals(psiNameValuePair.getName())) {
                PsiReference reference = psiNameValuePair.getValue().getReference();
                if (reference != null) {
//                    return RequestMethodEnum.valueOf(reference.resolve().getText());
                    return;
                }
                PsiElement[] children = psiNameValuePair.getValue().getChildren();
                for (PsiElement child : children) {
                    if (child instanceof PsiReferenceExpressionImpl) {
                        PsiElement resolve = ((PsiReferenceExpressionImpl) child).resolve();
                        PsiElement[] children1 = resolve.getChildren();
//                        return RequestMethodEnum.valueOf(((PsiReference) child).resolve().getText());
                        return;
                    }
                }
            }
        }
    }


    public static RequestMethodEnum extractMethodFromAttribute(PsiAnnotation annotation) {
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
            if ("method".equals(psiNameValuePair.getName())) {
                PsiReference reference = psiNameValuePair.getValue().getReference();
                if (reference != null) {
                    return RequestMethodEnum.valueOf(reference.resolve().getText());
                }
                PsiElement[] children = psiNameValuePair.getValue().getChildren();
                for (PsiElement child : children) {
                    if (child instanceof PsiReference) {
                        return RequestMethodEnum.valueOf(((PsiReference) child).resolve().getText());
                    }
                }
            }
        }
        return RequestMethodEnum.POST;
    }

    public static boolean isGetMethod(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains("GetMapping") || annotation.getText().contains("GET")) {
                return true;
            }
        }
        return false;
    }

    public static MediaType getMediaType(MethodInfo methodInfo) {
        return getMediaType(methodInfo.getPsiMethod());
    }

    public static MediaType getMediaType(PsiMethod psiMethod) {
        if (isGetMethod(psiMethod.getAnnotations())) {
            return null;
        }
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            String typeName = parameter.getType().getPresentableText();
            if (FieldUtil.isFileType(typeName)) {
                return MediaType.MULTIPART_FORM_DATA;
            }
            for (PsiAnnotation annotation : parameter.getAnnotations()) {
                if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                    return MediaType.APPLICATION_JSON;
                }
            }
        }
        return MediaType.APPLICATION_FORM_URLENCODED;
    }

}
