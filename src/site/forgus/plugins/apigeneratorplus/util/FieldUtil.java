package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import site.forgus.plugins.apigeneratorplus.curl.enums.ArrayFormatEnum;
import site.forgus.plugins.apigeneratorplus.setting.CURLSettingState;

import java.util.*;

public class FieldUtil {

    public static final Map<String, Object> normalTypes = new HashMap<>();

    public static final List<String> iterableTypes = Arrays.asList("List", "ArrayList", "Set", "Collection");
    /**
     * 泛型列表
     */
    public static final List<String> genericList = new ArrayList<>();


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
        normalTypes.put("LocalDateTime", "yyyy-MM-dd HH:mm:ss");
        normalTypes.put("BigInteger", 0);
        genericList.add("T");
        genericList.add("E");
        genericList.add("K");
        genericList.add("V");
    }

    public static Object getValue(PsiType psiType) {
        if (isIterableType(psiType)) {
            PsiType type = PsiUtil.extractIterableTypeParameter(psiType, false);
            if (type == null) {
                return "[]";
            }
            if (isNormalType(type)) {
                Object obj = normalTypes.get(type.getPresentableText());
                if (obj == null) {
                    return null;
                }
                return obj.toString() + "," + obj.toString();
            }
        }
        Object value = normalTypes.get(psiType.getPresentableText());
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
                String arrayFormat = StringUtils.isNotEmpty(state.arrayFormat) ? state.arrayFormat : CURLSettingState.ARRAY_FORMAT;
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


    public static boolean isNormalType(String typeName) {
        return normalTypes.containsKey(typeName);
    }

    private static boolean isIterableType(String typeName) {
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
}

