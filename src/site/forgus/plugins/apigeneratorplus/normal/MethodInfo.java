package site.forgus.plugins.apigeneratorplus.normal;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import site.forgus.plugins.apigeneratorplus.constant.CUrlClientType;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.util.DesUtil;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.*;

@Data
public class MethodInfo implements Serializable {
    private static final long serialVersionUID = -9143203778013000538L;

    private String desc;
    private String packageName;
    private String className;
    private String returnStr;
    private String paramStr;
    private String methodName;
    private List<FieldInfo> requestFields;
    private List<FieldInfo> responseFields;
    private FieldInfo response;

    private List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");

    public MethodInfo(PsiMethod psiMethod) {
        this.setDesc(DesUtil.getDescription(psiMethod));
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return;
        }
        this.setPackageName(PsiUtil.getPackageName(psiClass));
        this.setClassName(psiClass.getName());
        PsiType returnType = psiMethod.getReturnType();
        if (returnType != null) {
            this.setReturnStr(returnType.getPresentableText());
        }
        this.setParamStr(psiMethod.getParameterList().getText());
        this.setMethodName(psiMethod.getName());
        this.setRequestFields(listParamFieldInfos(psiMethod));
        FieldInfo fieldInfo = new FieldInfo(psiMethod.getProject(), psiMethod.getReturnType());
        this.response = fieldInfo;
        this.setResponseFields(fieldInfo.getChildren());
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

    private String getParamDesc(String tagText) {
        String[] strings = tagText.replace("*", "").trim().split(" ");
        if (strings.length == 3) {
            String desc = strings[2];
            return desc.replace("\n", "");
        }
        return "";
    }

    public String getCurlRequestBody(PsiMethod psiMethod, CUrlClientType cUrlClientType) {
        StringUtil.showPsiMethod(psiMethod);
        if (containRequestBodyAnnotation(psiMethod)) {
            for (FieldInfo requestField : this.requestFields) {
                if (containRequestBodyAnnotation((PsiAnnotation[]) requestField.getAnnotations().toArray())) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(" -H 'Content-Type: application/json;charset=UTF-8'");
                    stringBuilder.append(" --data-binary '");
                    String s = JsonUtil.buildRawJson(requestField);
//                    if(cUrlClientType.equals(CUrlClientType.CMD)){
//                        s = s.replace("\"", "^\\^\"");
//                        s = s.replace("{", "^{");
//                        s = s.replace("}", "^}");
//                        System.out.println(s );
//                    }
                    stringBuilder.append(s)
                            .append("'");
                    return stringBuilder.toString();
                }
            }
        } else {
            List<String> strings = generateKeyValue(this.requestFields);
            StringBuilder stringBuilder = new StringBuilder("");
            stringBuilder.append(" -H 'Content-Type: application/x-www-form-urlencoded'");
            stringBuilder.append(" --data-raw '");
            for (String string : strings) {
//                stringBuilder.append(string).append(cUrlClientType.getSymbolAnd());
                stringBuilder.append(string).append("&");
            }
            stringBuilder.append("'");
            return stringBuilder.toString();
        }
        return "";
    }

    public String getCurlRequestParams(PsiMethod psiMethod, CUrlClientType cUrlClientType) {
        StringUtil.showPsiMethod(psiMethod);
        List<String> strings = generateKeyValue(this.requestFields);
        StringBuilder stringBuilder = new StringBuilder("?");
        for (String string : strings) {
//            stringBuilder.append(string).append(cUrlClientType.getSymbolAnd());
            stringBuilder.append(string).append("&");
        }
        String str = stringBuilder.toString();
//        str = str.replaceAll("%", "^%");
        return str;
    }

    private List<String> generateKeyValue(List<FieldInfo> fieldInfoList) {
        ArrayList<String> strings = new ArrayList<>();
        for (FieldInfo requestField : fieldInfoList) {
            if (requestField.hasChildren()) {
                strings.addAll(generateKeyValue(requestField.getChildren()));
            } else {
                Object value = FieldUtil.getValue(requestField.getPsiType());
                String strVal = "";
                if(null != value){
                    strVal = URLEncoder.encode(value.toString());
                }
                strings.add(requestField.getName() + "=" + strVal);
            }
        }
        return strings;
    }

    private boolean containRequestBodyAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                return true;
            }
        }
        return false;
    }

    private boolean containRequestBodyAnnotation(PsiMethod psiMethod) {
        if (containRequestBodyAnnotation(psiMethod.getAnnotations())) {
            return true;
        }
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            if (parameter.getText().contains(WebAnnotation.RequestBody)) {
                return true;
            }
        }
        return false;
    }
}
