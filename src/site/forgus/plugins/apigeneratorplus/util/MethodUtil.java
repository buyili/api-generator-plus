package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.util.containers.ContainerUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import javax.print.attribute.standard.Media;
import java.util.ArrayList;
import java.util.List;

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

    public static int getIndexOnCanonicalClassNameList(String canonicalClassName, List<String> set) {
        for (String s : set) {
            if (canonicalClassName.startsWith(s)) {
                return set.indexOf(s);
            }
        }
        return -1;
    }

    public static List<FieldInfo> filterChildrenFiled(List<FieldInfo> list, FilterFieldInfo filterFieldInfo) {
        List<String> canonicalClassNameList = filterFieldInfo.getCanonicalClassNameList();
        List<String> includeFiledList = filterFieldInfo.getIncludeFiledList();
        List<String> excludeFiledList = filterFieldInfo.getExcludeFiledList();

        for (FieldInfo item : list) {
            List<FieldInfo> children = item.getChildren();
            int index = getIndexOnCanonicalClassNameList(item.getPsiType().getCanonicalText(), canonicalClassNameList);
            if (CollectionUtils.isNotEmpty(canonicalClassNameList) && index != -1) {

                if (includeFiledList.size() > index && StringUtils.isNotEmpty(includeFiledList.get(index))) {
                    String includeFieldStr = includeFiledList.get(index).concat(",");
                    children.removeIf(child -> !includeFieldStr.contains(child.getName() + ","));
                } else if (excludeFiledList.size() > index && StringUtils.isNotEmpty(excludeFiledList.get(index))) {
                    String excludeFieldStr = excludeFiledList.get(index).concat(",");
                    children.removeIf(child -> excludeFieldStr.contains(child.getName() + ","));
                }
                if (filterFieldInfo.excludeChildren) {
                    for (FieldInfo child : children) {
                        child.setChildren(ContainerUtil.newArrayList());
                    }
                }
            }
            item.setChildren(children);
        }

        return list;
    }

    public static String getFormDataVal(List<FieldInfo> list) {
        StringBuilder stringBuilder = new StringBuilder("var formData = new FormData();\n");
        List<Object[]> keyValues = generateKeyValue(list);
        for (Object[] keyValue : keyValues) {
            stringBuilder.append("formData.append(\"")
                    .append(keyValue[0])
                    .append("\", \"")
                    .append(keyValue[1] == null ? "" : keyValue[1].toString())
                    .append("\");\n");
        }
        return stringBuilder.toString();
    }

    private static List<Object[]> generateKeyValue(List<FieldInfo> fieldInfoList) {
        if (CollectionUtils.isEmpty(fieldInfoList)) {
            return ContainerUtil.newArrayList();
        }
        ArrayList<Object[]> strings = new ArrayList<>();
        for (FieldInfo requestField : fieldInfoList) {
            if (requestField.hasChildren()) {
                strings.addAll(generateKeyValue(requestField.getChildren()));
            } else {
                Object value = FieldUtil.getValue(requestField.getPsiType());
                strings.add(new Object[]{requestField.getName(), value});
            }
        }
        return strings;
    }

}
