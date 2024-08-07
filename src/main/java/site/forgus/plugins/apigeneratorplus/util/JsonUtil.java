package site.forgus.plugins.apigeneratorplus.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import site.forgus.plugins.apigeneratorplus.util.StringUtils;
import site.forgus.plugins.apigeneratorplus.constant.TypeEnum;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;

import java.lang.reflect.Modifier;
import java.util.*;

public class JsonUtil {

    public static final Gson prettyJson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.FINAL)
            .disableHtmlEscaping()
            .setPrettyPrinting().create();


    /**
     * 去掉字段名的双引号
     * @param children
     * @return
     */
    public static String buildPrettyJsonWithoutQuotes(List<FieldInfo> children) {
        String jsonVal = prettyJson.toJson(getStringObjectMap(children));
        return jsonVal.replaceAll("\"(\\w+)\"(\\s*:\\s*)", "$1$2");
    }

    /**
     * 去掉字段名的双引号
     * @param fieldInfo
     * @return
     */
    public static String buildPrettyJsonWithoutQuotes(FieldInfo fieldInfo) {
        String jsonVal = buildPrettyJson(fieldInfo);
        return jsonVal.replaceAll("\"(\\w+)\"(\\s*:\\s*)", "$1$2");
    }

    public static String buildPrettyJson(List<FieldInfo> children) {
        return prettyJson.toJson(getStringObjectMap(children));
    }

    public static String buildPrettyJson(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return "";
        }
        if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
            return prettyJson.toJson(FieldUtil.getValue(fieldInfo));
        }
        Map<String, Object> stringObjectMap = getStringObjectMap(fieldInfo.getChildren());
        if (TypeEnum.ARRAY.equals(fieldInfo.getParamType())) {
            return prettyJson.toJson(Collections.singletonList(stringObjectMap));
        }
        return prettyJson.toJson(stringObjectMap);
    }

    public static String buildRawJson(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return "";
        }
        Gson gson = new Gson();
        if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
            return FieldUtil.getValue(fieldInfo).toString();
        }
        Map<String, Object> stringObjectMap = getStringObjectMap(fieldInfo.getChildren());
        if (TypeEnum.ARRAY.equals(fieldInfo.getParamType())) {
            return gson.toJson(Collections.singletonList(stringObjectMap));
        }
        return gson.toJson(stringObjectMap);
    }

    private static String buildJson5(String prettyJson, List<String> fieldDesc) {
        if (!prettyJson.contains("{")) {
            if (CollectionUtils.isNotEmpty(fieldDesc)) {
                return prettyJson + "//" + fieldDesc.get(0);
            }
            return prettyJson;
        }
        String[] split = prettyJson.split("\n");
        StringBuffer json5 = new StringBuffer();
        int index = 0;
        for (String str : split) {
            String temp = str;
            if (str.contains(":")) {
                index++;
                String desc = fieldDesc.get(index - 1);
                if (!AssertUtils.isEmpty(desc)) {
                    temp = str + "//" + desc;
                }
            }
            json5.append(temp);
            json5.append("\n");
        }
        return json5.toString();
    }

    public static String buildJson5(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return "";
        }
        return buildJson5(buildPrettyJson(fieldInfo), buildFieldDescList(fieldInfo));
    }

    private static List<String> buildFieldDescList(List<FieldInfo> children) {
        List<String> descList = new ArrayList<>();
        if (children == null) {
            return descList;
        }
        for (FieldInfo fieldInfo : children) {
            descList.add(buildDesc(fieldInfo));
            if (!TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
                descList.addAll(buildFieldDescList(fieldInfo.getChildren()));
            }
        }
        return descList;
    }

    private static List<String> buildFieldDescList(FieldInfo fieldInfo) {
        List<String> descList = new ArrayList<>();
        if (fieldInfo == null) {
            return descList;
        }
        if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
            if (StringUtils.isEmpty(fieldInfo.getDesc())) {
                return descList;
            }
            descList.add(buildDesc(fieldInfo));
        } else {
            descList.addAll(buildFieldDescList(fieldInfo.getChildren()));
        }
        return descList;
    }

    private static String buildDesc(FieldInfo fieldInfo) {
        String desc = fieldInfo.getDesc();
        if (!fieldInfo.isRequire()) {
            return desc;
        }
        if (AssertUtils.isEmpty(desc)) {
            return "必填";
        }
        return desc + ",必填";
    }

    private static Map<String, Object> getStringObjectMap(List<FieldInfo> fieldInfos) {
        Map<String, Object> map = new LinkedHashMap<>(64);
        if (fieldInfos == null) {
            return map;
        }
        for (FieldInfo fieldInfo : fieldInfos) {
            buildJsonValue(map, fieldInfo);
        }
        return map;
    }

    private static void buildJsonValue(Map<String, Object> map, FieldInfo fieldInfo) {
        if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
            map.put(fieldInfo.getName(), FieldUtil.getValue(fieldInfo));
            return;
        }
        if (TypeEnum.ARRAY.equals(fieldInfo.getParamType())) {
            if (AssertUtils.isNotEmpty(fieldInfo.getChildren())) {
                map.put(fieldInfo.getName(), Collections.singletonList(getStringObjectMap(fieldInfo.getChildren())));
                return;
            }
//            PsiClass psiClass = PsiUtil.resolveClassInType(fieldInfo.getPsiType());
//            String innerType = PsiUtil.substituteTypeParameter(fieldInfo.getPsiType(), psiClass, 0, true).getPresentableText();
            String innerType = fieldInfo.getIterableTypeStr();
            map.put(fieldInfo.getName(), Collections.singletonList(FieldUtil.normalTypes.get(innerType) == null ? new HashMap<>() : FieldUtil.normalTypes.get(innerType)));
            return;
        }
        if (CollectionUtils.isEmpty(fieldInfo.getChildren())) {
            map.put(fieldInfo.getName(), new HashMap<>());
            return;
        }
        if (fieldInfo.getParent() == null) {
            map.putAll(getStringObjectMap(fieldInfo.getChildren()));
        } else {
            map.put(fieldInfo.getName(), getStringObjectMap(fieldInfo.getChildren()));
        }
        //for (FieldInfo info : fieldInfo.getChildren()) {
        //    if (!info.getName().equals(fieldInfo.getName())) {
        //        map.put(fieldInfo.getName(), getStringObjectMap(fieldInfo.getChildren()));
        //    }
        //}
    }

}
