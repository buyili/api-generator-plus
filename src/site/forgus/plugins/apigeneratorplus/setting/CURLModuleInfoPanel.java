package site.forgus.plugins.apigeneratorplus.setting;

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
        item.setRequestHeaders(new ArrayList<>(myHeaderListTableWithButton.getTableView().getItems()));
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
        myHeaderListTableWithButton.setValues(item.getRequestHeaders());
        myHeaderListTableWithButton.getTableView().repaint();

    }



    protected class MyHeaderListTableWithButton extends ListTableWithButtons<Header> {
        @Override
        protected ListTableModel createListModel() {
            return new ListTableModel(new KeyColumnInfo(), new ValueColumnInfo());
        }

        @Override
        protected Header createElement() {
            return new Header();
        }

        @Override
        protected boolean isEmpty(Header element) {
            return element.getKey() == null || "".equals(element.getKey());
        }

        @Override
        protected Header cloneElement(Header variable) {
            return variable == null ? null : variable.clone();
        }

        @Override
        protected boolean canDeleteElement(Header selection) {
            return true;
        }


        protected class KeyColumnInfo extends ElementsColumnInfoBase<Header> {

            protected KeyColumnInfo() {
                super("KEY");
            }

            @Override
            public void setValue(Header header, String value) {
                header.setKey(value);
            }

            @Override
            public boolean isCellEditable(Header header) {
                return true;
            }

            @Nullable
            @Override
            protected String getDescription(Header element) {
                return null;
            }

            @Nullable
            @Override
            public String valueOf(Header header) {
                return header == null ? "" : header.getKey();
            }
        }

        protected class ValueColumnInfo extends ElementsColumnInfoBase<Header> {

            protected ValueColumnInfo() {
                super("VALUE");
            }

            @Override
            public boolean isCellEditable(Header header) {
                return true;
            }

            @Override
            public void setValue(Header header, String value) {
                header.setValue(value);
            }

            @Nullable
            @Override
            protected String getDescription(Header element) {
                return null;
            }

            @Nullable
            @Override
            public String valueOf(Header header) {
                return header == null ? "" : header.getValue();
            }
        }
    }
}
