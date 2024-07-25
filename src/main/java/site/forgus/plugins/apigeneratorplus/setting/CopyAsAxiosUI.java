package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author lmx 2021/6/9 10:31
 */

public class CopyAsAxiosUI implements Configurable {
    private JTextArea appendTextArea;
    private JPanel myPanel;

    private Project project;
    private CURLSettingState oldState;

    public CopyAsAxiosUI(Project project, CURLSettingState oldState) {
        this.project = project;
        this.oldState = oldState;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return !oldState.axiosAppend.equals(appendTextArea.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        oldState.axiosAppend = appendTextArea.getText();
    }

    @Override
    public void reset() {
        appendTextArea.setText(oldState.axiosAppend);
    }

    @Override
    public void disposeUIResources() {
        myPanel = null;
    }
}
