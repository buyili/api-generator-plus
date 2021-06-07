package site.forgus.plugins.apigeneratorplus.config;

import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class YApiProjectListTableDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    public YApiProjectListTableDialog() {
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
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void showDialog(List<YApiServerUrlEntity> list) {
        YApiProjectListTableDialog dialog = new YApiProjectListTableDialog();
        dialog.setLocationByPlatform(true);
        dialog.setLocation(500, 200);
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        YApiProjectListTableDialog dialog = new YApiProjectListTableDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
