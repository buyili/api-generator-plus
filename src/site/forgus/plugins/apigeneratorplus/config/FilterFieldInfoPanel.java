package site.forgus.plugins.apigeneratorplus.config;

import site.forgus.plugins.apigeneratorplus.model.FilterFieldInfo;

import javax.swing.*;

/**
 * @author lmx 2020/12/6 11:47
 **/

public class FilterFieldInfoPanel {
    private JTextArea canonicalClassNameTextFields;
    private JTextArea includeFiledTextFields;
    private JTextArea excludeFieldTextFields;
    private JCheckBox excludeChildrenFieldCheckBox;
    private JPanel jPanel;

    public void setItem(FilterFieldInfo item){
        reset(item);
    }

    public boolean isModified(FilterFieldInfo item){
        return !canonicalClassNameTextFields.getText().equals(item.canonicalClassName)
                || !includeFiledTextFields.getText().equals(item.includeFiled)
                || !excludeFieldTextFields.getText().equals(item.excludeField)
                || excludeChildrenFieldCheckBox.isSelected() != item.excludeChildren;
    }

    public FilterFieldInfo apply(FilterFieldInfo item){
        item.setCanonicalClassName(canonicalClassNameTextFields.getText().trim());
        item.setIncludeFiled(includeFiledTextFields.getText().trim());
        item.setExcludeField(excludeFieldTextFields.getText().trim());
        item.setExcludeChildren(excludeChildrenFieldCheckBox.isSelected());
        return item;
    }


    public void reset(FilterFieldInfo item){
        canonicalClassNameTextFields.setText(item.getCanonicalClassName());
        includeFiledTextFields.setText(item.getIncludeFiled());
        excludeFieldTextFields.setText(item.getExcludeField());
        excludeChildrenFieldCheckBox.setSelected(item.excludeChildren);
    }

    public JPanel getPanel(){
        return jPanel;
    }

}
