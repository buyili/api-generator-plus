package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.openapi.ui.ComboBox;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class ChooseYApiProjectDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBoxProject;
    private JPanel bodyPanel;
    private JPanel yapiProjectPanelWrap;
    private YApiProjectListPanel yApiProjectListPanel;

    private List<YApiProjectConfigInfo> list;

    private YApiProjectConfigInfo selectConfigInfo;
    private int exitCode = -1;

    public ChooseYApiProjectDialog(List<YApiProjectConfigInfo> list) {
        this.list = list;
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

        comboBoxProject.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                YApiProjectConfigInfo info = (YApiProjectConfigInfo) value;
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(info.getName());
                return component;
            }
        });
        comboBoxProject.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    YApiProjectConfigInfo item = (YApiProjectConfigInfo) e.getItem();
                    exitCode = list.indexOf(item);
                    yApiProjectListPanel.setItem(item);
                }
            }
        });

        if (list.size() > 0) {
            exitCode = 0;
            yApiProjectListPanel.setItem(list.get(0));
        }
        yApiProjectListPanel.setEditable(false);
        yapiProjectPanelWrap.add(yApiProjectListPanel.getPanel());
        setTitle("Choose YApi project");
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        exitCode = -1;
        dispose();
    }

    public static int showDialog(List<YApiProjectConfigInfo> list) {
        ChooseYApiProjectDialog dialog = new ChooseYApiProjectDialog(list);
        dialog.setLocationByPlatform(true);
        dialog.setLocation(500, 200);
        dialog.pack();
        dialog.setVisible(true);
        return dialog.exitCode;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        comboBoxProject = new ComboBox<>(list.toArray(new YApiProjectConfigInfo[0]));
        yApiProjectListPanel = new YApiProjectListPanel();
    }

    //public static void main(String[] args) {
    //    YApiProjectConfigInfo info = new YApiProjectConfigInfo();
    //    info.setPackageName("packageName");
    //    info.setToken("token");
    //    info.setProjectId("projectId");
    //    info.setBasePath("basePath");
    //    YApiProject project = new YApiProject();
    //    project.setName("project name");
    //    info.setProject(project);
    //    YApiProjectConfigInfo info1 = new YApiProjectConfigInfo();
    //    info1.setPackageName("packageName");
    //    info1.setToken("token");
    //    info1.setProjectId("projectId");
    //    info1.setBasePath("basePath");
    //    YApiProject project1 = new YApiProject();
    //    project.setName("project name");
    //    info1.setProject(project1);
    //
    //    ChooseYApiProjectDialog dialog = new ChooseYApiProjectDialog(Arrays.asList(info, info1));
    //    dialog.setLocationByPlatform(true);
    //    dialog.pack();
    //    dialog.setVisible(true);
    //    System.exit(0);
    //}
}
