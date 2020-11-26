package site.forgus.plugins.apigeneratorplus.curl.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lmx 2020/11/11 22:53
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CURLModuleInfo implements Cloneable {

    private String id = String.valueOf(System.nanoTime());

    private String moduleName = "";

    private String port = "";

    private String contextPath = "";

    private List<String[]> headers = Collections.emptyList();

    @Override
    public CURLModuleInfo clone() {
        try {
            return (CURLModuleInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getJsonHeaders() {
        Map<String, String> map = new HashMap<>();
        for (String[] header : headers) {
            map.put(header[0], header[1]);
        }
        return JsonUtil.gson.toJson(map);
    }

    public Map<String, String> getHeadersAsMap() {
        Map<String, String> map = new HashMap<>();
        for (String[] header : headers) {
            map.put(header[0], header[1]);
        }
        return map;
    }

}
