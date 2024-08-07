package site.forgus.plugins.apigeneratorplus.curl.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lmx 2020/11/11 22:53
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CURLModuleInfo implements Cloneable, Serializable {

    private static final long serialVersionUID = -8561360758136970403L;

    private String id = String.valueOf(System.nanoTime());

    private String moduleName = "";

    private String port = "";

    private String contextPath = "";

    //@Deprecated
    //private List<String[]> headers = Collections.emptyList();

    /**
     * @since 1.0.2
     */
    private List<Header> requestHeaders = new ArrayList<>();


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
        for (Header header : requestHeaders) {
            map.put(header.getKey(), header.getValue());
        }
        return JsonUtil.prettyJson.toJson(map);
    }

    public Map<String, String> getHeadersAsMap() {
        Map<String, String> map = new HashMap<>(requestHeaders.size());
        for (Header header : requestHeaders) {
            map.put(header.getKey(), header.getValue());
        }
        return map;
    }

}
