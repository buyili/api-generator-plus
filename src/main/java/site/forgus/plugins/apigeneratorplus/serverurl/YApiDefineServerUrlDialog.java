package site.forgus.plugins.apigeneratorplus.serverurl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

import javax.swing.*;
import java.awt.*;

/**
 * @author lmx 2021/6/8 10:04
 */

public class YApiDefineServerUrlDialog extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(YApiDefineServerUrlDialog.class);

    @NotNull private final JTextField myServerUrl;

    public YApiDefineServerUrlDialog(@Nullable Project project) {
        this(project, "");
    }

    protected YApiDefineServerUrlDialog(@Nullable Project project, @NotNull String initialUrl) {
        super(project);
        myServerUrl = new JTextField(initialUrl, 30);
        setTitle("Define Server Url");
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
        defineRemoteComponent.add(myServerUrl, gb.next().weightx(1.0));
        return defineRemoteComponent;
    }

    @NotNull
    public String getUrl(){
        return StringUtil.notNullize(myServerUrl.getText()).trim();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myServerUrl;
    }

    @Override
    protected void doOKAction() {
        String url = getUrl();
        super.doOKAction();
    }
}
