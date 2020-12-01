package site.forgus.plugins.apigeneratorplus.setting;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

/**
 * @author lmx 2020/12/1 11:48
 */

public class CURLModuleInfoPanel {
    private final CollectionListModel<Object> actionsModel;
    private JPanel myPanel;
    JBTextField moduleNameTextField;
    JBTextField portTextField;
    JBTextField contextPathTextField;
    MyHeaderListTableWithButton myHeaderListTableWithButton;

    CURLModuleInfo item;


    public CURLModuleInfoPanel(@NotNull final CollectionListModel<CURLModuleInfo> model) {
        actionsModel = new CollectionListModel<>();

        myHeaderListTableWithButton = new MyHeaderListTableWithButton();

        moduleNameTextField = new JBTextField();
        portTextField = new JBTextField();
        contextPathTextField = new JBTextField();

        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Module Name:"), moduleNameTextField, 1, false)
                .addLabeledComponent(new JBLabel("Port:"), portTextField, 1, false)
                .addLabeledComponent(new JBLabel("Context Path:"), contextPathTextField, 1, false)
                .addLabeledComponent(new JBLabel("Headers"), myHeaderListTableWithButton.getComponent(), 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

    }

    public JPanel getPanel() {
        return myPanel;
    }

    public void apply() {
        if (item == null) {
            return;
        }

        item.setModuleName(moduleNameTextField.getText().trim());
        item.setPort(portTextField.getText().trim());
        item.setContextPath(contextPathTextField.getText().trim());
        item.setHeaders(myHeaderListTableWithButton.getTableView().getItems());

    }

    public void setItem(@Nullable CURLModuleInfo item) {
        apply();

        this.item = item;
        if (item == null) {
            return;
        }

        moduleNameTextField.setText(item.getModuleName());
        portTextField.setText(item.getPort());
        contextPathTextField.setText(item.getContextPath());
        myHeaderListTableWithButton.setValues(item.getHeaders());

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
