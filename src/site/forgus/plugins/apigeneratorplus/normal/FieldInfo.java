package site.forgus.plugins.apigeneratorplus.normal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.kotlin.psi.*;
import site.forgus.plugins.apigeneratorplus.config.ApiGeneratorConfig;
import site.forgus.plugins.apigeneratorplus.constant.TypeEnum;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.util.AssertUtils;
import site.forgus.plugins.apigeneratorplus.util.DesUtil;
import site.forgus.plugins.apigeneratorplus.util.FieldUtil;
import site.forgus.plugins.apigeneratorplus.util.KtUtil;

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
    @JsonIgnore
    private FieldInfo parent;
    private List<PsiAnnotation> annotations;
    private Project project;
    @Deprecated
    private Map<PsiTypeParameter, PsiType> genericsMap;
    private Map<String, PsiType> javaGenericsMap;

    private KtTypeReference ktTypeReference;
    private List<KtAnnotationEntry> ktAnnotationEntries;
    private Map<String, KtTypeReference> ktGenericsMap;
    private String canonicalText;
    private String typeText;
    private String iterableTypeStr;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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

    public FieldInfo(Project project, PsiType psiType, String desc) {
        this(project, psiType, desc, new PsiAnnotation[0]);
    }

    public FieldInfo(Project project, KtTypeReference ktTypeReference, String desc) {
        this(project, ktTypeReference, desc, ktTypeReference.getAnnotationEntries());
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
        this.javaGenericsMap = resolveJavaGenerics(psiType);
        if (psiType != null) {
            this.setTypeText(psiType.getPresentableText());
            this.setCanonicalText(psiType.getCanonicalText());
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

    public FieldInfo(Project project, String name, KtTypeReference ktTypeReference, String desc,
                     List<KtAnnotationEntry> annotations) {
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
        this.ktGenericsMap = resolveKtGenerics(ktTypeReference);
        if (ktTypeReference != null) {
            this.setTypeText(KtUtil.getText(ktTypeReference));
            String fqName = KtUtil.getFqName(ktTypeReference);
            this.setCanonicalText(fqName);
            if (FieldUtil.isNormalType(KtUtil.getText(ktTypeReference))) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(KtUtil.getText(ktTypeReference))) {
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
        this.javaGenericsMap = resolveJavaGenerics(psiType);
        this.parent = parent;
        if (psiType != null) {
            this.setTypeText(psiType.getPresentableText());
            this.setCanonicalText(psiType.getCanonicalText());
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

    public FieldInfo(Project project, FieldInfo parent, KtTypeReference ktTypeReference, String name, String desc,
                     List<KtAnnotationEntry> annotations) {
        this.project = project;
        config = ServiceManager.getService(project, ApiGeneratorConfig.class);
        RequireAndRange requireAndRange = getRequireAndRange(annotations);
        String fieldName = getParamName(name, annotations);
        this.name = fieldName == null ? "N/A" : fieldName;
        this.ktTypeReference = ktTypeReference;
        this.require = requireAndRange.isRequire();
        this.range = requireAndRange.getRange();
        this.desc = desc == null ? "" : desc;
        this.ktAnnotationEntries = annotations;
        this.ktGenericsMap = resolveKtGenerics(ktTypeReference);
        this.parent = parent;
        if (ktTypeReference != null) {
            this.setTypeText(KtUtil.getText(ktTypeReference));
            String fqName = KtUtil.getFqName(ktTypeReference);
            this.setCanonicalText(fqName);
            if (FieldUtil.isNormalType(KtUtil.getText(ktTypeReference))) {
                paramType = TypeEnum.LITERAL;
            } else if (FieldUtil.isIterableType(KtUtil.getText(ktTypeReference))) {
                paramType = TypeEnum.ARRAY;
            } else {
                paramType = TypeEnum.OBJECT;
            }
            if (needResolveChildren(parent, ktTypeReference)) {
                this.children = listChildrenKt(this);
            }
        } else {
            paramType = TypeEnum.OBJECT;
        }
    }

    public FieldInfo(Project project, PsiType psiType, String desc, PsiAnnotation[] annotations) {
        this(project, psiType.getPresentableText(), psiType, desc, annotations);
    }

    public FieldInfo(Project project, KtTypeReference ktTypeReference, String desc, List<KtAnnotationEntry> annotations) {
        this(project, ktTypeReference.getText(), ktTypeReference, desc, annotations);
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
                psiType = PsiUtil.extractIterableTypeParameter(psiType, false);
            }
            String typeName = psiType.getPresentableText();
            // 如果是泛型
            if (FieldUtil.isGenericType(typeName)) {
                Object tempType = getTypeByGenerics(typeName);
                if (tempType != null) {
                    if (tempType instanceof PsiType) {
                        PsiType genericType = (PsiType) tempType;
                        this.iterableTypeStr = genericType.getPresentableText();
                        if (FieldUtil.isNormalType(genericType.getPresentableText())
                                || FieldUtil.isMapType(genericType.getPresentableText())) {
                            return new ArrayList<>();
                        }
                        return listChildren(new FieldInfo(fieldInfo.getProject(), fieldInfo, genericType.getPresentableText(),
                                genericType, "", new PsiAnnotation[0]));
                    } else if (tempType instanceof KtTypeReference) {
                        KtTypeReference genericType = (KtTypeReference) tempType;
                        this.iterableTypeStr = genericType.getText();
                        if (FieldUtil.isNormalType(genericType.getText()) || FieldUtil.isMapType(genericType)) {
                            return new ArrayList<>();
                        }
                        return listChildrenKt(new FieldInfo(fieldInfo.getProject(), fieldInfo, genericType,
                                genericType.getText(), "", new ArrayList<>()));
                    }
                }
                return Collections.emptyList();
            }
            if (typeName.startsWith("Map")) {
                fieldInfos.add(new FieldInfo(project, fieldInfo, typeName, null, "", new PsiAnnotation[0]));
                return fieldInfos;
            }
            if (typeName.contains("<")) {
                PsiClass outerClass = PsiUtil.resolveClassInType(psiType);
                if (outerClass == null) {
                    return Collections.emptyList();
                }
                PsiField[] allFields = outerClass.getAllFields();
                for (PsiField outField : allFields) {
                    PsiType type = outField.getType();
                    if (config.getState().excludeFields.contains(outField.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project, fieldInfo, outField.getName(), type,
                            DesUtil.getDescription(outField.getDocComment()), outField.getAnnotations()));
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
                fieldInfos.add(new FieldInfo(project, fieldInfo, psiField.getName(), psiField.getType(),
                        DesUtil.getDescription(psiField.getDocComment()), psiField.getAnnotations()));
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
        if (FieldUtil.isNormalType(KtUtil.getText(ktTypeReference))) {
            //基础类或基础包装类没有子域
            return new ArrayList<>();
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        if (ktTypeReference instanceof KtTypeReference) {
            //如果是集合类型
            if (FieldUtil.isIterableType(KtUtil.getText(ktTypeReference))) {
                ktTypeReference = KtUtil.extractIterableTypeParameter(ktTypeReference);
            }

            String typeName = KtUtil.getText(ktTypeReference);
            // 如果是泛型
            if (FieldUtil.isGenericType(typeName)) {
                Object tempType = getTypeByGenerics(typeName);
                if (tempType != null) {
                    if (tempType instanceof PsiType) {
                        PsiType genericType = (PsiType) tempType;
                        this.iterableTypeStr = genericType.getPresentableText();
                        if (FieldUtil.isNormalType(genericType.getPresentableText())
                                || FieldUtil.isMapType(genericType)) {
                            return new ArrayList<>();
                        }
                        return listChildren(new FieldInfo(fieldInfo.getProject(), fieldInfo, genericType.getPresentableText(),
                                genericType, "", new PsiAnnotation[0]));
                    } else if (tempType instanceof KtTypeReference) {
                        KtTypeReference genericType = (KtTypeReference) tempType;
                        this.iterableTypeStr = genericType.getText();
                        if (FieldUtil.isNormalType(genericType.getText()) || FieldUtil.isMapType(genericType)) {
                            return new ArrayList<>();
                        }
                        return listChildrenKt(new FieldInfo(fieldInfo.getProject(), fieldInfo, genericType,
                                genericType.getText(), "", new ArrayList<>()));
                    }
                }
                return Collections.emptyList();
            }
            if (typeName.startsWith("Map")) {
                fieldInfos.add(new FieldInfo(project, fieldInfo, null, typeName, "", new ArrayList<>()));
                return fieldInfos;
            }
//            if (typeName.contains("<")) {
//                KtTypeReference genericType = ktTypeReference.getTypeElement().getTypeArgumentsAsTypes().get(0);
//                ktTypeReference = genericType;
////                return fieldInfos;
//            }
            PsiElement fieldClass = getPsiReference(ktTypeReference).resolve();
            if (fieldClass instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) fieldClass;
                for (PsiField outField : psiClass.getAllFields()) {
                    PsiType type = outField.getType();
                    if (config.getState().excludeFields.contains(outField.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project, fieldInfo, outField.getName(), type,
                            DesUtil.getDescription(outField.getDocComment()), outField.getAnnotations()));
                }
            } else if (fieldClass instanceof KtClass) {
                KtClass ktClass = (KtClass) fieldClass;
                for (KtProperty property : ktClass.getProperties()) {
                    KtTypeReference typeReference = property.getTypeReference();
                    if (config.getState().excludeFields.contains(property.getName())) {
                        continue;
                    }
                    fieldInfos.add(new FieldInfo(project, fieldInfo, typeReference, property.getName(),
                            "", property.getAnnotationEntries()));
                }
            }
            return fieldInfos;
        }
        return new ArrayList<>();
    }

    private PsiReference getPsiReference(KtTypeReference ktTypeReference) {
        PsiElement firstChild = ktTypeReference.getTypeElement().getFirstChild();
        PsiReference referenceAt = firstChild.findReferenceAt(0);
        return referenceAt;
    }

    private boolean needResolveChildren(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return false;
            }
        }
        return !FieldUtil.isMapType(psiType);
    }

    private boolean needResolveChildren(KtTypeReference ktTypeReference) {
//        PsiClass psiClass = PsiUtil.resolveClassInType(ktTypeReference);
//        if(psiClass != null) {
//            if(psiClass.isEnum()) {
//                return false;
//            }
//        }
        return !FieldUtil.isMapType(ktTypeReference);
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
        if (FieldUtil.isMapType(psiType)) {
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
            if (resolvedType != null && resolvedType.equals(psiType)) {
                return false;
            }
        }
        return true;
    }

    private boolean needResolveChildren(FieldInfo parent, KtTypeReference ktTypeReference) {
        if (parent == null) {
            return true;
        }
        PsiElement resolve = getPsiReference(ktTypeReference).resolve();
        if (resolve instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) resolve;
            if (psiClass.isEnum()) {
                return false;
            }
            if (FieldUtil.isMapType(psiClass.getName())) {
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
                if (resolvedType != null && resolvedType.equals(psiType)) {
                    return false;
                }
            }
        } else if (resolve instanceof KtClass) {
            KtClass ktClass = (KtClass) resolve;
            if (ktClass.isEnum()) {
                return false;
            }
            if (FieldUtil.isMapType(ktClass.getName())) {
                return false;
            }
//            Set<PsiType> resolvedTypeSet = new HashSet<>();
//            FieldInfo p = parent;
//            while (p != null) {
//                resolvedTypeSet.add(p.getPsiType());
//                p = p.getParent();
//            }
//            if (TypeEnum.ARRAY.equals(paramType)) {
//                // @todo
////            ktTypeReference = PsiUtil.extractIterableTypeParameter(ktTypeReference, false);
//            }
//            for (PsiType resolvedType : resolvedTypeSet) {
//                if (resolvedType != null && resolvedType.equals(ktTypeReference)) {
//                    return false;
//                }
//            }
        }
        return true;
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

    private RequireAndRange getRequireAndRange(List<KtAnnotationEntry> annotations) {
        if (annotations.size() == 0) {
            return RequireAndRange.instance();
        }
        boolean require = false;
        String min = "";
        String max = "";
        String range = "N/A";
        for (KtAnnotationEntry annotation : annotations) {
            if (isParamRequired(annotation)) {
                require = true;
                break;
            }
        }
        for (KtAnnotationEntry annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                KtValueArgumentList valueArgumentList = annotation.getValueArgumentList();
                if (valueArgumentList != null) {
                    for (KtValueArgument argument : valueArgumentList.getArguments()) {
                        if ("min".equals(argument.getName())) {
                            min = argument.getArgumentExpression().getName();
                        }
                        break;
                    }
                }
            }
            if (qualifiedName.contains("Min")) {
                KtValueArgumentList valueArgumentList = annotation.getValueArgumentList();
                if (valueArgumentList != null) {
                    for (KtValueArgument argument : valueArgumentList.getArguments()) {
                        if ("value".equals(argument.getName())) {
                            min = argument.getArgumentExpression().getName();
                        }
                        break;
                    }
                }
            }
        }
        for (KtAnnotationEntry annotation : annotations) {
            String qualifiedName = annotation.getText();
            if (qualifiedName.contains("Length") || qualifiedName.contains("Range") || qualifiedName.contains("Size")) {
                KtValueArgumentList valueArgumentList = annotation.getValueArgumentList();
                if (valueArgumentList != null) {
                    for (KtValueArgument argument : valueArgumentList.getArguments()) {
                        if ("max".equals(argument.getName())) {
                            min = argument.getArgumentExpression().getName();
                        }
                    }
                }
            }
            if (qualifiedName.contains("Max")) {
                KtValueArgumentList valueArgumentList = annotation.getValueArgumentList();
                if (valueArgumentList != null) {
                    for (KtValueArgument argument : valueArgumentList.getArguments()) {
                        if ("value".equals(argument.getName())) {
                            min = argument.getArgumentExpression().getName();
                        }
                        break;
                    }
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

    private boolean isParamRequired(KtAnnotationEntry annotation) {
        String annotationText = annotation.getText();
        if (annotationText.contains(WebAnnotation.RequestParam)) {
            KtValueArgumentList valueArgumentList = annotation.getValueArgumentList();
            if (valueArgumentList != null) {
                for (KtValueArgument argument : valueArgumentList.getArguments()) {
                    if ("required".equals(argument.getName()) && "false".equals(argument.getArgumentExpression())) {
                        return false;
                    }
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
     * @deprecated as of JDK 1.0.5, replace by {@link #resolveJavaGenerics(PsiType)}
     */
    @Deprecated
    private Map<PsiTypeParameter, PsiType> resolveGenerics(PsiType psiType) {
        // 拆解参数类型中的泛型类
        PsiClassType psiClassType = (PsiClassType) psiType;
        PsiType[] parameters = psiClassType.getParameters();

        // 拆解参数类型中的泛型 如 T、V
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

    private Map<String, PsiType> resolveJavaGenerics(PsiType psiType) {
        // 当字段类型为long 而不是 Long 时，psiType类型为 PsiPrimitiveType，cast to PsiClassType时报错，所以直接返回空map
        if (psiType instanceof PsiPrimitiveType) {
            return Collections.emptyMap();
        }
        // 拆解参数类型中的泛型类
        PsiClassType psiClassType = (PsiClassType) psiType;
        PsiType[] parameters = psiClassType.getParameters();
        if (parameters.length <= 0) {
            return Collections.emptyMap();
        }

        // 拆解参数类型中的泛型 如 T、V
        PsiClass resolve = ((PsiClassType) psiType).resolve();
        PsiTypeParameter[] typeParameters = resolve.getTypeParameters();
        if (typeParameters.length <= 0) {
            return Collections.emptyMap();
        }
        int i = 0;
        Map<String, PsiType> map = new HashMap<>();
        if (typeParameters.length == parameters.length) {
            for (PsiTypeParameter typeParameter : typeParameters) {
                map.put(typeParameter.getName(), parameters[i]);
                i++;
            }
        }
        return map;
    }

    /**
     * 提取泛型对应的PsiType
     *
     * @param ktTypeReference
     * @return
     */
    private Map<String, KtTypeReference> resolveKtGenerics(KtTypeReference ktTypeReference) {
        // 拆解参数类型中的泛型类
        List<KtTypeReference> typeArgumentsAsTypes = ktTypeReference.getTypeElement().getTypeArgumentsAsTypes();

        Map<String, KtTypeReference> map = new HashMap<>();
        int i = 0;

        PsiElement originClass = getPsiReference(ktTypeReference).resolve();
        if (originClass instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) originClass;
            PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
            for (PsiTypeParameter typeParameter : typeParameters) {
                map.put(typeParameter.getName(), typeArgumentsAsTypes.get(i));
                i++;
            }

        } else {
            KtClass ktClass = (KtClass) originClass;
            for (KtTypeParameter typeParameter : ktClass.getTypeParameters()) {
                map.put(typeParameter.getName(), typeArgumentsAsTypes.get(i));
                i++;
            }
        }
        return map;
    }

    /**
     * 根据泛型获取对应的PsiType
     *
     * @param psiType
     * @return
     */
    private PsiType getJavaTypeByGenerics(PsiType psiType) {
        if (null == psiType) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getJavaTypeByGenerics(psiType);
        }
        if (null != javaGenericsMap) {
            for (String keyStr : javaGenericsMap.keySet()) {
                if (keyStr.equals(psiType.getPresentableText())) {
                    return javaGenericsMap.get(keyStr);
                }
            }
        }
        return psiType;
    }

    private Object getTypeByGenerics(String genericName) {
        if (StringUtils.isBlank(genericName)) {
            return null;
        }
        if (this.parent != null) {
            Map<String, PsiType> parentGenericsMap = this.parent.javaGenericsMap;
            if (null != parentGenericsMap) {
                for (String genericKey : parentGenericsMap.keySet()) {
                    if (genericKey.equals(genericName)) {
                        return parentGenericsMap.get(genericKey);
                    }
                }
            }
            Map<String, KtTypeReference> parentKtGenericsMap = this.parent.ktGenericsMap;
            if (null != parentKtGenericsMap) {
                for (String genericKey : parentKtGenericsMap.keySet()) {
                    if (genericKey.equals(genericName)) {
                        return parentKtGenericsMap.get(genericKey);
                    }
                }
            }
            return this.parent.getTypeByGenerics(genericName);
        }
        return null;
    }

    /**
     * 根据泛型获取对应的PsiType
     *
     * @param ktTypeReference
     * @return
     */
    private KtTypeReference getKtTypeByGenerics(KtTypeReference ktTypeReference) {
        if (null == ktTypeReference) {
            return null;
        }
        if (this.parent != null) {
            return this.parent.getKtTypeByGenerics(ktTypeReference);
        }
        if (null != ktGenericsMap) {
            for (String psiTypeParameter : ktGenericsMap.keySet()) {
                if (psiTypeParameter.equals(ktTypeReference.getName())) {
                    return ktGenericsMap.get(psiTypeParameter);
                }
            }
        }
        return ktTypeReference;
    }

    public boolean containRequestBodyAnnotation() {
        if (CollectionUtils.isNotEmpty(this.annotations)) {
            for (PsiAnnotation annotation : annotations) {
                if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                    return true;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(this.ktAnnotationEntries)) {
            for (KtAnnotationEntry ktAnnotationEntry : ktAnnotationEntries) {
                if (ktAnnotationEntry.getText().contains(WebAnnotation.RequestBody)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containPathVariableAnnotation() {
        if (CollectionUtils.isNotEmpty(this.annotations)) {
            for (PsiAnnotation annotation : annotations) {
                if (annotation.getText().contains(WebAnnotation.PathVariable)) {
                    return true;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(this.ktAnnotationEntries)) {
            for (KtAnnotationEntry ktAnnotationEntry : ktAnnotationEntries) {
                if (ktAnnotationEntry.getText().contains(WebAnnotation.PathVariable)) {
                    return true;
                }
            }
        }
        return false;
    }

}
