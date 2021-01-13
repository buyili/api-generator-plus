package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.openapi.options.ConfigurationException;
import site.forgus.plugins.apigeneratorplus.util.AssertUtils;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

import javax.swing.*;
import java.io.IOException;

/**
 * @author lmx 2020/12/2 13:52
 */

public class YApiProjectListsPanel {
    private JCheckBox isMultipleModuleProjectCheckBox;
    private JCheckBox isUseDefaultTokenCheckBox;
    private JPanel listTablePanel;
    private JPanel myPanel;
    private JCheckBox matchWithModuleNameCheckBox;
    private JTextField yApiUrlTextField;
    private JTextField tokenTextField;
    private JTextField defaultCatTextField;
    private JLabel projectIdLabel;
    private JCheckBox autoCatCheckBox;
    private JCheckBox ignoreResponseCheckBox;
    YApiProjectListUI yApiProjectListUI;

    private ApiGeneratorConfig oldState;

    public YApiProjectListsPanel(ApiGeneratorConfig oldState) {
        this.oldState = oldState;
        reset();

        System.out.println();
    }

    private void createUIComponents() {
        yApiProjectListUI = new YApiProjectListUI(oldState);
        listTablePanel = (JPanel) yApiProjectListUI.getComponent();
    }

    public boolean isModified() {
        return !oldState.yApiServerUrl.equals(yApiUrlTextField.getText())
                || !oldState.projectToken.equals(tokenTextField.getText())
                || !oldState.projectId.equals(projectIdLabel.getText())
                || !oldState.defaultCat.equals(defaultCatTextField.getText())
                || oldState.autoCat != autoCatCheckBox.isSelected()
                || oldState.ignoreResponse != ignoreResponseCheckBox.isSelected()
                || oldState.isMultiModule != isMultipleModuleProjectCheckBox.isSelected()
                || oldState.isUseDefaultToken != isUseDefaultTokenCheckBox.isSelected()
                || oldState.matchWithModuleName != matchWithModuleNameCheckBox.isSelected()
                 || yApiProjectListUI.isModified(oldState.yApiProjectConfigInfoList);
    }

    public void apply() throws ConfigurationException {

        oldState.yApiServerUrl = yApiUrlTextField.getText();
        oldState.projectToken = tokenTextField.getText();
        if (AssertUtils.isNotEmpty(yApiUrlTextField.getText()) && AssertUtils.isNotEmpty(tokenTextField.getText())) {
            try {
                oldState.projectId = YApiSdk.getProjectInfo(yApiUrlTextField.getText(), tokenTextField.getText()).get_id().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        oldState.defaultCat = defaultCatTextField.getText();
        oldState.autoCat = autoCatCheckBox.isSelected();
        oldState.ignoreResponse = ignoreResponseCheckBox.isSelected();
        oldState.isMultiModule = isMultipleModuleProjectCheckBox.isSelected();
        oldState.isUseDefaultToken = isUseDefaultTokenCheckBox.isSelected();
        oldState.matchWithModuleName = matchWithModuleNameCheckBox.isSelected();

        yApiProjectListUI.apply(oldState.yApiProjectConfigInfoList);
    }

    public void reset() {
        yApiUrlTextField.setText(oldState.yApiServerUrl);
        tokenTextField.setText(oldState.projectToken);
        projectIdLabel.setText(oldState.projectId);
        defaultCatTextField.setText(oldState.defaultCat);
        autoCatCheckBox.setSelected(oldState.autoCat);
        ignoreResponseCheckBox.setSelected(oldState.ignoreResponse);
        isMultipleModuleProjectCheckBox.setSelected(oldState.isMultiModule);
        isUseDefaultTokenCheckBox.setSelected(oldState.isUseDefaultToken);
        matchWithModuleNameCheckBox.setSelected(oldState.matchWithModuleName);
        yApiProjectListUI.reset(oldState.yApiProjectConfigInfoList);
    }

    public JPanel getPanel() {
        return myPanel;
    }


}
