package site.forgus.plugins.apigenerator.setting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {

    CURLSettingState oldState;
    JBTextField ipTextField;

    public CURLSettingConfigurable(Project project) {
        oldState = ServiceManager.getService(project, CURLSettingState.class);
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Copy as cURL";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        ipTextField = new JBTextField();
        ipTextField.setText(oldState.ip);
        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("ip address:"), ipTextField)
                .getPanel();
        return jPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
