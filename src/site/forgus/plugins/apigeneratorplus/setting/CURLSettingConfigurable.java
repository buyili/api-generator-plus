package site.forgus.plugins.apigeneratorplus.setting;

import com.google.gson.Gson;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.*;
import com.intellij.ui.components.*;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.util.JsonUtil;
import site.forgus.plugins.apigeneratorplus.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

/**
 * @author lmx 2020/11/11 17:55
 */

public class CURLSettingConfigurable implements Configurable {
    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";

    Project project;

    CURLSettingState oldState;
    JBTextField baseApiTextField;

    private CURLModuleInfo selectedModuleInfo;
    MyOrderPanel myOrderPanel;
    JBTextField moduleNameTextField;
    JBTextField portTextField;
    JBTextField contextPathTextField;
    JBTextArea canonicalClassNameTextFields;
    JBTextArea includeFiledTextFields;
    JBTextArea excludeFieldTextFields;
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
        this.project = project;
        oldState = ServiceManager.getService(project, CURLSettingState.class);
    }

    @Override
    public String getDisplayName() {
        return "Copy as CURL";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        baseApiTextField = new JBTextField(oldState.baseApi);
        canonicalClassNameTextFields = new JBTextArea(oldState.filterFieldInfo.canonicalClassName, 5, 0);
        includeFiledTextFields = new JBTextArea(oldState.filterFieldInfo.includeFiled, 5, 0);
        excludeFieldTextFields = new JBTextArea(oldState.filterFieldInfo.excludeField, 5, 0);
        arrayFormatTextFields = new JBTextField(oldState.arrayFormat);
        excludeChildrenCheckBox = new JBCheckBox("", oldState.filterFieldInfo.excludeChildren);

        myOrderPanel = new MyOrderPanel();
        myOrderPanel.addAll(oldState.moduleInfoList);

        credentialsTextField = new JBTextField(oldState.fetchConfig.credentials);
        cacheTextField = new JBTextField(oldState.fetchConfig.cache);
        redirectTextField = new JBTextField(oldState.fetchConfig.redirect);
        referrerTextField = new JBTextField(oldState.fetchConfig.referrer);
        referrerPolicyTextField = new JBTextField(oldState.fetchConfig.referrerPolicy);
        integrityTextField = new JBTextField(oldState.fetchConfig.integrity);

        JBTabbedPane jbTabbedPane = new JBTabbedPane();

        JPanel modulePortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modulePortLabelPanel.add(new JBLabel("Module Info"));
        modulePortLabelPanel.add(LinkLabel.create("Find Module Info", new Runnable() {
            @Override
            public void run() {
                List<CURLModuleInfo> foundList = CurlUtils.findModuleInfo(project);
                if (isRepeat(foundList)) {
                    int yesNoCancel = Messages.showYesNoCancelDialog("是否覆盖同名模块？", "提示", Messages.getQuestionIcon());
                    if (Messages.YES == yesNoCancel) {
                        for (CURLModuleInfo foundItem : foundList) {
                            List<CURLModuleInfo> entries = myOrderPanel.getEntries();
                            boolean repeat = false;
                            for (CURLModuleInfo entry : entries) {
                                if (entry.getModuleName().equals(foundItem.getModuleName())) {
                                    if (entry.getId().equals(selectedModuleInfo.getId())) {
                                        myOrderPanel.removeRowSelected();
                                    }
                                    myOrderPanel.setValueAt(foundItem, entries.indexOf(entry));
                                    repeat = true;
                                    break;
                                }
                            }
                            if (!repeat) {
                                myOrderPanel.add(foundItem);
                            }
                        }
                    } else if (Messages.NO == yesNoCancel) {
                        for (CURLModuleInfo entry : myOrderPanel.getEntries()) {
                            foundList.removeIf(curlModuleInfo -> curlModuleInfo.getModuleName().equals(entry.getModuleName()));
                        }
                        myOrderPanel.addAll(foundList);
                    }
                } else {
                    myOrderPanel.addAll(foundList);
                }
            }

            public boolean isRepeat(List<CURLModuleInfo> foundList) {
                for (CURLModuleInfo entry : myOrderPanel.getEntries()) {
                    for (CURLModuleInfo curlModuleInfo : foundList) {
                        if (entry.getModuleName().equals(curlModuleInfo.getModuleName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }));


        JPanel jPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Base Api:"), baseApiTextField, 1, false)
                .addLabeledComponent(new JBLabel("Canonical Class Name:"), canonicalClassNameTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Include Fields:"), includeFiledTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Exclude Fields:"), excludeFieldTextFields, 1, true)
                .addLabeledComponent(new JBLabel("Array Format:"), arrayFormatTextFields, 1, false)
                .addTooltip("indices    // 'a[0]=b&a[1]=c'      brackets    // 'a[]=b&a[]=c'        repeat  // 'a=b&a=c'        comma   // 'a=b,c'")
                .addLabeledComponent(new JBLabel("Exclude Children Field"), excludeChildrenCheckBox)
                .addVerticalGap(16)
                .addSeparator()
                .addComponent(modulePortLabelPanel, 0)
                .addComponentFillVertically(myOrderPanel, 0)
                .getPanel();
        jbTabbedPane.add("Copy as cURL", jPanel);

        JPanel fetchPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("credentials:"), credentialsTextField, 1, false)
                .addTooltip("请求的 credentials，如 omit、same-origin 或者 include。为了在当前域名内自动发送 cookie ， 必须提供这个选项， ")
                .addTooltip("从 Chrome 50 开始， 这个属性也可以接受 FederatedCredential 实例或是一个 PasswordCredential 实例。")
                .addLabeledComponent(new JBLabel("cache:"), cacheTextField, 1, false)
                .addTooltip("请求的 cache 模式: default、 no-store、 reload 、 no-cache 、 force-cache 或者 only-if-cached 。")
                .addLabeledComponent(new JBLabel("redirect:"), redirectTextField, 1, false)
                .addTooltip("可用的 redirect 模式: follow (自动重定向), error (如果产生重定向将自动终止并且抛出一个错误）, 或者 manual (手动处理重定向). ")
                .addTooltip("在Chrome中默认使用follow（Chrome 47之前的默认值是manual）。")
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
//        List<CURLModuleInfo> items = curlSettingListTableWithButtons.getTableView().getItems();
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

            List<CURLModuleInfo> entries = myOrderPanel.getEntries();
        if (selectedModuleInfo != null) {
            for (CURLModuleInfo entry : entries) {
                if (entry.getId().equals(selectedModuleInfo.getId())) {
                    int i = entries.indexOf(entry);
                    entry.setModuleName(moduleNameTextField.getText());
                    entry.setPort(portTextField.getText());
                    entry.setContextPath(contextPathTextField.getText());
                    List<String[]> items = myHeaderListTableWithButton.getTableView().getItems();
//                    System.out.println(JsonUtil.gson.toJson(items));
                    entry.setHeaders(items);
//                    selectedModuleInfo.setModuleName(moduleNameTextField.getText());
//                    selectedModuleInfo.setPort(portTextField.getText());
//                    selectedModuleInfo.setContextPath(contextPathTextField.getText());
//                    List<String[]> items = myHeaderListTableWithButton.getTableView().getItems();
////                    System.out.println(JsonUtil.gson.toJson(items));
//                    selectedModuleInfo.setHeaders(items);
//                    myOrderPanel.getEntryTable().getModel().setValueAt(selectedModuleInfo, i, myOrderPanel.getEntryColumn());
                    break;
                }
            }
        }
//        List<CURLModuleInfo> entries = myOrderPanel.getEntries();
        List<CURLModuleInfo> modelInfoList = oldState.moduleInfoList;
        if (!gson.toJson(modelInfoList).equals(gson.toJson(entries))) {
            System.out.println(gson.toJson(modelInfoList));
            System.out.println(gson.toJson(entries));
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
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

        oldState.moduleInfoList = myOrderPanel.getEntries();
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
        myOrderPanel.addAll(oldState.moduleInfoList);
    }

    protected class MyOrderPanel extends OrderPanel<CURLModuleInfo> {


        private final ToolbarDecorator myDecorator;
        private final CommonActionsPanel myActionsPanel;
        JPanel myDescriptionPanel;
        private final JPanel itemPanelWrapper;
        final CardLayout cardLayout;

        protected MyOrderPanel() {
            super(CURLModuleInfo.class, false);

            cardLayout = new CardLayout();

            JTable entryTable = getEntryTable();
            entryTable.setTableHeader(null);
            entryTable.setDefaultRenderer(CURLModuleInfo.class, new ColoredTableCellRenderer() {
                @Override
                protected void customizeCellRenderer(JTable table, @Nullable Object value,
                                                     boolean selected,
                                                     boolean hasFocus, int row, int column) {
                    setBorder(null);
                    if (value != null) {
                        CURLModuleInfo curlModuleInfo = (CURLModuleInfo) value;
                        append(curlModuleInfo.getModuleName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                        if (StringUtils.isNotEmpty(curlModuleInfo.getPort())) {
                            append("    " + curlModuleInfo.getPort());
                        }
                    }
                }
            });
            entryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    int selectedRow = entryTable.getSelectedRow();
                    if (selectedRow != -1) {
                        selectedModuleInfo = getValueAt(selectedRow);
                        moduleNameTextField.setText(selectedModuleInfo.getModuleName());
                        portTextField.setText(selectedModuleInfo.getPort());
                        contextPathTextField.setText(selectedModuleInfo.getContextPath());
                        myHeaderListTableWithButton.setValues(selectedModuleInfo.getHeaders());
                        cardLayout.show(itemPanelWrapper, PANEL);
                    } else {
                        cardLayout.show(itemPanelWrapper, EMPTY);
                    }
                }
            });
            moduleNameTextField = new JBTextField();
            portTextField = new JBTextField();
            contextPathTextField = new JBTextField();
            myHeaderListTableWithButton = new MyHeaderListTableWithButton();

            myDescriptionPanel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(new JBLabel("Module Name:"), moduleNameTextField, 1, false)
                    .addLabeledComponent(new JBLabel("Port:"), portTextField, 1, false)
                    .addLabeledComponent(new JBLabel("Context Path:"), contextPathTextField, 1, false)
                    .addLabeledComponent(new JBLabel("Headers"), myHeaderListTableWithButton.getComponent(), 1, true)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();

            itemPanelWrapper = new JPanel(cardLayout);

            JLabel descLabel =
                    new JLabel("<html>select module on left</html>");
            descLabel.setBorder(new EmptyBorder(0, 25, 0, 25));

            itemPanelWrapper.add(descLabel, EMPTY);
            itemPanelWrapper.add(myDescriptionPanel, PANEL);
//            myDescriptionPanel.setPreferredSize(new JBDimension(600, 400));
            removeAll();

            myDecorator = createToolbarDecorator();
            myDecorator.setAddAction(createAddAction()).setRemoveAction(createRemoveAction());
            myActionsPanel = myDecorator.getActionsPanel();

            Splitter splitter = new Splitter(false, 0.25f);
            splitter.setFirstComponent(myDecorator.createPanel());
            splitter.setSecondComponent(itemPanelWrapper);
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

        protected CURLModuleInfo createElement() {
            CURLModuleInfo curlModuleInfo = new CURLModuleInfo();
            curlModuleInfo.setId(String.valueOf(System.currentTimeMillis()));
            curlModuleInfo.setModuleName(StringUtil.getName());
            return curlModuleInfo;
        }

        protected void addNewElement(CURLModuleInfo newElement) {
            add(newElement);
            int index = getEntries().size() - 1;
            getEntryTable().setRowSelectionInterval(index, index);
        }

        public void removeRowSelected() {
            int row = getEntryTable().getSelectedRow();
            getEntryTable().removeRowSelectionInterval(row, getEntryColumn());
        }

        protected void removeSelected() {
            JTable entryTable = getEntryTable();
            int[] selectedRows = entryTable.getSelectedRows();
            if (selectedRows.length == 0) {
                return;
            }
            int selectedIndex = entryTable.getSelectedRow();
            List<CURLModuleInfo> entries = getEntries();
            CURLModuleInfo selectRemoveInfo = entries.get(selectedIndex);
            remove(selectRemoveInfo);

            entries = getEntries();
            int pre = selectedIndex - 1;
            if (pre >= 0) {
                entryTable.setRowSelectionInterval(pre, pre);
            } else if (selectedIndex < entries.size()) {
                entryTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                cardLayout.show(itemPanelWrapper, EMPTY);
            }
        }

        @NotNull
        private JScrollPane wrapWithPane(@NotNull JComponent c, int left, int right) {
            JScrollPane pane = ScrollPaneFactory.createScrollPane(c);
            pane.setBorder(JBUI.Borders.customLine(OnePixelDivider.BACKGROUND, 1, left, 1, right));
            return pane;
        }

        public void setValueAt(Object value, int row) {
            getEntryTable().setValueAt(value, row, getEntryColumn());
        }

        @Override
        public String getCheckboxColumnName() {
            return "";
        }

        @Override
        public boolean isCheckable(CURLModuleInfo entry) {
            return false;
        }

        @Override
        public boolean isChecked(CURLModuleInfo entry) {
            return selectedModuleInfo != null && selectedModuleInfo.getModuleName().equals(entry.getModuleName());
        }

        @Override
        protected int getEntryColumn() {
            return super.getEntryColumn();
        }

        @Override
        public void setChecked(CURLModuleInfo entry, boolean checked) {
        }

        public void addAll(List<CURLModuleInfo> orderEntries) {
            for (CURLModuleInfo orderEntry : orderEntries) {
                add(orderEntry.clone());
            }
        }

        public void updateAll(List<CURLModuleInfo> orderEntries) {
            this.clear();
            addAll(orderEntries);
            selectedModuleInfo = null;
            myDescriptionPanel.setVisible(false);
            myDescriptionPanel.updateUI();
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
