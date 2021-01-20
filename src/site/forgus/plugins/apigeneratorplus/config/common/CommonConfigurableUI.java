package site.forgus.plugins.apigeneratorplus.config.common;

import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;

import javax.swing.*;

/**
 * @author lmx 2021/1/20 20:18
 **/

public class CommonConfigurableUI {
    private JTextField excludeAnnotationTextField;
    private JPanel jPanel;

    public boolean isModified(FilterFieldInfo item) {
        return !excludeAnnotationTextField.getText().equals(item.canonicalClassName);
    }

    public FilterFieldInfo apply(FilterFieldInfo item) {
        item.setCanonicalClassName(excludeAnnotationTextField.getText().trim());
        return item;
    }


    public void reset(FilterFieldInfo item) {
        excludeAnnotationTextField.setText(item.getCanonicalClassName());
    }

    public JPanel getPanel() {
        return jPanel;
    }
}
