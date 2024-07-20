package site.forgus.plugins.apigeneratorplus.http;


import java.util.HashMap;
import java.util.Map;

/**
 * @author lmx 2020/12/11 13:46
 */
public enum MediaType {

    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),

    APPLICATION_JSON("application/json"),

    @Deprecated
    APPLICATION_JSON_UTF8("application/json;charset=UTF-8"),

    MULTIPART_FORM_DATA("multipart/form-data"),

    ;


    private String value;

    MediaType(String value) {
        this.value = value;
    }

    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

    public static final String APPLICATION_JSON_VALUE = "application/json";

    @Deprecated
    public static final String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";

    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    public Map<String, String> getHeader() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Content-Type", this.value);
        return hashMap;
    }

}
