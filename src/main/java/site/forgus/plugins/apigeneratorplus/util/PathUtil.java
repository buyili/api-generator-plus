package site.forgus.plugins.apigeneratorplus.util;

import org.apache.commons.lang.StringUtils;

/**
 * @author lmx 2020/12/22 16:52
 */

public class PathUtil {

    public static String pathResolve(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            if (StringUtils.isNotBlank(arg)) {
                if (!arg.startsWith("/")) {
                    stringBuilder.append('/');
                }
                if (arg.endsWith("/")) {
                    stringBuilder.append(arg.substring(0, arg.length() - 1));
                } else {
                    stringBuilder.append(arg);
                }
            }
        }
        return stringBuilder.toString();
    }

}
