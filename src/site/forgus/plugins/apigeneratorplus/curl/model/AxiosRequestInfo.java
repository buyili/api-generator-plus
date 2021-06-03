package site.forgus.plugins.apigeneratorplus.curl.model;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;

import java.util.Map;

/**
 * @author lmx 2021/6/2 15:52
 */
@Data
public class AxiosRequestInfo {

    private String formDataVal;

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
        String raw = "axios({\n" +
                "method: \"" + method + "\",\n";
        raw += "url: \"" + url + "\",\n";
        if(StringUtils.isNotBlank(params)){
            if(params.startsWith("{") && params.endsWith("}")){
                raw += "params: " + params + ",\n";
            }else {
                raw += "params: \"" + params + "\",\n";
            }
        }
        if(StringUtils.isNotBlank(data)){
            if(data.startsWith("{") && data.endsWith("}")){
                raw += "data: " + data + ",\n";
            }else {
                raw += "data: \"" + data + "\",\n";
            }
        }
        return raw +
                "headers: " + rawHeaders + ",\n" +
                "})"
                ;
    }

    public String toPrettyStringForFormData() {
        this.setData("$body");
        String axiosString = this.toPrettyString();
        axiosString = axiosString.replace("\"$body\"", "formData");
        return formDataVal +
                axiosString
                ;
    }

}
