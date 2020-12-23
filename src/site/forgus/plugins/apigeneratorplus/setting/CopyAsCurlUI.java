package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author lmx 2020/12/22 9:54
 */

public class CopyAsCurlUI {
    private JTabbedPane tabbedPane1;
    private JPanel myPanel;
    private JTextField baseApiTextField;
    private JTextArea canonicalClassNameTextFields;
    private JTextArea includeFiledTextFields;
    private JTextArea excludeFieldTextFields;
    private JCheckBox excludeChildrenCheckBox;
    private JTextField arrayFormatTextFields;
    private JPanel curlModulePanel;
    private JTextField credentialsTextField;
    private JTextField cacheTextField;
    private JTextField redirectTextField;
    private JTextField referrerTextField;
    private JTextField referrerPolicyTextField;
    private JTextField integrityTextField;
    private JPanel modulePortLabelPanel;

    private CURLModuleInfoUI curlModuleInfoUI;
    private CURLSettingState oldState;
    private Project project;

    public CopyAsCurlUI(CURLSettingState oldState, Project project) {
        this.oldState = oldState;
        this.project = project;
    }

    private void createUIComponents() {
        curlModuleInfoUI = new CURLModuleInfoUI(oldState);
        curlModulePanel = (JPanel) curlModuleInfoUI.getComponent();

        modulePortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modulePortLabelPanel.add(new JBLabel("Module Info"));
        modulePortLabelPanel.add(LinkLabel.create("Find Module Info", new Runnable() {
            @Override
            public void run() {
                java.util.List<CURLModuleInfo> foundList = CurlUtils.findModuleInfo(project);
                if (isRepeat(foundList)) {
                    int yesNoCancel = Messages.showYesNoCancelDialog("是否覆盖同名模块？", "提示", Messages.getQuestionIcon());
                    if (Messages.YES == yesNoCancel) {
                        for (CURLModuleInfo foundItem : foundList) {
                            java.util.List<CURLModuleInfo> entries = curlModuleInfoUI.editor.getModel().getItems();
                            boolean repeat = false;
                            for (CURLModuleInfo entry : entries) {
                                if (entry.getModuleName().equals(foundItem.getModuleName())) {
                                    CURLModuleInfo mutable = curlModuleInfoUI.editor.getMutable(entry);
                                    mutable.setPort(entry.getPort());
                                    mutable.setContextPath(entry.getContextPath());

                                    CURLModuleInfo selected = curlModuleInfoUI.editor.getSelected();
                                    if (selected != null && entry.getId().equals(selected.getId())) {
                                        curlModuleInfoUI.itemPanel.setItem(mutable);
                                    }
                                    repeat = true;
                                    break;
                                }
                            }
                            if (!repeat) {
                                curlModuleInfoUI.editor.getModel().add(foundItem);
                            }
                        }
                    } else if (Messages.NO == yesNoCancel) {
                        for (CURLModuleInfo entry : curlModuleInfoUI.editor.getModel().getItems()) {
                            foundList.removeIf(curlModuleInfo -> curlModuleInfo.getModuleName().equals(entry.getModuleName()));
                        }
                        curlModuleInfoUI.editor.getModel().add(foundList);
                    }
                } else {
                    curlModuleInfoUI.editor.getModel().add(foundList);
                }
            }

            public boolean isRepeat(List<CURLModuleInfo> foundList) {
                for (CURLModuleInfo entry : curlModuleInfoUI.editor.getModel().getItems()) {
                    for (CURLModuleInfo curlModuleInfo : foundList) {
                        if (entry.getModuleName().equals(curlModuleInfo.getModuleName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }));
    }

    public boolean isModified() {
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

        return curlModuleInfoUI.isModified(oldState.moduleInfoList);
    }

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

        curlModuleInfoUI.apply(oldState.moduleInfoList);
    }

    public void reset() {
        baseApiTextField.setText(oldState.baseApi);
        canonicalClassNameTextFields.setText(oldState.filterFieldInfo.canonicalClassName);
        includeFiledTextFields.setText(oldState.filterFieldInfo.includeFiled);
        excludeFieldTextFields.setText(oldState.filterFieldInfo.excludeField);
        excludeChildrenCheckBox.setSelected(oldState.filterFieldInfo.excludeChildren);
        arrayFormatTextFields.setText(oldState.arrayFormat);
        credentialsTextField.setText(oldState.fetchConfig.credentials);
        cacheTextField.setText(oldState.fetchConfig.cache);
        redirectTextField.setText(oldState.fetchConfig.redirect);
        referrerTextField.setText(oldState.fetchConfig.referrer);
        referrerPolicyTextField.setText(oldState.fetchConfig.referrerPolicy);
        integrityTextField.setText(oldState.fetchConfig.integrity);
        curlModuleInfoUI.reset(oldState.moduleInfoList);
    }

    public JPanel getPanel() {
        return myPanel;
    }
}
