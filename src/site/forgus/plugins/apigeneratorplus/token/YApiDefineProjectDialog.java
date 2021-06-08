package site.forgus.plugins.apigeneratorplus.token;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;
import site.forgus.plugins.apigeneratorplus.serverurl.YApiDefineServerUrlDialog;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;

/**
 * @author lmx 2021/6/8 14:47
 */

public class YApiDefineProjectDialog extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(YApiDefineServerUrlDialog.class);

    private Project project;

    @NotNull private final JTextField myToken;
    @NotNull private final JComboBox myUrl;

    private YApiServerUrlEntity yApiServerUrlEntity;
    private YApiProject myProject;

    protected YApiDefineProjectDialog(@Nullable Project project, List<YApiServerUrlEntity> urls, String initialToken) {
        super(project);
        this.project = project;
        myToken = new JTextField(initialToken, 30);
        myUrl = new ComboBox<YApiServerUrlEntity>(new UrlComboBoxModel(urls));
        myUrl.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    yApiServerUrlEntity = (YApiServerUrlEntity) e.getItem();
                }else if(e.getStateChange() == ItemEvent.DESELECTED){
                    yApiServerUrlEntity = null;
                }
                System.out.println();
            }
        });
        setTitle("Define Token");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }

    @Nullable
    @Override
    protected JComponent createNorthPanel() {
        JPanel defineRemoteComponent = new JPanel(new GridBagLayout());
        GridBag gb = new GridBag().
                setDefaultAnchor(GridBagConstraints.LINE_START).
                setDefaultInsets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, 0, 0).
                setDefaultFill(GridBagConstraints.HORIZONTAL);
        defineRemoteComponent.add(new JBLabel("URL: ", SwingConstants.RIGHT), gb.nextLine().next().weightx(0.0));
        defineRemoteComponent.add(myUrl, gb.next().weightx(1.0));
        defineRemoteComponent.add(new JBLabel("Token: ", SwingConstants.RIGHT), gb.nextLine().next().weightx(0.0));
        defineRemoteComponent.add(myToken, gb.next().weightx(1.0));
        return defineRemoteComponent;
    }

    @NotNull
    public String getToken(){
        return StringUtil.notNullize(myToken.getText()).trim();
    }

    @NotNull
    public String getServerUrl(){
        return yApiServerUrlEntity == null ? "" : yApiServerUrlEntity.getServerUrl();
    }

    @NotNull
    public String getServerUrlId(){
        return yApiServerUrlEntity == null ? "" : yApiServerUrlEntity.getId();
    }

    public YApiProject getMyProject() {
        return myProject;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myToken;
    }

    @Override
    protected void doOKAction() {
        String token = getToken();
        YApiServerUrlEntity selectedItem = (YApiServerUrlEntity) myUrl.getSelectedItem();
        assert selectedItem != null;
        String url = selectedItem.serverUrl;
        String error = validateTokenUnderModal(token);
        if (error != null) {
            LOG.warn(String.format("Invalid token. token: [%s], URL: [%s], error: %s", getToken(), url, error));
            Messages.showErrorDialog(project, XmlStringUtil.wrapInHtml(error), "Invalid Remote");
        }
        else {
            super.doOKAction();
        }
    }

    @Nullable
    private String validateTokenUnderModal(String token) {
        return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                YApiProject projectInfo = YApiSdk.getProjectInfo(getServerUrl(), token);
                if(projectInfo != null){
                    myProject = projectInfo;
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Token test faild: " + token;
        }, "Checking URL...", true, project);
    }

    private class UrlComboBoxModel extends AbstractListModel<YApiServerUrlEntity> implements ComboBoxModel<YApiServerUrlEntity> {

        private volatile List<YApiServerUrlEntity> allUrls = Collections.emptyList();
        private Object mySelectedItem;

        public UrlComboBoxModel(List<YApiServerUrlEntity> allUrls) {
            this.allUrls = allUrls;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            mySelectedItem = anItem;
            System.out.println();
        }

        @Override
        public Object getSelectedItem() {
            return mySelectedItem;
        }

        @Override
        public int getSize() {
            return allUrls.size();
        }

        @Override
        public YApiServerUrlEntity getElementAt(int index) {
            return allUrls.get(index);
        }
    }
}
