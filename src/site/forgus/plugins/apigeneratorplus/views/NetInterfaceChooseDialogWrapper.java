package site.forgus.plugins.apigeneratorplus.views;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.CheckBoxListListener;
import site.forgus.plugins.apigeneratorplus.model.NetInterfaceWrap;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * @author limaoxu
 */
public class NetInterfaceChooseDialogWrapper extends DialogWrapper {

    private int selectedInt = -1;
    private List<NetInterfaceWrap> netInterfaceWraps;
    private String port = "";
    private ComboBox<String> myComboBox;
    private CheckBoxList<NetInterfaceWrap> checkBoxList;

    public NetInterfaceChooseDialogWrapper(List<NetInterfaceWrap> netInterfaceWraps, String[] ports, String portInitialValue) {
        super(true); // use current window as parent
        setTitle("Select IP and Port");
        init();

        this.myComboBox.setModel(new DefaultComboBoxModel(ports));
        this.myComboBox.setSelectedItem(portInitialValue);
        this.myComboBox.setEditable(true);
        this.myComboBox.getEditor().setItem(portInitialValue);
        this.myComboBox.setSelectedItem(portInitialValue);

        this.netInterfaceWraps = netInterfaceWraps;
        for (int i = 0; i < netInterfaceWraps.size(); i++) {
            NetInterfaceWrap netInterfaceWrap = netInterfaceWraps.get(i);
            if (netInterfaceWrap.isChecked()) {
                selectedInt = i;
            }
            this.checkBoxList.addItem(netInterfaceWrap, netInterfaceWrap.getDisplayName() + "    " + netInterfaceWrap.getIpV4(), netInterfaceWrap.isChecked());
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Testing");
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        checkBoxList = new CheckBoxList<>();

        checkBoxList.setCheckBoxListListener(new CheckBoxListListener() {
            @Override
            public void checkBoxSelectionChanged(int i, boolean b) {
                System.out.println(i + "  " + b);
                // 设置已选中复选框不能取消选中
                if (selectedInt == i) {
                    checkBoxList.setItemSelected(netInterfaceWraps.get(selectedInt), true);
                }

                // 取消选中其他复选框
                if (selectedInt != -1 && selectedInt != i) {
                    checkBoxList.setItemSelected(netInterfaceWraps.get(selectedInt), false);
                }
                selectedInt = i;
            }
        });
        dialogPanel.add(checkBoxList);

        this.myComboBox = new ComboBox(220);
        dialogPanel.add(this.myComboBox, "South");

        return dialogPanel;
    }

    @Nullable
    public NetInterfaceWrap getSelectedItem() {
        return selectedInt != -1 ? netInterfaceWraps.get(selectedInt) : null;
    }

    @Nullable
    public String getInputString() {
        return this.getExitCode() == 0 ? this.myComboBox.getSelectedItem().toString() : null;
    }

    //public static parseNetworkInterfaces(NetworkInterface[])

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                //Messages.showCheckboxOkCancelDialog("message", "title", "checkboxText,2", false, 1, 1, null);
                //MessagesService.getInstance().showChooseDialog((Project)null, (Component)null,"message", "title", new String[]{"item", "item2"}, "item2", null);
                //Messages.showEditableChooseDialog("message", "Title", null, new String[]{"item", "item2"}, "item2",null);


                List<NetInterfaceWrap> interfaceList = Arrays.asList(
                        new NetInterfaceWrap("xxx", "192.168.1.1"),
                        new NetInterfaceWrap("xxx1", "192.168.1.1", true)
                );
                NetInterfaceChooseDialogWrapper chooseDialogWrapper = new NetInterfaceChooseDialogWrapper(interfaceList, new String[]{"8080", "8081"}, "8080");
                if (chooseDialogWrapper.showAndGet()) {
                    // user pressed OK
                    System.out.println(chooseDialogWrapper.getSelectedItem());
                    System.out.println(chooseDialogWrapper.getInputString());
                }
            }
        });
    }
}
