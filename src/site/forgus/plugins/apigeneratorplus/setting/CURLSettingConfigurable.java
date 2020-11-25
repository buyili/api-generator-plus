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
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
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


    JBTextField credentialsTextField;
    JBTextField cacheTextField;
    JBTextField redirectTextField;
    JBTextField referrerTextField;
    JBTextField referrerPolicyTextField;
    JBTextField integrityTextField;

    public CURLSettingConfigurable(Project project) {
        oldState = ServiceManager.getService(project, CURLSettingState.class);
    }

    @Override
    public String getDisplayName() {
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

        credentialsTextField = new JBTextField(oldState.fetchConfig.credentials);
        cacheTextField = new JBTextField(oldState.fetchConfig.cache);
        redirectTextField = new JBTextField(oldState.fetchConfig.redirect);
        referrerTextField = new JBTextField(oldState.fetchConfig.referrer);
        referrerPolicyTextField = new JBTextField(oldState.fetchConfig.referrerPolicy);
        integrityTextField = new JBTextField(oldState.fetchConfig.integrity);


        JBTabbedPane jbTabbedPane = new JBTabbedPane();

        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Base Api:"), baseApiTextField, 1, false)
                .addLabeledComponent(new JBLabel("Canonical Class Name:"), canonicalClassNameTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Include Fields:"), includeFiledTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Exclude Fields:"), excludeFieldTextFields, 1, false)
                .addLabeledComponent(new JBLabel("Array Format:"), arrayFormatTextFields, 1, false)
                .addTooltip("indices    // 'a[0]=b&a[1]=c'      brackets    // 'a[]=b&a[]=c'        repeat  // 'a=b&a=c'        comma   // 'a=b,c'")
                .addLabeledComponent(new JBLabel("Exclude Children Field"), excludeChildrenCheckBox)
                .addVerticalGap(4)
                .addLabeledComponentFillVertically("Module and Port", myOrderPanel)
                .getPanel();
        jbTabbedPane.add("Copy as cURL", jPanel);

        JPanel fetchPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("credentials:"), credentialsTextField, 1, false)
                .addTooltip("请求的 credentials，如 omit、same-origin 或者 include。为了在当前域名内自动发送 cookie ， 必须提供这个选项， 从 Chrome 50 开始， 这个属性也可以接受 FederatedCredential 实例或是一个 PasswordCredential 实例。")
                .addLabeledComponent(new JBLabel("cache:"), cacheTextField, 1, false)
                .addTooltip("请求的 cache 模式: default、 no-store、 reload 、 no-cache 、 force-cache 或者 only-if-cached 。")
                .addLabeledComponent(new JBLabel("redirect:"), redirectTextField, 1, false)
                .addTooltip("可用的 redirect 模式: follow (自动重定向), error (如果产生重定向将自动终止并且抛出一个错误）, 或者 manual (手动处理重定向). 在Chrome中默认使用follow（Chrome 47之前的默认值是manual）。")
                .addLabeledComponent(new JBLabel("referrer:"), referrerTextField, 1, false)
                .addTooltip("一个 USVString 可以是 no-referrer、client或一个 URL。默认是 client。")
                .addLabeledComponent(new JBLabel("referrerPolicy:"), referrerPolicyTextField, 1, false)
                .addTooltip("no-referrer、 no-referrer-when-downgrade、 origin、origin-when-cross-origin、 unsafe-url 。")
                .addLabeledComponent(new JBLabel("integrity:"), integrityTextField)
                .addTooltip("包括请求的  subresource integrity 值 （ 例如： sha256-BpfBw7ivV8q2jLiT13fxDYAe2tJllusRSZ273h2nFSE=）。")
                .addVerticalGap(4)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        jbTabbedPane.add("Copy as fetch", fetchPanel);
        return jbTabbedPane;
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
        if (!oldState.fetchConfig.credentials.equals(credentialsTextField.getText())
                || !oldState.fetchConfig.cache.equals(cacheTextField.getText())
                || !oldState.fetchConfig.redirect.equals(redirectTextField.getText())
                || !oldState.fetchConfig.referrer.equals(referrerTextField.getText())
                || !oldState.fetchConfig.referrerPolicy.equals(referrerPolicyTextField.getText())
                || !oldState.fetchConfig.integrity.equals(integrityTextField.getText())
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


        oldState.fetchConfig.credentials = credentialsTextField.getText();
        oldState.fetchConfig.cache = cacheTextField.getText();
        oldState.fetchConfig.redirect = redirectTextField.getText();
        oldState.fetchConfig.referrer = referrerTextField.getText();
        oldState.fetchConfig.referrerPolicy = referrerPolicyTextField.getText();
        oldState.fetchConfig.integrity = integrityTextField.getText();

        oldState.modelInfoList = myOrderPanel.getEntries();
    }

    @Override
    public void reset() {
        baseApiTextField.setText(oldState.baseApi);
        canonicalClassNameTextFields.setText(oldState.filterFieldInfo.canonicalClassName);
        includeFiledTextFields.setText(oldState.filterFieldInfo.includeFiled);
        excludeFieldTextFields.setText(oldState.filterFieldInfo.excludeField);
        arrayFormatTextFields.setText(oldState.arrayFormat);
        excludeChildrenCheckBox.setSelected(oldState.filterFieldInfo.excludeChildren);

        credentialsTextField.setText(oldState.fetchConfig.credentials);
        cacheTextField.setText(oldState.fetchConfig.cache);
        redirectTextField.setText(oldState.fetchConfig.redirect);
        referrerTextField.setText(oldState.fetchConfig.referrer);
        referrerPolicyTextField.setText(oldState.fetchConfig.referrerPolicy);
        integrityTextField.setText(oldState.fetchConfig.integrity);

        myOrderPanel.clear();
        myOrderPanel.addAll(oldState.modelInfoList);
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
