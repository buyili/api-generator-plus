package site.forgus.plugins.apigeneratorplus.normal;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.kotlin.psi.*;
import site.forgus.plugins.apigeneratorplus.config.ApiGeneratorConfig;
import site.forgus.plugins.apigeneratorplus.constant.TypeEnum;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.util.AssertUtils;
import site.forgus.plugins.apigeneratorplus.util.DesUtil;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;

import java.util.*;

@Data
public class FieldInfo {

    private String name;
    private PsiType psiType;
    private boolean require;
    private String range;
    private String desc;
    private TypeEnum paramType;
    private List<FieldInfo> children;
    private FieldInfo parent;
    private List<PsiAnnotation> annotations;
    private Project project;
    private Map<PsiTypeParameter, PsiType> genericsMap;

    private KtTypeReference ktTypeReference;
    private List<KtAnnotationEntry> ktAnnotationEntries;
    private Map<KtTypeReference, KtTypeReference> ktGenericsMap;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldInfo fieldInfo = (FieldInfo) o;
        return name.equals(fieldInfo.name) &&
                Objects.equals(parent, fieldInfo.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    private static List<String> requiredTexts = Arrays.asList("@NotNull", "@NotBlank", "@NotEmpty", "@PathVariable");

    protected ApiGeneratorConfig config;

    public FieldInfo(Project project, PsiType psiType) {
        this(project, psiType, "", new PsiAnnotation[0]);
    }

    public FieldInfo(Project project, String name, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this.project = project;
        config = ServiceManager.getService(project, ApiGeneratorConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.psiType = psiType;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.annotations = Arrays.asList(annotations);
        this.genericsMap = resolveGenerics(psiType);
        if (psiType != null) {
            if (FieldUtil.isNormalType(psiType)) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(psiType)) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(psiType)) {
                this.children = listChildren(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, String name, KtTypeReference ktTypeReference, String desc, List<KtAnnotationEntry> annotations) {
        this.project = project;
        config = ServiceManager.getService(project, ApiGeneratorConfig.class);
//        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.ktTypeReference = ktTypeReference;
//        this.require = requireAndRange.isRequire();
//        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.ktAnnotationEntries = annotations;
//        this.genericsMap = resolveGenerics(ktTypeReference);
        if (ktTypeReference != null) {
            if (FieldUtil.isNormalType(ktTypeReference.getText())) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(ktTypeReference.getText())) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(ktTypeReference)) {
                this.children = listChildrenKt(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, FieldInfo parent, String name, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this.project = project;
        config = ServiceManager.getService(project, ApiGeneratorConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.psiType = psiType;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.annotations = Arrays.asList(annotations);
        this.parent = parent;
        if (psiType != null) {
            if (FieldUtil.isNormalType(psiType)) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(psiType)) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(parent, psiType)) {
                this.children = listChildren(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, FieldInfo parent, KtTypeReference ktTypeReference, String name, String desc, PsiAnnotation[] annotations) {
        this.project = project;
        config = ServiceManager.getService(project, ApiGeneratorConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.ktTypeReference = ktTypeReference;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.annotations = Arrays.asList(annotations);
        this.parent = parent;
        if (ktTypeReference != null) {
            if (FieldUtil.isNormalType(ktTypeReference.getText())) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(ktTypeReference.getText())) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(parent, ktTypeReference)) {
                this.children = listChildren(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this(project, psiType.getPresentableText(), psiType, desc, annotations);
    }

    private String getParamName(String name, PsiAnnotation[] annotations) {
        PsiAnnotation requestParamAnnotation = getRequestParamAnnotation(annotations);
        if (requestParamAnnotation == null) {
            return name;
        }
        PsiNameValuePair[] attributes = requestParamAnnotation.getParameterList().getAttributes();
        if (attributes.length == 1 && attributes[0].getName() == null) {
            return attributes[0].getLiteralValue();
        }
        for (PsiNameValuePair psiNameValuePair : attributes) {
            String pairName = psiNameValuePair.getName();
            if ("value".equals(pairName) || "name".equals(pairName)) {
                return psiNameValuePair.getLiteralValue();
            }
        }
        return name;
    }

    private String getParamName(String name, List<KtAnnotationEntry> annotations) {
        KtAnnotationEntry requestParamAnnotation = getRequestParamAnnotation(annotations);
        if (requestParamAnnotation == null) {
            return name;
        }
        KtValueArgumentList valueArgumentList = requestParamAnnotation.getValueArgumentList();
        if (valueArgumentList != null) {
            for (KtValueArgument ktValueArgument : valueArgumentList.getArguments()) {
                if (ktValueArgument.getArgumentName() == null) {
                    return ktValueArgument.getText().replace("\"", "");
                }
                if ("name".equals(ktValueArgument.getArgumentName().getText())) {
                    return ktValueArgument.getArgumentExpression().getText().replace("\"", "");
                }
            }
        }
        return name;
    }

    private PsiAnnotation getRequestParamAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestParam)) {
                return annotation;
            }
        }
        return null;
    }

    private KtAnnotationEntry getRequestParamAnnotation(List<KtAnnotationEntry> annotations) {
        for (KtAnnotationEntry annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestParam)) {
                return annotation;
            }
        }
        return null;
    }

    private List<FieldInfo> listChildren(FieldInfo fieldInfo) {
        PsiType psiType = fieldInfo.getPsiType();
        if (psiType == null) {
            return new ArrayList<>();
        }
        if (FieldUtil.isNormalType(psiType.getPresentableText())) {
            //基础类或基础包装类没有子域
            return new ArrayList<>();
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        if (psiType instanceof PsiClassReferenceType) {
            //如果是集合类型
            if (FieldUtil.isIterableType(psiType)) {
                PsiType iterableType = PsiUtil.extractIterableTypeParameter(psiType, false);
                iterableType = getTypeByGenerics(iterableType);
                if (iterableType == null || FieldUtil.isNormalType(iterableType.getPresentableText()) || isMapType(iterableType)) {
                    return new ArrayList<>();
                }
                return listChildren(new FieldInfo(fieldInfo.getProject(), fieldInfo, iterableType.getPresentableText(), iterableType, "", new PsiAnnotation[0]));
            }
            String typeName = psiType.getPresentableText();
            if (typeName.startsWith("Map")) {
                fieldInfos.add(new FieldInfo(project, fieldInfo, typeName, null, "", new PsiAnnotation[0]));
                return fieldInfos;
            }
            if (typeName.contains("<")) {
                PsiClass outerClass = PsiUtil.resolveClassInType(psiType);
                for (PsiField outField : outerClass.getAllFields()) {
                    PsiType type = outField.getType();
                    if (config.getState().excludeFields.contains(outField.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project, fieldInfo, outField.getName(), type, DesUtil.getDescription(outField.getDocComment()), outField.getAnnotations()));
                }
                return fieldInfos;
            }
            PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
            if (psiClass == null) {
                return new ArrayList<>();
            }
            for (PsiField psiField : psiClass.getAllFields()) {
                if (config.getState().excludeFields.contains(psiField.getName())) {
                    continue;
                }
                fieldInfos.add(new FieldInfo(project, fieldInfo, psiField.getName(), psiField.getType(), DesUtil.getDescription(psiField.getDocComment()), psiField.getAnnotations()));
            }
            return fieldInfos;
        }
        return new ArrayList<>();
    }

    private List<FieldInfo> listChildrenKt(FieldInfo fieldInfo) {
        KtTypeReference ktTypeReference = fieldInfo.getKtTypeReference();
        if (ktTypeReference == null) {
            return new ArrayList<>();
        }
        if (FieldUtil.isNormalType(ktTypeReference.getText())) {
            //基础类或基础包装类没有子域
            return new ArrayList<>();
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        if (ktTypeReference instanceof KtTypeReference) {
            //如果是集合类型
            if (FieldUtil.isIterableType(ktTypeReference.getText())) {
                List<KtTypeReference> typeArgumentsAsTypes = ktTypeReference.getTypeElement().getTypeArgumentsAsTypes();
                KtTypeReference iterableType = null;
                for (KtTypeReference typeArgumentsAsType : typeArgumentsAsTypes) {
                    iterableType = typeArgumentsAsType;
//                    String text = typeArgumentsAsType.getText();
//                    System.out.println(true);
                }
                iterableType = getTypeByGenerics(iterableType);
                if (iterableType == null || FieldUtil.isNormalType(iterableType.getText()) || isMapType(iterableType)) {
                    return new ArrayList<>();
                }
                return listChildren(new FieldInfo(fieldInfo.getProject(), fieldInfo, iterableType, iterableType.getText(), "", new PsiAnnotation[0]));
            }
            String typeName = ktTypeReference.getText();
            if (typeName.startsWith("Map")) {
                fieldInfos.add(new FieldInfo(project,fieldInfo, null, typeName, "", new PsiAnnotation[0]));
                return fieldInfos;
            }
            if (typeName.contains("<")) {
                KtTypeReference outerKtType = ktTypeReference.getTypeElement().getTypeArgumentsAsTypes().get(0);

                PsiClass outerClass = PsiUtil.resolveClassInType(ktTypeReference);
                for (PsiField outField : outerClass.getAllFields()) {
                    PsiType type = outField.getType();
                    if (config.getState().excludeFields.contains(outField.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project,fieldInfo, outField.getName(), type, DesUtil.getDescription(outField.getDocComment()), outField.getAnnotations()));
                }
                return fieldInfos;
            }
//            PsiClass psiClass = PsiUtil.resolveClassInType(ktTypeReference);
//            if (psiClass == null) {
//                return new ArrayList<>();
//            }
//            for (PsiField psiField : psiClass.getAllFields()) {
//                if (config.getState().excludeFields.contains(psiField.getName())) {
//                    continue;
//                }
//                fieldInfos.add(new FieldInfo(project,fieldInfo, psiField.getName(), psiField.getType(), DesUtil.getDescription(psiField.getDocComment()), psiField.getAnnotations()));
//            }
            return fieldInfos;
        }
        return new ArrayList<>();
    }

    private boolean needResolveChildren(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return false;
            }
        }
        return !isMapType(psiType);
    }

    private boolean needResolveChildren(KtTypeReference ktTypeReference) {
//        PsiClass psiClass = PsiUtil.resolveClassInType(ktTypeReference);
//        if(psiClass != null) {
//            if(psiClass.isEnum()) {
//                return false;
//            }
//        }
        return !isMapType(ktTypeReference);
    }

    private boolean needResolveChildren(FieldInfo parent, PsiType psiType) {
        if (parent == null) {
            return true;
        }
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return false;
            }
        }
        if (isMapType(psiType)) {
            return false;
        }
        Set<PsiType> resolvedTypeSet = new HashSet<>();
        FieldInfo p = parent;
        while (p != null) {
            resolvedTypeSet.add(p.getPsiType());
            p = p.getParent();
        }
        if (TypeEnum.ARRAY.equals(paramType)) {
            psiType = PsiUtil.extractIterableTypeParameter(psiType, false);
        }
        for (PsiType resolvedType : resolvedTypeSet) {
            if (resolvedType.equals(psiType)) {
                return false;
            }
        }
        return true;
    }

    private boolean needResolveChildren(FieldInfo parent, KtTypeReference ktTypeReference) {
        if (parent == null) {
            return true;
        }
//        PsiClass psiClass = PsiUtil.resolveClassInType(ktTypeReference);
//        if(psiClass != null) {
//            if(psiClass.isEnum()) {
//                return false;
//            }
//        }
        if (isMapType(ktTypeReference)) {
            return false;
        }
        Set<PsiType> resolvedTypeSet = new HashSet<>();
        FieldInfo p = parent;
        while (p != null) {
            resolvedTypeSet.add(p.getPsiType());
            p = p.getParent();
        }
        if (TypeEnum.ARRAY.equals(paramType)) {
            // @todo
//            ktTypeReference = PsiUtil.extractIterableTypeParameter(ktTypeReference, false);
        }
        for (PsiType resolvedType : resolvedTypeSet) {
            if (resolvedType.equals(ktTypeReference)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMapType(PsiType psiType) {
        String presentableText = psiType.getPresentableText();
        List<String> mapList = Arrays.asList("Map", "HashMap", "LinkedHashMap", "JSONObject");
        if (mapList.contains(presentableText)) {
            return true;
        }
        return presentableText.startsWith("Map<") || presentableText.startsWith("HashMap<") || presentableText.startsWith("LinkedHashMap<");
    }

    private boolean isMapType(KtTypeReference ktTypeReference) {
        String presentableText = ktTypeReference.getText();
        List<String> mapList = Arrays.asList("Map", "HashMap", "LinkedHashMap", "JSONObject");
        if (mapList.contains(presentableText)) {
            return true;
        }
        return presentableText.startsWith("Map<") || presentableText.startsWith("HashMap<") || presentableText.startsWith("LinkedHashMap<");
    }

    private boolean containGeneric(String str) {
        for (String generic : FieldUtil.genericList) {
            if (str.contains(generic)) {
                return true;
            }
        }
        return false;
    }

    private RequireAndRange getRequireAndRange(PsiAnnotation[] annotations) {
        if (annotations.length == 0) {
            return RequireAndRange.instance();
        }
        boolean require = false;
        String min = "";
        String max = "";
        String range = "N/A";
        for (PsiAnnotation annotation : annotations) {
            if (isParamRequired(annotation)) {
                require = true;
                break;
            }
        }
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                PsiAnnotationMemberValue minValue = annotation.findAttributeValue("min");
                if (minValue != null) {
                    min = minValue.getText();
                    break;
                }
            }
            if (qualifiedName.contains("Min")) {
                PsiAnnotationMemberValue minValue = annotation.findAttributeValue("value");
                if (minValue != null) {
                    min = minValue.getText();
                    break;
                }
            }
        }
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                PsiAnnotationMemberValue maxValue = annotation.findAttributeValue("max");
                if (maxValue != null) {
                    max = maxValue.getText();
                    break;
                }
            }
            if (qualifiedName.contains("Max")) {
                PsiAnnotationMemberValue maxValue = annotation.findAttributeValue("value");
                if (maxValue != null) {
                    max = maxValue.getText();
                    break;
                }
            }
        }
        if (StringUtils.isNotEmpty(min) || StringUtils.isNotEmpty(max)) {
            range = "[" + min + "," + max + "]";
        }
        return new RequireAndRange(require, range);
    }

    private boolean isParamRequired(PsiAnnotation annotation) {
        String annotationText = annotation.getText();
        if (annotationText.contains(WebAnnotation.RequestParam)) {
            PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if ("required".equals(psiNameValuePair.getName()) && "false".equals(psiNameValuePair.getLiteralValue())) {
                    return false;
                }
            }
            return true;
        }
        return requiredTexts.contains(annotationText.split("\\(")[0]);
    }

    public boolean hasChildren() {
        return AssertUtils.isNotEmpty(children);
    }

    /**
     * 提取泛型对应的PsiType
     *
     * @param psiType
     * @return
     */
    private Map<PsiTypeParameter, PsiType> resolveGenerics(PsiType psiType) {
        PsiClassType psiClassType = (PsiClassType) psiType;
        PsiType[] parameters = psiClassType.getParameters();

        PsiClass resolve = ((PsiClassType) psiType).resolve();
        PsiTypeParameter[] typeParameters = resolve.getTypeParameters();
        int i = 0;
        Map<PsiTypeParameter, PsiType> map = new HashMap<>();
        for (PsiTypeParameter typeParameter : typeParameters) {
            map.put(typeParameter, parameters[i]);
            i++;
        }
        return map;
    }

    /**
     * 提取泛型对应的PsiType
     * @Todo
     * @param ktTypeReference
     * @return
     */
    private Map<PsiTypeParameter, PsiType> resolveGenerics(KtTypeReference ktTypeReference) {
//        PsiClassType psiClassType = (PsiClassType) ktTypeReference;
//        PsiType[] parameters = psiClassType.getParameters();
//
//        PsiClass resolve = ((PsiClassType) ktTypeReference).resolve();
//        PsiTypeParameter[] typeParameters = resolve.getTypeParameters();
//        int i = 0;
        Map<PsiTypeParameter, PsiType> map = new HashMap<>();
//        for (PsiTypeParameter typeParameter : typeParameters) {
//            map.put(typeParameter, parameters[i]);
//            i++;
//        }
        return map;
    }

    /**
     * 根据泛型获取对应的PsiType
     *
     * @param psiType
     * @return
     */
    private PsiType getTypeByGenerics(PsiType psiType) {
        if (null == psiType) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getTypeByGenerics(psiType);
        }
        if (null != genericsMap) {
            for (PsiTypeParameter psiTypeParameter : genericsMap.keySet()) {
                if (psiTypeParameter.getName().equals(psiType.getPresentableText())) {
                    return genericsMap.get(psiTypeParameter);
                }
            }
        }
        return psiType;
    }

    /**
     * 根据泛型获取对应的PsiType
     *
     * @param ktTypeReference
     * @return
     * @todo
     */
    private KtTypeReference getTypeByGenerics(KtTypeReference ktTypeReference) {
        if (null == ktTypeReference) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getTypeByGenerics(ktTypeReference);
        }
        if (null != ktGenericsMap) {
            for (KtTypeReference psiTypeParameter : ktGenericsMap.keySet()) {
                if (psiTypeParameter.getName().equals(ktTypeReference.getText())) {
                    return ktGenericsMap.get(psiTypeParameter);
                }
            }
        }
        return ktTypeReference;
    }

}
