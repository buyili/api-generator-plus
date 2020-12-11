package site.forgus.plugins.apigeneratorplus.curl.model;

import lombok.Data;
import lombok.experimental.Accessors;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;

import java.util.Map;

/**
 * @author lmx 2020/11/25 10:08
 */
@Data
@Accessors(chain = true)
public class FetchRequestInfo {

    private String input;

    private InitOptions initOptions;

    public String toPrettyString() {
        return "fetch('" + input + "'," + initOptions.toPrettyString() +
                ')';
    }

    @Data
    @Accessors(chain = true)
    public static class InitOptions {

        private String method;

        private Map<String, String> headers;

        private String body;

        /**
         * omit、same-origin 或者 include。为了在当前域名内自动发送 cookie ， 必须提供这个选项，
         * 从 Chrome 50 开始， 这个属性也可以接受 FederatedCredential 实例或是一个 PasswordCredential 实例。
         */
        private String credentials;

        /**
         * 请求的 cache 模式: default、 no-store、 reload 、 no-cache 、 force-cache 或者 only-if-cached 。
         */
        private String cache;

        /**
         * 可用的 redirect 模式: follow (自动重定向), error (如果产生重定向将自动终止并且抛出一个错误）, 或者 manual (手动处理重定向).
         * 在Chrome中默认使用follow（Chrome 47之前的默认值是manual）。
         */
        private String redirect;

        /**
         * 一个 USVString 可以是 no-referrer、client或一个 URL。默认是 client。
         */
        private String referrer;

        /**
         * 指定了HTTP头部referer字段的值。可能为以下值之一： no-referrer、 no-referrer-when-downgrade、 origin、
         * origin-when-cross-origin、 unsafe-url 。
         */
        private String referrerPolicy;

        /**
         * 包括请求的  subresource integrity 值 （ 例如： sha256-BpfBw7ivV8q2jLiT13fxDYAe2tJllusRSZ273h2nFSE=）。
         */
        private String integrity;

        public String toPrettyString() {
            return JsonUtil.prettyJson.toJson(this);
        }
    }

}
