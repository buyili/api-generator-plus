package site.forgus.plugins.apigeneratorplus.curl;

import site.forgus.plugins.apigeneratorplus.exception.BizException;

import javax.annotation.Nullable;

/**
 * @author lmx 2020/11/23 15:52
 */

public class Assert {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BizException(message);
        }
    }

    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    public static void isNull(@Nullable Object object, String message) {
        if (object != null) {
            throw new BizException(message);
        }
    }

    public static void isNull(@Nullable Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new BizException(message);
        }
    }

    public static void notNull(@Nullable Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

}
