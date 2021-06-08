package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtTypeReference;
import site.forgus.plugins.apigeneratorplus.config.ApiGeneratorConfig;
import site.forgus.plugins.apigeneratorplus.constant.TypeEnum;
import site.forgus.plugins.apigeneratorplus.curl.enums.ArrayFormatEnum;
import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class FieldUtil {

    public static final Map<String, Object> normalTypes = new HashMap<>();

    public static final List<String> iterableTypes = Arrays.asList("List", "ArrayList", "Set", "Collection");
    /**
     * 泛型列表
     */
    public static final List<String> genericList = new ArrayList<>();

    public static final List<String> fileList = Arrays.asList("MultipartFile", "CommonsMultipartFile", "MockMultipartFile",
            "StandardMultipartFile");

    public static final List<String> mapTypeList = Arrays.asList("Map", "HashMap", "LinkedHashMap", "JSONObject");

    private static List<String> excludeParamTypes = Arrays.asList("RedirectAttributes", "HttpServletRequest", "HttpServletResponse");


    static {
        normalTypes.put("int", 1);
        normalTypes.put("boolean", false);
        normalTypes.put("byte", 1);
        normalTypes.put("short", 1);
        normalTypes.put("long", 1L);
        normalTypes.put("float", 1.0F);
        normalTypes.put("double", 1.0D);
        normalTypes.put("char", 'a');
        normalTypes.put("Boolean", false);
        normalTypes.put("Byte", 0);
        normalTypes.put("Short", Short.valueOf((short) 0));
        normalTypes.put("Integer", 0);
        normalTypes.put("Long", 0L);
        normalTypes.put("Float", 0.0F);
        normalTypes.put("Double", 0.0D);
        normalTypes.put("String", "@string");
        normalTypes.put("Date", new Date().getTime());
        normalTypes.put("BigDecimal", 0.111111);
        normalTypes.put("LocalTime", LocalTime.now().getLong(ChronoField.MILLI_OF_DAY));
        normalTypes.put("LocalDate", System.currentTimeMillis());
        normalTypes.put("LocalDateTime", System.currentTimeMillis());
        normalTypes.put("BigInteger", 0);
        normalTypes.put("MultipartFile", "@/path");
        normalTypes.put("CommonsMultipartFile", "@/path");
        normalTypes.put("MockMultipartFile", "@/path");
        normalTypes.put("StandardMultipartFile", "@/path");
        genericList.add("T");
        genericList.add("E");
        genericList.add("K");
        genericList.add("V");
    }

//    public static Object getValue(PsiType psiType) {
//        if (isIterableType(psiType)) {
//            PsiType type = PsiUtil.extractIterableTypeParameter(psiType, false);
//            if (type == null) {
//                return "[]";
//            }
//            if (isNormalType(type)) {
//                Object obj = normalTypes.get(type.getPresentableText());
//                if (obj == null) {
//                    return null;
//                }
//                return obj.toString() + "," + obj.toString();
//            }
//        }
//        Object value = normalTypes.get(psiType.getPresentableText());
//        return value == null ? "" : value;
//    }

    public static Object getValue(FieldInfo fieldInfo) {
        if (TypeEnum.ARRAY == fieldInfo.getParamType()) {
            if (isNormalType(fieldInfo.getIterableTypeStr())) {
                Object obj = normalTypes.get(fieldInfo.getIterableTypeStr());
                if (obj == null) {
                    return null;
                }
                return obj.toString() + "," + obj.toString();
            }
        }
        ApiGeneratorConfig config = GlobalVariable.getApiConfig();
        if ("Date".equals(fieldInfo.getTypeText())) {
            if (StringUtils.isNotBlank(config.dateFormat)) {
                return "@datetime('" + config.dateFormat + "')";
            }
        }
        if ("LocalDateTime".equals(fieldInfo.getTypeText())) {
            if (StringUtils.isNotBlank(config.localDateTimeFormat)) {
                return "@datetime('" + config.localDateTimeFormat + "')";
            }
        }
        if ("LocalDate".equals(fieldInfo.getTypeText())) {
            if (StringUtils.isNotBlank(config.localDateFormat)) {
                return "@date('" + config.localDateFormat + "')";
            }
        }
        if ("LocalTime".equals(fieldInfo.getTypeText())) {
            if (StringUtils.isNotBlank(config.localTimeFormat)) {
                return "@time('" + config.localTimeFormat + "')";
            }
        }
        Object value = normalTypes.get(fieldInfo.getTypeText());
        return value == null ? "" : value;
    }

    /**
     * copy as curl时，上传格式为application/x-www-form-urlencoded。
     *
     * @param psiType
     * @return
     */
    public static String getValueForCurl(String keyName, PsiType psiType, CURLSettingState state) {
        if (isIterableType(psiType)) {
            PsiType type = PsiUtil.extractIterableTypeParameter(psiType, false);
            if (type == null) {
                return keyName + "=[]";
            }
            if (isNormalType(type)) {
                Object obj = normalTypes.get(type.getPresentableText());
                if (obj == null) {
                    return null;
                }
                String arrayFormat = StringUtils.isNotEmpty(state.arrayFormat) ? state.arrayFormat : ArrayFormatEnum.repeat.name();
                if (ArrayFormatEnum.indices.name().equals(arrayFormat)) {
                    return keyName + "[0]=" + obj.toString() + "&" + keyName + "[1]=" + obj.toString();
                } else if (ArrayFormatEnum.brackets.name().equals(arrayFormat)) {
                    return keyName + "[]=" + obj.toString() + "&" + keyName + "[]=" + obj.toString();
                } else if (ArrayFormatEnum.repeat.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "&" + keyName + "=" + obj.toString();
                } else if (ArrayFormatEnum.comma.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "," + obj.toString();
                }
            }
        }
        Object value = normalTypes.get(psiType.getPresentableText());
        return value == null ? "" : keyName + "=" + value.toString();
    }

    /**
     * copy as curl时，上传格式为application/x-www-form-urlencoded。
     *
     * @param fieldInfo
     * @return
     */
    public static String getValueForCurl(FieldInfo fieldInfo, CURLSettingState state) {
        String keyName = fieldInfo.getName();
        if (TypeEnum.ARRAY == fieldInfo.getParamType()) {
            if (isNormalType(fieldInfo.getIterableTypeStr())) {
                Object obj = normalTypes.get(fieldInfo.getIterableTypeStr());
                if (obj == null) {
                    return null;
                }
                String arrayFormat = StringUtils.isNotEmpty(state.arrayFormat) ? state.arrayFormat : ArrayFormatEnum.repeat.name();
                if (ArrayFormatEnum.indices.name().equals(arrayFormat)) {
                    return keyName + "[0]=" + obj.toString() + "&" + keyName + "[1]=" + obj.toString();
                } else if (ArrayFormatEnum.brackets.name().equals(arrayFormat)) {
                    return keyName + "[]=" + obj.toString() + "&" + keyName + "[]=" + obj.toString();
                } else if (ArrayFormatEnum.repeat.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "&" + keyName + "=" + obj.toString();
                } else if (ArrayFormatEnum.comma.name().equals(arrayFormat)) {
                    return keyName + "=" + obj.toString() + "," + obj.toString();
                }
            }
        }
        Object value = normalTypes.get(fieldInfo.getTypeText());
        return value == null ? "" : keyName + "=" + value.toString();
    }


    public static boolean isNormalType(String typeName) {
        return normalTypes.containsKey(typeName);
    }

    public static boolean isFileType(String typeName) {
        return fileList.contains(typeName);
    }

    public static boolean isIterableType(String typeName) {
        if (iterableTypes.contains(typeName)) {
            return true;
        }
        for (String iterableType : iterableTypes) {
            if (typeName.startsWith(iterableType + "<")) {
                return true;
            }
        }
        return false;
//        return typeName.startsWith("List<") || typeName.startsWith("Set<") || typeName.startsWith("Collection<");
    }

    public static boolean isIterableType(PsiType psiType) {
        return isIterableType(psiType.getPresentableText());
    }

    public static boolean isNormalType(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        if (psiClass != null) {
            if (psiClass.isEnum()) {
                return true;
            }
        }
        return isNormalType(psiType.getPresentableText());
    }

    public static boolean isGenericType(String typeName) {
        return genericList.contains(typeName);
    }

    public static boolean isMapType(String typeText) {
        for (String mapType : mapTypeList) {
            if (mapType.equals(typeText) || typeText.startsWith(mapType.concat("<"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMapType(PsiType psiType) {
        return isMapType(psiType.getPresentableText());
    }

    public static boolean isMapType(KtTypeReference ktTypeReference) {
        return isMapType(KtUtil.getText(ktTypeReference));
    }

    public static PsiAnnotation findAnnotationByName(List<PsiAnnotation> annotations, String text) {
        if (annotations == null) {
            return null;
        }
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(text)) {
                return annotation;
            }
        }
        return null;
    }

    public static KtAnnotationEntry findKtAnnotationByName(List<KtAnnotationEntry> annotations, String text) {
        if (annotations == null) {
            return null;
        }
        for (KtAnnotationEntry annotation : annotations) {
            if (annotation.getText().contains(text)) {
                return annotation;
            }
        }
        return null;
    }

    public static List<FieldInfo> filterChildrenFiled(List<FieldInfo> items, FilterFieldInfo filterFieldInfo) {
        List<String> canonicalClassNameList = filterFieldInfo.getCanonicalClassNameList();
        List<String> includeFiledList = filterFieldInfo.getIncludeFiledList();
        List<String> excludeFiledList = filterFieldInfo.getExcludeFiledList();
        for (FieldInfo item : items) {
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
        return items;
    }

    public static int getIndexOnCanonicalClassNameList(String canonicalClassName, List<String> set) {
        for (String s : set) {
            if (canonicalClassName.startsWith(s)) {
                return set.indexOf(s);
            }
        }
        return -1;
    }

    public static List<PsiParameter> filterParameters(PsiParameter[] psiParameters) {
        List<PsiParameter> psiParameterList = new ArrayList<>();
        ApiGeneratorConfig apiConfig = GlobalVariable.getApiConfig();

        for (PsiParameter psiParameter : psiParameters) {
            PsiType psiType = psiParameter.getType();
            if (excludeParamTypes.contains(psiType.getPresentableText())) {
                continue;
            }
            boolean ignore = false;
            for (String annotationName : StringUtil.string2Set(apiConfig.excludeAnnotations)) {
                if (psiParameter.getText().contains(annotationName)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                continue;
            }
            psiParameterList.add(psiParameter);
        }
        return psiParameterList;
    }
}

