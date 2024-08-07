package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.util.containers.ContainerUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import site.forgus.plugins.apigeneratorplus.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtTypeReference;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import java.util.ArrayList;
import java.util.Collections;
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

    public static boolean isGetMethod(List<KtAnnotationEntry> annotations) {
        for (KtAnnotationEntry annotation : annotations) {
            if (annotation.getText().contains("GetMapping") || annotation.getText().contains("GET")) {
                return true;
            }
        }
        return false;
    }

    public static MediaType getRequestMediaType(MethodInfo methodInfo) {
        return getRequestMediaType(methodInfo.getPsiMethod());
    }

    public static MediaType getRequestMediaType(PsiMethod psiMethod) {
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

    public static MediaType getRequestMediaType(KtFunction ktFunction) {
        if (isGetMethod(ktFunction.getAnnotationEntries())) {
            return null;
        }
        List<KtParameter> parameters = ktFunction.getValueParameterList().getParameters();
        for (KtParameter parameter : parameters) {
            KtTypeReference typeReference = parameter.getTypeReference();
            String typeName = KtUtil.getText(typeReference);
            if (FieldUtil.isFileType(typeName)) {
                return MediaType.MULTIPART_FORM_DATA;
            }
            if (parameter.getText().contains(WebAnnotation.RequestBody)) {
                return MediaType.APPLICATION_JSON;
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
            int index = getIndexOnCanonicalClassNameList(item.getCanonicalText(), canonicalClassNameList);
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
                        child.setChildren(Collections.emptyList());
                    }
                }
            }
            item.setChildren(children);
        }

        return list;
    }

    public static String getFormDataVal(List<FieldInfo> list) {
        StringBuilder stringBuilder = new StringBuilder("var formData = new FormData();\n");
        List<Object[]> keyValues = generateParamKeyValue(list);
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
            return Collections.emptyList();
        }
        ArrayList<Object[]> strings = new ArrayList<>();
        for (FieldInfo requestField : fieldInfoList) {
            if (requestField.hasChildren()) {
                strings.addAll(generateKeyValue(requestField.getChildren()));
            } else {
                Object value = FieldUtil.getValue(requestField);
                strings.add(new Object[]{requestField.getName(), value});
            }
        }
        return strings;
    }

    private static List<Object[]> generateParamKeyValue(List<FieldInfo> fieldInfoList) {
        ArrayList<FieldInfo> tempList = new ArrayList<>(fieldInfoList);
        CollectionUtils.filter(tempList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                FieldInfo fieldInfo = (FieldInfo) o;
                return !fieldInfo.containPathVariableAnnotation() && !fieldInfo.containRequestBodyAnnotation();
            }
        });
        return generateKeyValue(tempList);
    }

    public static RequestMethodEnum getRequestMethod(String funStr) {
        if (funStr.contains(WebAnnotation.RequestMapping)) {
            if (funStr.contains(RequestMethodEnum.GET.name())) {
                return RequestMethodEnum.GET;
            }
            if (funStr.contains(RequestMethodEnum.POST.name())) {
                return RequestMethodEnum.POST;
            }
            if (funStr.contains(RequestMethodEnum.PUT.name())) {
                return RequestMethodEnum.PUT;
            }
            if (funStr.contains(RequestMethodEnum.DELETE.name())) {
                return RequestMethodEnum.DELETE;
            }
            if (funStr.contains(RequestMethodEnum.PATCH.name())) {
                return RequestMethodEnum.PATCH;
            }
        }
        if (funStr.contains(WebAnnotation.GetMapping)) {
            return RequestMethodEnum.GET;
        }
        if (funStr.contains(WebAnnotation.PutMapping)) {
            return RequestMethodEnum.PUT;
        }
        if (funStr.contains(WebAnnotation.DeleteMapping)) {
            return RequestMethodEnum.DELETE;
        }
        if (funStr.contains(WebAnnotation.PatchMapping)) {
            return RequestMethodEnum.PATCH;
        }
        return RequestMethodEnum.POST;
    }

    public static String replacePathVariable(MethodInfo methodInfo) {
        String methodPath = methodInfo.getMethodPath();
        for (FieldInfo requestField : methodInfo.getRequestFields()) {
            if (requestField.containPathVariableAnnotation()) {
                methodPath = methodPath.replace("{" + requestField.getName() + "}",
                        FieldUtil.getValue(requestField).toString());
            }
        }
        return methodPath;
    }

    public static String getTagContent(String content) {
        return content.replaceAll("\n *\\*", "\n");
    }

}
