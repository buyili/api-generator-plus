package site.forgus.plugins.apigeneratorplus.normal;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import site.forgus.plugins.apigeneratorplus.util.StringUtils;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import org.jetbrains.kotlin.psi.*;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.exception.ReportException;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.util.DesUtil;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.MethodUtil;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiInterface;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class MethodInfo implements Serializable {
    private static final long serialVersionUID = -9143203778013000538L;

    private static final String SLASH = "/";

    private String title;
    private String desc;
    private String packageName;
    private String className;

    // 返回类型文本
    private String returnStr;
    private String paramStr;
    private String methodName;
    private List<FieldInfo> requestFields;
    private List<FieldInfo> responseFields;
    private FieldInfo response;
    private PsiMethod psiMethod;
    private MediaType requestMediaType;
    private YApiInterface yApiInterface;

    private Language language;
    private KtFunction ktFunction;
    private RequestMethodEnum requestMethod;
    private String methodPath;
    private String classPath;
    private String funStr;
    private List<String> classAnnotationTexts;
    // YApi分类名称
    private String catName;

    private List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");

    public MethodInfo(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
        this.language = psiMethod.getLanguage();
        this.requestMediaType = MethodUtil.getRequestMediaType(psiMethod);
        this.setFunStr(psiMethod.getText());
        this.setRequestMethod(MethodUtil.getRequestMethod(psiMethod.getText()));
        this.setMethodPath(extraMethodPath(psiMethod));
        this.setClassPath(extraClassPath(psiMethod));
        this.setDesc(DesUtil.getInterfaceDesc(psiMethod));
        this.setTitle(DesUtil.getInterfaceTitle(psiMethod));
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return;
        }
        this.setPackageName(PsiUtil.getPackageName(psiClass));
        this.setClassName(psiClass.getName());
        PsiDocComment docComment = psiClass.getDocComment();
        String classCatName = DesUtil.getInterfaceCatName(docComment);
        this.catName = classCatName;

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
                PsiType responseType = returnType;
                String canonicalText = returnType.getCanonicalText();
                if (canonicalText.contains("org.springframework.http.ResponseEntity")) {
                    PsiType[] parameters = ((PsiClassType) returnType).getParameters();
                    responseType = parameters[0];
                    System.out.println();
                }
                FieldInfo fieldInfo = new FieldInfo(psiMethod.getProject(), responseType,
                        getReturnDesc(psiMethod.getDocComment()));
                this.response = fieldInfo;
                this.setResponseFields(fieldInfo.getChildren());
            }
        }
        yApiInterface = buildDocYApiInterface(psiMethod.getDocComment());
        System.out.println();
    }

    public MethodInfo(KtFunction ktFunction) {
        this.ktFunction = ktFunction;
        this.language = ktFunction.getLanguage();
        this.requestMediaType = MethodUtil.getRequestMediaType(ktFunction);
        this.setFunStr(ktFunction.getText());
        this.setParamStr(ktFunction.getValueParameterList().getText());
        this.setMethodName(ktFunction.getName());
        this.setRequestFields(listParamFieldInfos(ktFunction));
        this.setRequestMethod(MethodUtil.getRequestMethod(ktFunction.getText()));
        this.setMethodPath(extraMethodPathKt(ktFunction));
        this.setClassPath(extraClassPathKt(ktFunction));
        this.setDesc(DesUtil.getInterfaceDesc(ktFunction));
        this.setTitle(DesUtil.getInterfaceTitle(ktFunction));

        KtClass ktClass = (KtClass) ktFunction.getParent().getParent();
        this.setPackageName(ktClass.getFqName().toString());
        this.setClassName(ktClass.getName());
        KDoc docComment = ktClass.getDocComment();
        String classCatName = DesUtil.getInterfaceCatName(docComment);
        this.catName = classCatName;

        List<String> classAnnotationTexts = new ArrayList<>();
        for (KtAnnotationEntry annotationEntry : ktClass.getAnnotationEntries()) {
            classAnnotationTexts.add(annotationEntry.getText());
        }
        this.setClassAnnotationTexts(classAnnotationTexts);

        KtTypeReference returnTypeReference = ktFunction.getTypeReference();
        if (returnTypeReference != null) {
            this.setReturnStr(returnTypeReference.getText());
            if (!"Void".equals(returnTypeReference.getText())) {
                FieldInfo fieldInfo = new FieldInfo(ktFunction.getProject(), returnTypeReference,
                        getReturnDesc(ktFunction.getDocComment()));
                this.response = fieldInfo;
                this.setResponseFields(fieldInfo.getChildren());
            }
        }
        yApiInterface = buildDocYApiInterface(ktFunction.getDocComment());
        System.out.println();
    }

    private YApiInterface buildDocYApiInterface(PsiDocComment docComment) {
        if (null == docComment) {
            return null;
        }
        YApiInterface yApiInterface = new YApiInterface();
        PsiDocTag[] tags = docComment.getTags();
        for (PsiDocTag tag : tags) {
            if ("res_body".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                String text = tag.getText();
                String res_body = text.replace("@res_body", "")
                        .replaceAll("\n *\\*", "\n");
                //res_body = res_body.trim();
                if(res_body.endsWith("\n")){
                    res_body = res_body.substring(0, res_body.length() -1);
                }
                yApiInterface.setRes_body(res_body);
            }
            if ("res_body_type".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                PsiDocTagValue valueElement = tag.getValueElement();
                String type = null;
                if (valueElement != null) {
                    type = valueElement.getText();
                }
                yApiInterface.setRes_body_type(type);
            }
            if ("res_body_is_json_schema".equals(tag.getName())) {
//                StringUtil.testTag(tag);
                PsiDocTagValue valueElement = tag.getValueElement();
                Boolean aBoolean = null;
                if (valueElement != null) {
                    try {
                        if (!"ignore".equals(valueElement.getText().trim())) {
                            aBoolean = Boolean.parseBoolean(valueElement.getText());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                yApiInterface.setRes_body_is_json_schema(aBoolean);
            }
        }
        return yApiInterface;
    }

    private YApiInterface buildDocYApiInterface(KDoc kDoc) {
        if (null == kDoc) {
            return null;
        }
        YApiInterface yApiInterface = new YApiInterface();
        List<KDocTag> resBodyTags = kDoc.getDefaultSection().findTagsByName("res_body");
        for (KDocTag resBodyTag : resBodyTags) {
            yApiInterface.setRes_body(DesUtil.getTagContent(resBodyTag.getContent()));
        }
        List<KDocTag> resBodyTypeTags = kDoc.getDefaultSection().findTagsByName("res_body_type");
        for (KDocTag resBodyTag : resBodyTypeTags) {
            yApiInterface.setRes_body_type(resBodyTag.getSubjectName());
        }
        List<KDocTag> resBodyIsJsonSchemaTags = kDoc.getDefaultSection().findTagsByName("res_body_is_json_schema");
        for (KDocTag tag : resBodyIsJsonSchemaTags) {
            Boolean aBoolean = null;
            try {
                aBoolean = Boolean.parseBoolean(tag.getSubjectName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            yApiInterface.setRes_body_is_json_schema(aBoolean);
        }
        return yApiInterface;
    }

    public boolean containRequestBodyAnnotation() {
        return funStr.contains(WebAnnotation.RequestBody);
    }

    public boolean containResponseBodyAnnotation() {
        return funStr.contains(WebAnnotation.ResponseBody);
    }

    public boolean containRestControllerAnnotation() {
        for (String annotationText : classAnnotationTexts) {
            if (annotationText.contains(WebAnnotation.RestController)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReturnJSON() {
        return containRequestBodyAnnotation() || containRestControllerAnnotation();
    }

    public boolean containControllerAnnotation() {
        for (String annotationText : classAnnotationTexts) {
            if (annotationText.contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param ktFunction
     * @return
     */
    private String extraClassPathKt(KtFunction ktFunction) {
        KtClass ktClass = (KtClass) ktFunction.getParent().getParent();
        for (KtAnnotationEntry annotationEntry : ktClass.getAnnotationEntries()) {
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
        List<PsiParameter> psiParameterList = FieldUtil.filterParameters(psiParameters);
        for (PsiParameter psiParameter : psiParameterList) {
            PsiType psiType = psiParameter.getType();
            FieldInfo fieldInfo = new FieldInfo(
                    psiMethod.getProject(),
                    psiParameter.getName(),
                    psiType,
                    paramNameDescMap.get(psiParameter.getName()),
                    psiParameter.getAnnotations()
            );
            fieldInfoList.add(fieldInfo);
        }
//        for (PsiParameter psiParameter : psiParameters) {
//            PsiType psiType = psiParameter.getType();
//            if (excludeParamTypes.contains(psiType.getPresentableText())) {
//                continue;
//            }
//            FieldInfo fieldInfo = new FieldInfo(
//                    psiMethod.getProject(),
//                    psiParameter.getName(),
//                    psiType,
//                    paramNameDescMap.get(psiParameter.getName()),
//                    psiParameter.getAnnotations()
//            );
//            fieldInfoList.add(fieldInfo);
//        }
        return fieldInfoList;
    }

    private String extraClassPath(PsiMethod psiMethod) {
        String path = "";
        for (PsiAnnotation annotation : Objects.requireNonNull(psiMethod.getContainingClass()).getAnnotations()) {
            if (annotation.getText().contains("Mapping")) {
                path = getPathFromAnnotation(annotation);
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
            PsiNameValuePair psiNameValuePair = psiNameValuePairs[0];
            return getPsiNameValuePairValue(psiNameValuePair);
        }
        if (psiNameValuePairs.length >= 1) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if ("value".equals(psiNameValuePair.getName()) || "path".equals(psiNameValuePair.getName())) {
                    return getPsiNameValuePairValue(psiNameValuePair);
                }
            }
        }
        return "";
    }

    private String getPsiNameValuePairValue(PsiNameValuePair psiNameValuePair){
        PsiAnnotationMemberValue value = psiNameValuePair.getValue();
        if(value instanceof PsiExpression){
            String stringValue = getPsiExpressionValue((PsiExpression) value);
            return appendSlash(stringValue);
        }
        return appendSlash(psiNameValuePair.getLiteralValue());
    }

    private String getPsiExpressionValue(PsiExpression psiExpression){
        if(psiExpression instanceof PsiPolyadicExpressionImpl) {
            PsiExpression[] operands = ((PsiPolyadicExpressionImpl) psiExpression).getOperands();
            return Arrays.stream(operands).map(this::getPsiExpressionValue).collect(Collectors.joining());
        }
        if(psiExpression instanceof PsiReferenceExpressionImpl){
            PsiElement psiElement = ((PsiReferenceExpressionImpl) psiExpression).resolve();
            if(psiElement instanceof PsiFieldImpl){
                PsiExpression tmpPsiExpression = ((PsiFieldImpl) psiElement).getInitializer();
                return getPsiExpressionValue(tmpPsiExpression);
            }
            assert psiElement != null;
            throw new ReportException("未知类型: " + psiElement.getClass().getName() + "请向作者反馈问题！");
        }
        if(psiExpression instanceof PsiBinaryExpressionImpl){
            PsiExpression[] operands = ((PsiBinaryExpressionImpl) psiExpression).getOperands();
            return Arrays.stream(operands).map(this::getPsiExpressionValue).collect(Collectors.joining());
        }
        if(psiExpression instanceof PsiLiteralExpressionImpl){
            return String.valueOf(((PsiLiteralExpressionImpl) psiExpression).getValue());
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
                paramDescMap.put(tagValue, getParamDesc(docTag));
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
//            StringUtil.testTag(kDocTag);
            String subjectName = kDocTag.getSubjectName();
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

    private String getParamDesc(PsiDocTag docTag) {
        PsiElement[] dataElements = docTag.getDataElements();
        String desc = "";
        for (int i = 1; i < dataElements.length; i++) {
            desc = desc + dataElements[i].getText();
        }
        return desc;
    }

    private String getReturnDesc(PsiDocComment docComment) {
        return DesUtil.getTagContent(docComment, "return");
    }

    private String getReturnDesc(KDoc docComment) {
        return DesUtil.getTagContent(docComment, "return");
    }

    @Override
    public String toString() {
        return "";
    }

}
