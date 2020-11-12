package site.forgus.plugins.apigenerator.util;

/**
 * @author lmx 2020/11/12 17:05
 */

public class StringUtil {

    private static int nameNumber = 0;

    public static String getName() {
        nameNumber = nameNumber + 1;
        return "Unnameed (" + nameNumber + ")";
    }

}
