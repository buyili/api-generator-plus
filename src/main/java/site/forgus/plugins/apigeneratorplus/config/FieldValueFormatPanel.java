package site.forgus.plugins.apigeneratorplus.config;

import javax.swing.*;

/**
 * @author lmx 2021/6/1 15:10
 */

public class FieldValueFormatPanel {
    private JTextField dateTextField;
    private JTextField localDateTimeTextField;
    private JTextField localDateTextField;
    private JTextField localTimeTextField;
    private JPanel myPanel;

    private ApiGeneratorConfig state;

    public FieldValueFormatPanel(ApiGeneratorConfig state) {
        this.state = state;
        reset();
    }

    public JPanel getPanel() {
        return myPanel;
    }

    public void apply() {
        state.dateFormat = dateTextField.getText();
        state.localDateFormat = localDateTextField.getText();
        state.localDateTimeFormat = localDateTimeTextField.getText();
        state.localTimeFormat = localTimeTextField.getText();
    }

    public boolean isModified() {
        return !state.dateFormat.equals(dateTextField.getText())
                || !state.localDateTimeFormat.equals(localDateTimeTextField.getText())
                || !state.localDateFormat.equals(localDateTextField.getText())
                || !state.localTimeFormat.equals(localTimeTextField.getText())
                ;
    }

    public void reset() {
        dateTextField.setToolTipText("福建傲问卷法我金额of");
        dateTextField.setText(state.dateFormat);
        localDateTimeTextField.setText(state.localDateTimeFormat);
        localDateTextField.setText(state.localDateFormat);
        localTimeTextField.setText(state.localTimeFormat);
    }
}
