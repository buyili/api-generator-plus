package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.ListItemEditor;
import com.intellij.util.ui.ListModelEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2020/12/1 11:37
 */

public class YApiProjectListUI implements ConfigurableUi<List<YApiProjectConfigInfo>> {

    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";

    private final JPanel itemPanelWrapper;
    final CardLayout cardLayout;
    YApiProjectListPanel itemPanel;

    private ApiGeneratorConfig oldState;

    private final ListItemEditor<YApiProjectConfigInfo> itemEditor = new ListItemEditor<YApiProjectConfigInfo>() {

        @NotNull
        @Override
        public Class<? extends YApiProjectConfigInfo> getItemClass() {
            return YApiProjectConfigInfo.class;
        }

        @Override
        public YApiProjectConfigInfo clone(@NotNull YApiProjectConfigInfo item, boolean forInPlaceEditing) {
            return item.clone();
        }

        @Override
        public boolean isEmpty(@NotNull YApiProjectConfigInfo item) {
            return item.getName().isEmpty() && item.getToken().isEmpty() && item.getModuleName().isEmpty()
                    && item.getPackageName().isEmpty() && item.getBasePath().isEmpty();
        }

        @NotNull
        @Override
        public String getName(@NotNull YApiProjectConfigInfo item) {
            return item.getName();
        }
    };

    public final ListModelEditor<YApiProjectConfigInfo> editor = new ListModelEditor<>(itemEditor);

    private final JComponent component;

    protected YApiProjectListUI(ApiGeneratorConfig state) {
        this.oldState = state;
        cardLayout = new CardLayout();

        editor.disableUpDownActions();
        editor.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                YApiProjectConfigInfo item = editor.getSelected();
                if (item == null) {
                    cardLayout.show(itemPanelWrapper, EMPTY);
                    itemPanel.setItem(null);
                } else {
                    itemPanel.setItem(editor.getMutable(item));
                    cardLayout.show(itemPanelWrapper, PANEL);
                }
            }
        });

        itemPanel = new YApiProjectListPanel(editor.getModel());
        itemPanel.nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                YApiProjectConfigInfo item = itemPanel.item;
                if (item != null) {
                    String name = itemPanel.nameTextField.getText();
                    boolean changed = !item.getName().equals(name);
                    item.setName(name);
                    if (changed) {
                        editor.getList().repaint();
                    }
                }
            }
        });


        itemPanelWrapper = new JPanel(cardLayout);

        JLabel descLabel =
                new JLabel("<html>select module on left</html>");
        descLabel.setBorder(new EmptyBorder(0, 25, 0, 25));

        itemPanelWrapper.add(descLabel, EMPTY);
        itemPanelWrapper.add(itemPanel.getPanel(), PANEL);

        Splitter splitter = new Splitter(false, 0.25f);
        splitter.setFirstComponent(editor.createComponent());
        splitter.setSecondComponent(itemPanelWrapper);
        component = splitter;
    }


    @Override
    public void reset(@NotNull List<YApiProjectConfigInfo> settings) {
        editor.reset(settings);
    }

    @Override
    public boolean isModified(@NotNull List<YApiProjectConfigInfo> settings) {
        itemPanel.apply();
        return editor.isModified();
    }

    @Override
    public void apply(@NotNull List<YApiProjectConfigInfo> settings) throws ConfigurationException {
        itemPanel.apply();

        editor.ensureNonEmptyNames("'Name' must not to be empty");
//        editor.processModifiedItems((newItem, oldItem) -> {
//            if (!oldItem.getModuleName().equals(newItem.getModuleName())) {
////                keymapListener.quickListRenamed(oldItem, newItem);
//            }
//            return true;
//        });

        if (isModified(settings)) {
            List<YApiProjectConfigInfo> result = editor.apply();
            if (result.size() == 0) {
                editor.reset(result);
            }
            if (editor.isModified()) {
                // 解决   editor.reset(result);   后result被清空问题
                List<YApiProjectConfigInfo> newList = new ArrayList<>(result);
                editor.reset(newList);
            }

            // apply后，不切换左侧列表项，再次修改后检测不到是否修改
            YApiProjectConfigInfo item = editor.getSelected();
            if (item != null) {
                itemPanel.setItem(editor.getMutable(item));
            }

            oldState.yApiProjectConfigInfoList = new ArrayList<>(result);

        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return component;
    }
}

