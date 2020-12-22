package site.forgus.plugins.apigeneratorplus.normal;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.*;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.util.DesUtil;
import site.forgus.plugins.apigeneratorplus.util.MethodUtil;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;

import java.io.Serializable;
import java.util.*;

@Data
public class MethodInfo implements Serializable {
    private static final long serialVersionUID = -9143203778013000538L;

    private static final String SLASH = "/";

    private String desc;
    private String packageName;
    private String className;
    private String returnStr;
    private String paramStr;
    private String methodName;
    private List<FieldInfo> requestFields;
    private List<FieldInfo> responseFields;
    private FieldInfo response;
    private PsiMethod psiMethod;
    private MediaType mediaType;

    private Language language;
    private KtFunction ktFunction;
    private RequestMethodEnum requestMethod;
    private String methodPath;
    private String classPath;
    private String funStr;
    private List<String> classAnnotationTexts;

    private List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");

    public MethodInfo(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
        this.language = psiMethod.getLanguage();
        this.mediaType = MethodUtil.getMediaType(psiMethod);
        this.setFunStr(psiMethod.getText());
        this.setRequestMethod(MethodUtil.getRequestMethod(psiMethod.getText()));
        this.setMethodPath(extraMethodPath(psiMethod));
        this.setClassPath(extraClassPath(psiMethod));
        this.setDesc(DesUtil.getDescription(psiMethod));
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return;
        }
        this.setPackageName(PsiUtil.getPackageName(psiClass));
        this.setClassName(psiClass.getName());

        List<String> classAnnotationTexts = new ArrayList<>();
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            classAnnotationTexts.add(annotation.getText());
        }
        this.setClassAnnotationTexts(classAnnotationTexts);

        this.setParamStr(psiMethod.getParameterList().getText());
        this.setMethodName(psiMethod.getName());
        this.setRequestFields(listParamFieldInfos(psiMethod));
        PsiType returnType = psiMethod.getReturnType();
        if (returnType != null) {
            this.setReturnStr(returnType.getPresentableText());
            if (!"void".equals(psiMethod.getReturnType().getPresentableText())) {
                FieldInfo fieldInfo = new FieldInfo(psiMethod.getProject(), psiMethod.getReturnType());
                this.response = fieldInfo;
                this.setResponseFields(fieldInfo.getChildren());
            }
        }

    }

    public MethodInfo(KtFunction ktFunction) {
        this.ktFunction = ktFunction;
        this.language = ktFunction.getLanguage();
        this.mediaType = MethodUtil.getMediaType(ktFunction);
        this.setFunStr(ktFunction.getText());
        this.setParamStr(ktFunction.getValueParameterList().getText());
        this.setMethodName(ktFunction.getName());
        this.setRequestFields(listParamFieldInfos(ktFunction));
        this.setRequestMethod(MethodUtil.getRequestMethod(ktFunction.getText()));
        this.setMethodPath(extraMethodPathKt(ktFunction));
        this.setClassPath(extraClassPathKt(ktFunction));
        this.setDesc(DesUtil.getDescription(ktFunction));

        KtClass ktClass = (KtClass) ktFunction.getParent().getParent();
        this.setPackageName(ktClass.getFqName().toString());
        this.setClassName(ktClass.getName());

        List<String> classAnnotationTexts = new ArrayList<>();
        for (KtAnnotationEntry annotationEntry : ktClass.getAnnotationEntries()) {
            classAnnotationTexts.add(annotationEntry.getText());
        }
        this.setClassAnnotationTexts(classAnnotationTexts);

        KtTypeReference returnTypeReference = ktFunction.getTypeReference();
        if (returnTypeReference != null) {
            this.setReturnStr(returnTypeReference.getText());
            if (!"void".equals(returnTypeReference.getText())) {
                FieldInfo fieldInfo = new FieldInfo(ktFunction.getProject(), returnTypeReference);
                this.response = fieldInfo;
                this.setResponseFields(fieldInfo.getChildren());
            }
        }
    }

    public boolean containRequestBodyAnnotation() {
        return funStr.contains(WebAnnotation.RequestBody);
    }

    public boolean containRestControllerAnnotation() {
        for (String annotationText : classAnnotationTexts) {
            if(annotationText.contains(WebAnnotation.RestController)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param ktFunction
     * @return
     * @todo
     */
    private String extraClassPathKt(KtFunction ktFunction) {
        return null;
    }

    private String extraMethodPathKt(KtFunction ktFunction) {
        for (KtAnnotationEntry annotationEntry : ktFunction.getAnnotationEntries()) {
            if (annotationEntry.getText().contains("Mapping")) {
                KtValueArgumentList valueArgumentList = annotationEntry.getValueArgumentList();
                if (valueArgumentList != null) {
                    for (KtValueArgument ktValueArgument : valueArgumentList.getArguments()) {
                        if (ktValueArgument.getArgumentName() == null) {
                            return ktValueArgument.getText().replace("\"", "");
                        }
                        if ("value".equals(ktValueArgument.getArgumentName().getText())) {
                            KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
                            String text = null;
                            if (argumentExpression == null) {
                                return "";
                            }
                            if (argumentExpression instanceof KtCollectionLiteralExpression) {
                                KtCollectionLiteralExpression collectionLiteralExpression =
                                        (KtCollectionLiteralExpression) argumentExpression;
                                List<KtExpression> innerExpressions = collectionLiteralExpression.getInnerExpressions();
                                if (CollectionUtils.isNotEmpty(innerExpressions)) {
                                    text = innerExpressions.get(0).getText();
                                }
                            } else {
                                text = argumentExpression.getText();
                            }
                            return text == null ? "" : text.replace("\"", "");
                        }
                    }
                }
            }
        }
        return "";
    }

    private List<FieldInfo> listParamFieldInfos(KtFunction ktFunction) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        Map<String, String> paramNameDescMap = getParamDescMap(ktFunction.getDocComment());
        List<KtParameter> parameters = ktFunction.getValueParameterList().getParameters();
        for (KtParameter parameter : parameters) {
            KtTypeReference ktTypeReference = parameter.getTypeReference();
            FieldInfo fieldInfo = new FieldInfo(
                    ktFunction.getProject(),
                    parameter.getName(),
                    ktTypeReference,
                    paramNameDescMap.get(parameter.getName()),
                    parameter.getAnnotationEntries()
            );
            fieldInfoList.add(fieldInfo);
        }
        return fieldInfoList;
    }

    private List<FieldInfo> listParamFieldInfos(PsiMethod psiMethod) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        Map<String, String> paramNameDescMap = getParamDescMap(psiMethod.getDocComment());
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            PsiType psiType = psiParameter.getType();
            if (excludeParamTypes.contains(psiType.getPresentableText())) {
                continue;
            }
            FieldInfo fieldInfo = new FieldInfo(
                    psiMethod.getProject(),
                    psiParameter.getName(),
                    psiType,
                    paramNameDescMap.get(psiParameter.getName()),
                    psiParameter.getAnnotations()
            );
            fieldInfoList.add(fieldInfo);
        }
        return fieldInfoList;
    }

    private String extraClassPath(PsiMethod psiMethod) {
        String path = "";
        for (PsiAnnotation annotation : Objects.requireNonNull(psiMethod.getContainingClass()).getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                classPath = getPathFromAnnotation(annotation);
                break;
            }
        }
        return path;
    }

    private String extraMethodPath(PsiMethod psiMethod) {
        String methodPath = "";
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                methodPath = getPathFromAnnotation(annotation);
                break;
            }
        }
        return methodPath;
    }

    private String getPathFromAnnotation(PsiAnnotation annotation) {
        if (annotation == null) {
            return "";
        }
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        if (psiNameValuePairs.length == 1 && psiNameValuePairs[0].getName() == null) {
            return appendSlash(psiNameValuePairs[0].getLiteralValue());
        }
        if (psiNameValuePairs.length >= 1) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if (psiNameValuePair.getName().equals("value") || psiNameValuePair.getName().equals("path")) {
                    return appendSlash(psiNameValuePair.getLiteralValue());
                }
            }
        }
        return "";
    }

    private String appendSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String p = path;
        if (!path.startsWith(SLASH)) {
            p = SLASH + path;
        }
        if (path.endsWith(SLASH)) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private Map<String, String> getParamDescMap(PsiDocComment docComment) {
        Map<String, String> paramDescMap = new HashMap<>();
        if (docComment == null) {
            return paramDescMap;
        }
        for (PsiDocTag docTag : docComment.getTags()) {
            String tagValue = docTag.getValueElement() == null ? "" : docTag.getValueElement().getText();
            if ("param".equals(docTag.getName()) && StringUtils.isNotEmpty(tagValue)) {
                paramDescMap.put(tagValue, getParamDesc(docTag.getText()));
            }
        }
        return paramDescMap;
    }

    private Map<String, String> getParamDescMap(KDoc docComment) {
        Map<String, String> paramDescMap = new HashMap<>();
        if (docComment == null) {
            return paramDescMap;
        }
        List<KDocTag> kDocTags = docComment.getDefaultSection().findTagsByName("param");
        for (KDocTag kDocTag : kDocTags) {
            String name = kDocTag.getName();
            String subjectName = kDocTag.getSubjectName();
            String linkText = kDocTag.getSubjectLink().getLinkText();
            String content = kDocTag.getContent();
            paramDescMap.put(subjectName, content);
        }
        return paramDescMap;
    }

    private String getParamDesc(String tagText) {
        String[] strings = tagText.replace("*", "").trim().split(" ");
        if (strings.length == 3) {
            String desc = strings[2];
            return desc.replace("\n", "");
        }
        return "";
    }

}
