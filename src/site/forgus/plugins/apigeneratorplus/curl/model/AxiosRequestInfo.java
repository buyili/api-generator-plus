package site.forgus.plugins.apigeneratorplus.curl.model;

import lombok.Data;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;

import java.util.Map;

/**
 * @author lmx 2021/6/2 15:52
 */
@Data
public class AxiosRequestInfo {

    private String method;

    private String url;

    private String params;

    private String data;

    private Map<String, String> headers;

    //public AxiosRequestInfo(MethodInfo methodInfo) {
    //    this.method = methodInfo.getMethodName();
    //    this.url = methodInfo.getMethodPath();
    //
    //}

    public String toPrettyString(){
        String rawHeaders = JsonUtil.prettyJson.toJson(headers);
        return "axios({\n" +
                "method: \"" + method + "\",\n" +
                "url: \"" + url + "\",\n" +
                "params: " + params + ",\n" +
                "data: \"" + data + "\",\n" +
                "headers: " + rawHeaders + ",\n" +
                "})"
                ;
    }

}
