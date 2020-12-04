package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.curl.model.Header;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author lmx 2020/12/1 11:48
 */

public class YApiProjectListPanel {
    private final CollectionListModel<Object> actionsModel;
    private JPanel myPanel;

    YApiProjectConfigInfo item;
    JTextField nameTextField;
    private JBTextField moduleNameTextField;
    private JTextField tokenTextField;
    private JTextField packageNameTextField;
    private JTextField basePathTextField;

    YApiProjectPanel yApiProjectPanel;
    private JPanel detailPanel;


    public YApiProjectListPanel(@NotNull final CollectionListModel<YApiProjectConfigInfo> model) {
        actionsModel = new CollectionListModel<>();
    }

    public JPanel getPanel() {
        return myPanel;
    }

    public void apply() {
        if (item == null) {
            return;
        }

        item.setName(nameTextField.getText().trim());
        item.setToken(tokenTextField.getText().trim());
        item.setModuleName(moduleNameTextField.getText().trim());
        item.setPackageName(packageNameTextField.getText().trim());
        item.setBasePath(basePathTextField.getText().trim());
    }

    public void setItem(@Nullable YApiProjectConfigInfo item) {
        apply();

        this.item = item;
        if (item == null) {
            return;
        }

        nameTextField.setText(item.getName());
        tokenTextField.setText(item.getToken());
        moduleNameTextField.setText(item.getModuleName());
        packageNameTextField.setText(item.getPackageName());
        basePathTextField.setText(item.getBasePath());

        yApiProjectPanel.setItem(item);

    }

    private void createUIComponents() {
        yApiProjectPanel = new YApiProjectPanel();
        detailPanel = yApiProjectPanel.getPanel();
    }


}
