package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author lmx 2020/11/11 17:44
 */

public class GroupSettingConfigurable implements Configurable {
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Api Generator Plus";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
