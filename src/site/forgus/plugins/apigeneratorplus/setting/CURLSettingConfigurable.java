package site.forgus.plugins.apigeneratorplus.setting;

import com.google.gson.Gson;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
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
import java.util.List;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {

    CURLSettingState oldState;
    JBTextField baseApiTextField;
    CURLSettingListTableWithButtons curlSettingListTableWithButtons;

    private CURLModelInfo selectedInfo;
    MyOrderPanel myOrderPanel;
    JBTextField moduleNameTextField;
    JBTextField portTextField;
    JBTextField canonicalClassNameTextFields;
    JBTextField includeFiledTextFields;
    JBTextField excludeFieldTextFields;
    JBTextField arrayFormatTextFields;
    JBCheckBox excludeChildrenCheckBox;
    MyHeaderListTableWithButton myHeaderListTableWithButton;

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
        baseApiTextField = new JBTextField();
        baseApiTextField.setText(oldState.baseApi);

        canonicalClassNameTextFields = new JBTextField(oldState.filterFieldInfo.canonicalClassName);
        includeFiledTextFields = new JBTextField(oldState.filterFieldInfo.includeFiled);
        excludeFieldTextFields = new JBTextField(oldState.filterFieldInfo.excludeField);
        arrayFormatTextFields = new JBTextField(oldState.arrayFormat);
        excludeChildrenCheckBox = new JBCheckBox("", oldState.filterFieldInfo.excludeChildren);

//        curlSettingListTableWithButtons = new CURLSettingListTableWithButtons();
//        curlSettingListTableWithButtons.setValues(oldState.modelInfoList);

        myOrderPanel = new MyOrderPanel();
        myOrderPanel.addAll(oldState.modelInfoList);

        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Base Api:"), baseApiTextField, 1, false)
//                .addComponent(curlSettingListTableWithButtons.getComponent())
                .addLabeledComponent(new JBLabel("Canonical Class Name:"), canonicalClassNameTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Include Fields:"), includeFiledTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Exclude Fields:"), excludeFieldTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Array Format:"), arrayFormatTextFields, 1, false)
                .addTooltip("indices    // 'a[0]=b&a[1]=c'      brackets    // 'a[]=b&a[]=c'        repeat  // 'a=b&a=c'        comma   // 'a=b,c'")
                .addLabeledComponent(new JBLabel("Exclude Children Field"), excludeChildrenCheckBox)
                .addVerticalGap(4)
                .addComponentFillVertically(myOrderPanel, 0)
                .getPanel();
        return jPanel;
    }

    @Override
    public boolean isModified() {
        Gson gson = new Gson();
//        List<CURLModelInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
//        if (!gson.toJson(oldState.modelInfoList).equals(gson.toJson(items))) {
//            return true;
//        }
        if (!oldState.baseApi.equals(baseApiTextField.getText())
                || !oldState.filterFieldInfo.canonicalClassName.equals(canonicalClassNameTextFields.getText())
                || !oldState.filterFieldInfo.includeFiled.equals(includeFiledTextFields.getText())
                || !oldState.filterFieldInfo.excludeField.equals(excludeFieldTextFields.getText())
                || !oldState.arrayFormat.equals(arrayFormatTextFields.getText())
                || oldState.filterFieldInfo.excludeChildren != excludeChildrenCheckBox.isSelected()
        ) {
            return true;
        }

        if (selectedInfo != null) {
            List<CURLModelInfo> entries = myOrderPanel.getEntries();
            for (CURLModelInfo entry : entries) {
                if (entry.getId().equals(selectedInfo.getId())) {
                    int i = entries.indexOf(entry);
                    selectedInfo.setModuleName(moduleNameTextField.getText());
                    selectedInfo.setPort(portTextField.getText());
                    List<String[]> items = myHeaderListTableWithButton.getTableView().getItems();
                    selectedInfo.setHeaders(items);
                    myOrderPanel.getEntryTable().getModel().setValueAt(selectedInfo, i, myOrderPanel.getEntryColumn());
                    break;
                }
            }
        }
        List<CURLModelInfo> entries = myOrderPanel.getEntries();
        List<CURLModelInfo> modelInfoList = oldState.modelInfoList;
        if (!gson.toJson(modelInfoList).equals(gson.toJson(entries))) {
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
//        List<CURLModelInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
//        oldState.modelInfoList = items;
        oldState.baseApi = baseApiTextField.getText();
        oldState.filterFieldInfo.canonicalClassName = canonicalClassNameTextFields.getText();
        oldState.filterFieldInfo.includeFiled = includeFiledTextFields.getText();
        oldState.filterFieldInfo.excludeField = excludeFieldTextFields.getText();
        oldState.arrayFormat = arrayFormatTextFields.getText();
        oldState.filterFieldInfo.excludeChildren = excludeChildrenCheckBox.isSelected();

        oldState.modelInfoList = myOrderPanel.getEntries();
    }

    @Override
    public void reset() {
        baseApiTextField.setText(oldState.baseApi);
        canonicalClassNameTextFields.setText(oldState.filterFieldInfo.canonicalClassName);
        includeFiledTextFields.setText(oldState.filterFieldInfo.includeFiled);
        excludeFieldTextFields.setText(oldState.filterFieldInfo.excludeField);
        arrayFormatTextFields.setText(oldState.arrayFormat);

        myOrderPanel.clear();
        myOrderPanel.addAll(oldState.modelInfoList);
    }

    protected static class CURLSettingListTableWithButtons extends ListTableWithButtons<CURLModelInfo> {
        public CURLSettingListTableWithButtons() {
            getTableView().getEmptyText().setText(ExecutionBundle.message("empty.text.no.variables"));
        }

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


        private final ToolbarDecorator myDecorator;
        private final CommonActionsPanel myActionsPanel;
        JPanel myDescriptionPanel;

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
                        portTextField.setText(selectedInfo.getPort());
                        myHeaderListTableWithButton.setValues(selectedInfo.getHeaders());
                        myDescriptionPanel.setVisible(true);
                        myDescriptionPanel.updateUI();
                    }
                }
            });
            setCheckboxColumnName("");
            moduleNameTextField = new JBTextField();
            portTextField = new JBTextField();
            myHeaderListTableWithButton = new MyHeaderListTableWithButton();

            myDescriptionPanel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(new JBLabel("Module Name"), moduleNameTextField, 1, false)
                    .addLabeledComponent(new JBLabel("Port"), portTextField, 1, false)
                    .addLabeledComponent(new JBLabel("Headers"), myHeaderListTableWithButton.getComponent(), 1, true)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();
            myDescriptionPanel.setVisible(false);
//            myDescriptionPanel.setPreferredSize(new JBDimension(600, 400));
            removeAll();

            myDecorator = createToolbarDecorator();
            myDecorator.setAddAction(createAddAction()).setRemoveAction(createRemoveAction());
            myActionsPanel = myDecorator.getActionsPanel();

            Splitter splitter = new OnePixelSplitter(false);
            splitter.setFirstComponent(wrapWithPane(myDecorator.createPanel(), 1, 0));
            splitter.setSecondComponent(wrapWithPane(myDescriptionPanel, 0, 1));
            add(splitter, BorderLayout.CENTER);
        }


        protected ToolbarDecorator createToolbarDecorator() {
            return ToolbarDecorator.createDecorator(getEntryTable());
        }

        @Nullable
        protected AnActionButtonRunnable createRemoveAction() {
            return button -> removeSelected();
        }

        @Nullable
        protected AnActionButtonRunnable createAddAction() {
            return button -> addNewElement(createElement());
        }

        protected CURLModelInfo createElement() {
            CURLModelInfo curlModelInfo = new CURLModelInfo();
            curlModelInfo.setId(String.valueOf(System.currentTimeMillis()));
            curlModelInfo.setModuleName(StringUtil.getName());
            return curlModelInfo;
        }

        protected void addNewElement(CURLModelInfo newElement) {
            add(newElement);
            int index = getEntries().size() - 1;
            getEntryTable().setRowSelectionInterval(index, index);
        }

        protected void removeSelected() {
            JTable entryTable = getEntryTable();
            int[] selectedRows = entryTable.getSelectedRows();
            if (selectedRows.length == 0) {
                return;
            }
            int selectedIndex = entryTable.getSelectedRow();
            List<CURLModelInfo> entries = getEntries();
            CURLModelInfo selectRemoveInfo = entries.get(selectedIndex);
            remove(selectRemoveInfo);
//            removeItem(selectRemoveInfo);

            entries = getEntries();
            int pre = selectedIndex - 1;
            if (pre >= 0) {
                entryTable.setRowSelectionInterval(pre, pre);
            } else if (selectedIndex < entries.size()) {
                entryTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                myDescriptionPanel.setVisible(false);
//                myDescriptionPanel.removeAll();
                myDescriptionPanel.updateUI();
            }
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
        protected int getEntryColumn() {
            return super.getEntryColumn();
        }

        @Override
        public void setChecked(CURLModelInfo entry, boolean checked) {
            System.out.println("--------");
        }

        public void addAll(List<CURLModelInfo> orderEntries) {
            for (CURLModelInfo orderEntry : orderEntries) {
                add(orderEntry.clone());
            }
        }
    }

    protected class MyHeaderListTableWithButton extends ListTableWithButtons<String[]> {

        @Override
        protected ListTableModel createListModel() {
            return new ListTableModel(new KeyColumnInfo(), new ValueColumnInfo());
        }

        @Override
        protected String[] createElement() {
            return new String[2];
        }

        @Override
        protected boolean isEmpty(String[] element) {
            return element[0] == null || "".equals(element[0]);
        }

        @Override
        protected String[] cloneElement(String[] variable) {
            return variable.clone();
        }

        @Override
        protected boolean canDeleteElement(String[] selection) {
            return true;
        }


        protected class KeyColumnInfo extends ElementsColumnInfoBase<String[]> {

            protected KeyColumnInfo() {
                super("KEY");
            }

            @Nullable
            @Override
            protected String getDescription(String[] element) {
                return null;
            }

            @Nullable
            @Override
            public String valueOf(String[] strings) {
                return strings[0];
            }

            @Override
            public void setValue(String[] strings, String value) {
                strings[0] = value;
            }

            @Override
            public boolean isCellEditable(String[] strings) {
                return true;
            }
        }

        protected class ValueColumnInfo extends ElementsColumnInfoBase<String[]> {

            protected ValueColumnInfo() {
                super("VALUE");
            }

            @Nullable
            @Override
            protected String getDescription(String[] element) {
                return null;
            }

            @Nullable
            @Override
            public String valueOf(String[] strings) {
                return strings[1];
            }

            @Override
            public void setValue(String[] strings, String value) {
                strings[1] = value;
            }

            @Override
            public boolean isCellEditable(String[] strings) {
                return true;
            }
        }
    }

}
