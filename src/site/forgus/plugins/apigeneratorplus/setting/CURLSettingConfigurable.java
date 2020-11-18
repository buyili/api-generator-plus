package site.forgus.plugins.apigeneratorplus.setting;

import com.google.gson.Gson;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModelInfo;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {

    CURLSettingState oldState;
    JBTextField ipTextField;
    CURLSettingListTableWithButtons curlSettingListTableWithButtons;

    private CURLModelInfo selectedInfo;
    JBTextField moduleNameTextField;
    JBTextField portTextField;

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

        MyOrderPanel myOrderPanel = new MyOrderPanel();
        myOrderPanel.addAll(oldState.modelInfoList);

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
                .addComponent(myOrderPanel)
                .getPanel();
        return jPanel;
    }

    @Override
    public boolean isModified() {
        List<CURLModelInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
        Gson gson = new Gson();
        if (!gson.toJson(oldState.modelInfoList).equals(gson.toJson(items))) {
            return true;
        }
        if (selectedInfo != null) {
            if (!selectedInfo.getModuleName().equals(moduleNameTextField.getText())
                    || !selectedInfo.getPort().equals(portTextField.getText())) {
                return true;
            }
        }
        return false;
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

        protected class ModuleNameColumnInfo extends ElementsColumnInfoBase<CURLModelInfo> {

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

        class PortColumnInfo extends ElementsColumnInfoBase<CURLModelInfo> {
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

    protected class MyOrderPanel extends OrderPanel<CURLModelInfo> {


        protected MyOrderPanel() {
            super(CURLModelInfo.class);
            JTable entryTable = getEntryTable();
            entryTable.setTableHeader(null);
            entryTable.setDefaultRenderer(CURLModelInfo.class, new ColoredTableCellRenderer() {
                @Override
                protected void customizeCellRenderer(JTable table, @Nullable Object value,
                                                     boolean selected,
                                                     boolean hasFocus, int row, int column) {
                    setBorder(null);
                    if (value != null) {
                        CURLModelInfo curlModelInfo = (CURLModelInfo) value;
                        append(curlModelInfo.getModuleName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    }
                }
            });
            entryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    int selectedRow = entryTable.getSelectedRow();
                    if (selectedRow != -1) {
                        selectedInfo = getValueAt(selectedRow);
                        moduleNameTextField.setText(selectedInfo.getModuleName());
                    }
                }
            });
            setCheckboxColumnName("");
            moduleNameTextField = new JBTextField();
            portTextField = new JBTextField();
            JPanel myDescriptionPanel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(new JBLabel("module name"), moduleNameTextField, 1, false)
                    .addLabeledComponent(new JBLabel("port"), portTextField, 1, false)
                    .getPanel();
//            myDescriptionPanel.setPreferredSize(new JBDimension(600, 400));
            removeAll();

            Splitter splitter = new OnePixelSplitter(false);
            splitter.setFirstComponent(wrapWithPane(entryTable, 1, 0));
            splitter.setSecondComponent(wrapWithPane(myDescriptionPanel, 0, 1));
            add(splitter, BorderLayout.CENTER);
        }

        @NotNull
        private JScrollPane wrapWithPane(@NotNull JComponent c, int left, int right) {
            JScrollPane pane = ScrollPaneFactory.createScrollPane(c);
            pane.setBorder(JBUI.Borders.customLine(OnePixelDivider.BACKGROUND, 1, left, 1, right));
            return pane;
        }

        @Override
        public String getCheckboxColumnName() {
            return "";
        }

        @Override
        public boolean isCheckable(CURLModelInfo entry) {
            return true;
        }

        @Override
        public boolean isChecked(CURLModelInfo entry) {
            return selectedInfo != null && selectedInfo.getModuleName().equals(entry.getModuleName());
        }

        @Override
        public void setChecked(CURLModelInfo entry, boolean checked) {
            System.out.println("--------");
        }

        public void addAll(List<CURLModelInfo> orderEntries) {
            for (CURLModelInfo orderEntry : orderEntries) {
                add(orderEntry);
            }
        }
    }


}
