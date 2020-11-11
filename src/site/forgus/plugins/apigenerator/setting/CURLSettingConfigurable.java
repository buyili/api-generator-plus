package site.forgus.plugins.apigenerator.setting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigenerator.curl.model.CURLModelInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

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
        AddEditDeleteListPanel helo = new AddEditDeleteListPanel<CURLModelInfo>("hello", oldState.modelInfoList) {
            @Nullable
            @Override
            protected CURLModelInfo findItemToAdd() {
                return null;
            }

            @Nullable
            @Override
            protected CURLModelInfo editSelectedItem(CURLModelInfo item) {
                return null;
            }

            @Override
            protected void addElement(@Nullable CURLModelInfo itemToAdd) {
                CURLModelInfo curlModelInfo = new CURLModelInfo();
                super.addElement(curlModelInfo);
            }

            @Override
            protected ListCellRenderer getListCellRenderer() {
                ListCellRenderer<CURLModelInfo> listCellRenderer = new ListCellRenderer<CURLModelInfo>() {
                    @Override
                    public Component getListCellRendererComponent(JList<? extends CURLModelInfo> list, CURLModelInfo value, int index, boolean isSelected, boolean cellHasFocus) {
                        return new JBLabel(value.getModuleName());
                    }
                };
                return listCellRenderer;
            }
        };
        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("ip address:"), ipTextField, 1, false)
                .addComponent(helo)
                .addComponentFillVertically(new JPanel(), 0)
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
