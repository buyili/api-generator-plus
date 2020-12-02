package site.forgus.plugins.apigeneratorplus.config;

import com.google.gson.Gson;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.components.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.util.AssertUtils;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiGeneratorSetting implements Configurable {

    private ApiGeneratorConfig oldState;


    JBTextField dirPathTextField;
    JBTextField prefixTextField;
    JBCheckBox cnFileNameCheckBox;
    JBCheckBox overwriteCheckBox;

    JBTextField excludeFields;


    YApiProjectPanel yApiProjectPanel;
    YApiProjectListsPanel yApiProjectListsPanel;

    public ApiGeneratorSetting(Project project) {
        oldState = ServiceManager.getService(project, ApiGeneratorConfig.class);
        yApiProjectListsPanel = new YApiProjectListsPanel(project);
        yApiProjectPanel = new YApiProjectPanel();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Generate Api Plus Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JBTabbedPane jbTabbedPane = new JBTabbedPane();
        GridBagLayout layout = new GridBagLayout();

        //normal setting
        excludeFields = new JBTextField(oldState.excludeFields);
        dirPathTextField = buildTextField(layout, oldState.dirPath);
        prefixTextField = buildTextField(layout, oldState.prefix);
        overwriteCheckBox = buildJBCheckBox(layout, "Overwrite exists docs", oldState.overwrite);
        cnFileNameCheckBox = buildJBCheckBox(layout, "Extract filename from doc comments", oldState.cnFileName);

        JPanel normalJPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Exclude Fields:"), excludeFields, 1, false)
                .addLabeledComponent(new JBLabel("Save Directory:"), dirPathTextField, 1, false)
                .addLabeledComponent(new JBLabel("Indent Style:"), prefixTextField, 1, false)
                .addComponent(overwriteCheckBox)
                .addComponent(cnFileNameCheckBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        jbTabbedPane.addTab("Api Setting", normalJPanel);

        //YApi setting
        jbTabbedPane.addTab("YApi Setting", yApiProjectListsPanel.getPanel());

        return jbTabbedPane;
    }

    private JBCheckBox buildJBCheckBox(GridBagLayout layout, String text, boolean selected) {
        JBCheckBox checkBox = new JBCheckBox();
        checkBox.setText(text);
        checkBox.setSelected(selected);
        layout.setConstraints(checkBox, getValueConstraints());
        return checkBox;
    }

    private JBLabel buildLabel(GridBagLayout layout, String name) {
        JBLabel jbLabel = new JBLabel(name);
        layout.setConstraints(jbLabel, getLabelConstraints());
        return jbLabel;
    }

    private JBTextField buildTextField(GridBagLayout layout, String text) {
        JBTextField textField = new JBTextField(text);
        layout.setConstraints(textField, getValueConstraints());
        return textField;
    }

    private GridBagConstraints getLabelConstraints() {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.fill = GridBagConstraints.EAST;
        labelConstraints.gridwidth = 1;
        return labelConstraints;
    }

    private GridBagConstraints getValueConstraints() {
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.fill = GridBagConstraints.WEST;
        textConstraints.gridwidth = GridBagConstraints.REMAINDER;
        return textConstraints;
    }

    public boolean compareProjectConfigInfoList(List<YApiProjectConfigInfo> var1, List<YApiProjectConfigInfo> var2) {
        Gson gson = new Gson();
        return gson.toJson(var1).equals(gson.toJson(var2));
    }

    @Override
    public boolean isModified() {
        return !oldState.prefix.equals(prefixTextField.getText()) ||
                oldState.cnFileName != cnFileNameCheckBox.isSelected() ||
                oldState.overwrite != overwriteCheckBox.isSelected() ||
                !oldState.dirPath.equals(dirPathTextField.getText()) ||
                !oldState.excludeFields.equals(excludeFields.getText()) ||
                yApiProjectListsPanel.isModified();
    }

    @Override
    public void apply() {
        oldState.excludeFields = excludeFields.getText();
        if (!StringUtils.isEmpty(excludeFields.getText())) {
            String[] split = excludeFields.getText().split(",");
            for (String str : split) {
                oldState.excludeFieldNames.add(str);
            }
        }
        oldState.dirPath = dirPathTextField.getText();
        oldState.prefix = prefixTextField.getText();
        oldState.cnFileName = cnFileNameCheckBox.isSelected();
        oldState.overwrite = overwriteCheckBox.isSelected();

        yApiProjectListsPanel.apply();

    }

    @Override
    public void reset() {
        excludeFields.setText(oldState.excludeFields);
        dirPathTextField.setText(oldState.dirPath);
        prefixTextField.setText(oldState.prefix);
        cnFileNameCheckBox.setSelected(oldState.cnFileName);
        overwriteCheckBox.setSelected(oldState.overwrite);
        yApiProjectListsPanel.reset();
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Test");
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JBMenu jbMenu = new JBMenu();
        AddEditDeleteListPanel helo = new AddEditDeleteListPanel<String>("hello", Arrays.asList("helo")) {
            @Nullable
            @Override
            protected String findItemToAdd() {
                return null;
            }

            @Nullable
            @Override
            protected String editSelectedItem(String item) {
                return null;
            }
        };

        String[] hello = new String[]{"hel", "l"};
        DefaultListModel<String> defaultListModel = JBList.createDefaultListModel(hello);
        JBList<Object> objectJBList = new JBList<Object>(defaultListModel);
        frame.getContentPane().add(helo);

//        final MultiColumnList list = new MultiColumnList("1", 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
//        list.setFixedColumnsMode(5);
//        frame.getContentPane().add(list);
        frame.setVisible(true);
    }

}
