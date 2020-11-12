package site.forgus.plugins.apigeneratorplus.util;

import java.util.Collection;

public class AssertUtils {

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    private static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
