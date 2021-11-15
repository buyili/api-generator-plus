package site.forgus.plugins.apigeneratorplus.bundles;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author lmx 2021/11/14 16:32
 **/

public class ApiGeneratorPlusBundle extends AbstractBundle {

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static final String BUNDLE = "messages.ApiGeneratorPlusBundle";
    private static final ApiGeneratorPlusBundle INSTANCE = new ApiGeneratorPlusBundle();
    protected ApiGeneratorPlusBundle() {
        super(BUNDLE);
    }
}
