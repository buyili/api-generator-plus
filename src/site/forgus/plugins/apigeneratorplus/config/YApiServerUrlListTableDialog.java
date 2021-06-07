package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class YApiServerUrlListTableDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel bodyPanel;
    private YApiServerUrlListTableWithButtons yApiServerUrlListTableWithButtons;

    public YApiServerUrlListTableDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        yApiServerUrlListTableWithButtons = new YApiServerUrlListTableWithButtons();
        bodyPanel.add(yApiServerUrlListTableWithButtons.getComponent());

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void setItems(List<YApiServerUrlEntity> items){
        yApiServerUrlListTableWithButtons.setValues(items);
        yApiServerUrlListTableWithButtons.getTableView().repaint();
    }

    public static void showDialog(List<YApiServerUrlEntity> list) {
        YApiServerUrlListTableDialog dialog = new YApiServerUrlListTableDialog();

        dialog.setItems(list);
        dialog.setLocationByPlatform(true);
        dialog.setLocation(800, 200);
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        YApiServerUrlListTableDialog dialog = new YApiServerUrlListTableDialog();

        YApiServerUrlEntity yApiServerUrlEntity = new YApiServerUrlEntity();
        yApiServerUrlEntity.setId("1");
        yApiServerUrlEntity.setServerUrl("hello");
        List<YApiServerUrlEntity> items = Arrays.asList(yApiServerUrlEntity);
        dialog.setItems(items);

        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
