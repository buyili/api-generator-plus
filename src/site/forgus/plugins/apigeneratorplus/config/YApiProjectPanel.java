package site.forgus.plugins.apigeneratorplus.config;

import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

import javax.swing.*;

/**
 * @author lmx 2020/11/28 1:07
 **/

public class YApiProjectPanel {
    private JPanel myPanel;
    private JTextField nameTextField;
    private JTextField idTextField;
    private JTextField descTextField;
    private JTextField tokenTextField;
    private JTextField basePathTextField;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void setItem(@Nullable YApiProjectConfigInfo item) {
        if (item == null) {
            return;
        }
        tokenTextField.setText(item.getToken());
        YApiProject yApiProject = item.getProject();
        if (yApiProject == null) {
            nameTextField.setText("");
            idTextField.setText("");
            descTextField.setText("");
            basePathTextField.setText("");
            return;
        }
        nameTextField.setText(yApiProject.getName());
        idTextField.setText(yApiProject.get_id() == null ? "" : yApiProject.get_id().toString());
        descTextField.setText(yApiProject.getDesc());
        basePathTextField.setText(yApiProject.getBasepath());
    }

    public JPanel getPanel() {
        return myPanel;
    }
}
