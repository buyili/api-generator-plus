package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ListItemEditor;
import com.intellij.util.ui.ListModelEditor;
import com.intellij.util.ui.ListModelEditorBase;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.curl.model.CURLModuleInfo;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;
import site.forgus.plugins.apigeneratorplus.util.DeepCloneUtil;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

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

    public final MyListModelEditor<YApiProjectConfigInfo> editor = new MyListModelEditor<>(itemEditor);

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
        editor.setRefreshAction(anActionButton -> {
            Project project = GlobalVariable.getInstance().getProject();
            List<CURLModuleInfo> foundList = CurlUtils.findModuleInfo(project);
            List<YApiProjectConfigInfo> addList = new ArrayList<>();
            List<YApiProjectConfigInfo> entries = editor.getModel().getItems();
            for (YApiProjectConfigInfo entry : entries) {
                foundList.removeIf(curlModuleInfo -> curlModuleInfo.getModuleName().equals(entry.getModuleName()));
            }
            for (CURLModuleInfo curlModuleInfo : foundList) {
                YApiProjectConfigInfo info = new YApiProjectConfigInfo();
                info.setName(curlModuleInfo.getModuleName());
                info.setModuleName(curlModuleInfo.getModuleName());
                addList.add(info);
            }
            editor.getModel().add(addList);
        });

        itemPanel = new YApiProjectListPanel();
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
        List<YApiProjectConfigInfo> cloneList = DeepCloneUtil.deepCloneList(settings);
        editor.reset(cloneList);
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

        List<YApiProjectConfigInfo> newItems = editor.getModel().getItems();

        for (YApiProjectConfigInfo yApiProjectConfigInfo : newItems) {
            if (StringUtils.isNotBlank(yApiProjectConfigInfo.getToken())) {
                try {
                    YApiProject projectInfo = YApiSdk.getProjectInfo(oldState.yApiServerUrl, yApiProjectConfigInfo.getToken());
                    yApiProjectConfigInfo.setProject(projectInfo);
                    yApiProjectConfigInfo.setProjectId(String.valueOf(projectInfo.get_id()));
                    yApiProjectConfigInfo.setBasePath(projectInfo.getBasepath());
                } catch (Exception e) {
                    e.printStackTrace();
                    editor.getList().setSelectedIndex(newItems.indexOf(yApiProjectConfigInfo));
                    throw new ConfigurationException(e.getMessage());
                }
            } else {
                yApiProjectConfigInfo.setProject(null);
            }
        }

        List<YApiProjectConfigInfo> result = editor.apply();

        // apply后，不切换左侧列表项，并且解决再次修改后检测不到是否修改的问题
        YApiProjectConfigInfo item = editor.getSelected();
        if (item != null) {
            itemPanel.setItem(editor.getMutable(item));
        }

        oldState.yApiProjectConfigInfoList = DeepCloneUtil.deepCloneList(result);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return component;
    }

    /**
     * 参考 {@link ListModelEditor} 实现
     * @param <T>
     */
    public static class MyListModelEditor<T> extends ListModelEditorBase<T> {
        private final ToolbarDecorator toolbarDecorator;
        protected boolean myRefreshActionEnabled;
        protected AnActionButtonRunnable myRefreshAction;

        private final JBList list = new JBList(model);

        public MyListModelEditor(@NotNull ListItemEditor<T> itemEditor) {
            super(itemEditor);

            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new MyListCellRenderer());

            toolbarDecorator = ToolbarDecorator.createDecorator(list, model)
                    .setAddAction(button -> {
                        if (!model.isEmpty()) {
                            T lastItem = model.getElementAt(model.getSize() - 1);
                            if (MyListModelEditor.this.itemEditor.isEmpty(lastItem)) {
                                ScrollingUtil.selectItem(list, ContainerUtil.indexOfIdentity(model.getItems(), lastItem));
                                return;
                            }
                        }

                        T item = createElement();
                        model.add(item);
                        ScrollingUtil.selectItem(list, ContainerUtil.indexOfIdentity(model.getItems(), item));
                    })
                    .addExtraAction(new AnActionButton("Scan Module", AllIcons.Actions.Find) {

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            if (myRefreshActionEnabled) {
                                myRefreshAction.run(this);
                            }
                        }
                    })
                    .setRemoveActionUpdater(e -> areSelectedItemsRemovable(list.getSelectionModel()));
        }

        public void setRefreshAction(AnActionButtonRunnable action) {
            myRefreshActionEnabled = action != null;
            myRefreshAction = action;
        }

        @NotNull
        public MyListModelEditor<T> disableUpDownActions() {
            toolbarDecorator.disableUpDownActions();
            return this;
        }

        @NotNull
        public JComponent createComponent() {
            return toolbarDecorator.createPanel();
        }

        @NotNull
        public JBList getList() {
            return list;
        }

        @Nullable
        public T getSelected() {
            //noinspection unchecked
            return (T) list.getSelectedValue();
        }

        @Override
        public void reset(@NotNull List<T> items) {
            super.reset(items);

            // todo should we really do this?
            //noinspection SSBasedInspection
            SwingUtilities.invokeLater(() -> {
                if (!model.isEmpty()) {
                    list.setSelectedIndex(0);
                }
            });
        }

        @NotNull
        @Override
        public List<T> apply() {
            List<T> items = super.apply();
            // 解决点击apply按钮执行成功后，reset按钮不消失的两种情况。1. 给空列表新添一个或多个配置 2. 删除列表中的所有配置
            // 不使用super.apply()方法中的 if (!helper.hasModifiedItems()) { , 避免污染其他情况
            if (items.isEmpty() || this.isModified()) {
                this.helper.reset(items);
            }
            return items;
        }

        private class MyListCellRenderer extends ColoredListCellRenderer {
            @Override
            protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
                setBackground(UIUtil.getListBackground(selected));
                if (value != null) {
                    //noinspection unchecked
                    append((itemEditor.getName(((T) value))));
                }
            }
        }

        @Override
        protected void removeEmptyItem(int i) {
            ListUtil.removeIndices(getList(), new int[]{i});
        }
    }
}

