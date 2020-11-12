package site.forgus.plugins.apigenerator.setting;

import com.google.gson.Gson;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigenerator.curl.model.CURLModelInfo;
import site.forgus.plugins.apigenerator.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {

    CURLSettingState oldState;
    JBTextField ipTextField;
    CURLSettingListTableWithButtons curlSettingListTableWithButtons;

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

        curlSettingListTableWithButtons = new CURLSettingListTableWithButtons();
        curlSettingListTableWithButtons.setValues(oldState.modelInfoList);

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
                .addComponent(curlSettingListTableWithButtons.getComponent())
                .addComponent(helo)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        return jPanel;
    }

    @Override
    public boolean isModified() {
        List<CURLModelInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
        Gson gson = new Gson();
        return !gson.toJson(oldState.modelInfoList).equals(gson.toJson(items));
    }

    @Override
    public void apply() throws ConfigurationException {
        List<CURLModelInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
        oldState.modelInfoList = items;

    }

    protected class CURLSettingListTableWithButtons extends ListTableWithButtons<CURLModelInfo> {

        @Override
        protected ListTableModel createListModel() {
            return new ListTableModel(new ModuleNameColumnInfo(), new PortColumnInfo());
        }

        @Override
        protected CURLModelInfo createElement() {
            CURLModelInfo curlModelInfo = new CURLModelInfo();
            curlModelInfo.setModuleName(StringUtil.getName());
            return curlModelInfo;
        }

        @Override
        protected boolean isEmpty(CURLModelInfo element) {
            return false;
        }

        @Override
        protected CURLModelInfo cloneElement(CURLModelInfo variable) {
            return variable.clone();
        }

        @Override
        protected boolean canDeleteElement(CURLModelInfo selection) {
            return true;
        }

        protected class ModuleNameColumnInfo extends ElementsColumnInfoBase<CURLModelInfo>{

            protected ModuleNameColumnInfo() {
                super("Module Name");
            }

            @Nullable
            @Override
            protected String getDescription(CURLModelInfo element) {
                return "Module Name";
            }

            @Nullable
            @Override
            public String valueOf(CURLModelInfo curlModelInfo) {
                return curlModelInfo.getModuleName();
            }

            @Override
            public boolean isCellEditable(CURLModelInfo curlModelInfo) {
                return true;
            }

            @Override
            public void setValue(CURLModelInfo curlModelInfo, String value) {
                curlModelInfo.setModuleName(value);
            }
        }

        class PortColumnInfo extends ElementsColumnInfoBase<CURLModelInfo>{
            public PortColumnInfo() {
                super("Port");
            }

            @Nullable
            @Override
            protected String getDescription(CURLModelInfo element) {
                return "Port";
            }

            @Nullable
            @Override
            public String valueOf(CURLModelInfo curlModelInfo) {
                return curlModelInfo.getPort();
            }

            @Override
            public boolean isCellEditable(CURLModelInfo curlModelInfo) {
                return true;
            }

            @Override
            public void setValue(CURLModelInfo curlModelInfo, String value) {
                curlModelInfo.setPort(value);
            }
        }
    }


}
