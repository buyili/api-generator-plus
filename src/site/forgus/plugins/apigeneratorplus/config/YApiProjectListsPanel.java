package site.forgus.plugins.apigeneratorplus.config;

import com.google.gson.Gson;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.util.AssertUtils;
import site.forgus.plugins.apigeneratorplus.yapi.model.YApiProject;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lmx 2020/12/2 13:52
 */

public class YApiProjectListsPanel {
    private JCheckBox isMultipleModuleProjectCheckBox;
    private JCheckBox isUseDefaultTokenCheckBox;
    private JPanel detailPanel;
    private JPanel listTablePanel;
    private JPanel myPanel;
    private JCheckBox matchWithModuleNameCheckBox;
    YApiProjectPanel yApiProjectPanel;

    ProjectConfigListTableWithButtons projectConfigListTableWithButtons;
    private ApiGeneratorConfig oldState;

    public YApiProjectListsPanel(Project project) {
        oldState = ServiceManager.getService(project, ApiGeneratorConfig.class);

//        isMultipleModuleProjectCheckBox.setSelected(oldState.isMultiModule);
//        isUseDefaultTokenCheckBox.setSelected(oldState.isUseDefaultToken);
//        projectConfigListTableWithButtons.setValues(oldState.yApiProjectConfigInfoList);
        reset();

        System.out.println();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        projectConfigListTableWithButtons = new ProjectConfigListTableWithButtons();
        projectConfigListTableWithButtons.getTableView().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                YApiProjectConfigInfo item = projectConfigListTableWithButtons.getTableView().getSelectedObject();
                yApiProjectPanel.setItem(item);
            }
        });
        listTablePanel = (JPanel) projectConfigListTableWithButtons.getComponent();
        yApiProjectPanel = new YApiProjectPanel();
        detailPanel = yApiProjectPanel.getPanel();
    }

    public boolean isModified() {
        return oldState.isMultiModule != isMultipleModuleProjectCheckBox.isSelected()
                || oldState.isUseDefaultToken != isUseDefaultTokenCheckBox.isSelected()
                || oldState.matchWithModuleName != matchWithModuleNameCheckBox.isSelected()
                || !compareProjectConfigInfoList(oldState.yApiProjectConfigInfoList, projectConfigListTableWithButtons.getTableView().getItems());
    }

    public void apply() {
        oldState.isMultiModule = isMultipleModuleProjectCheckBox.isSelected();
        oldState.isUseDefaultToken = isUseDefaultTokenCheckBox.isSelected();
        oldState.matchWithModuleName = matchWithModuleNameCheckBox.isSelected();
        List<YApiProjectConfigInfo> items = projectConfigListTableWithButtons.getTableView().getItems();
        for (YApiProjectConfigInfo item : items) {
            if (AssertUtils.isNotEmpty(oldState.yApiServerUrl) && AssertUtils.isNotEmpty(item.getToken())) {
                try {
                    YApiProject yApiProject = YApiSdk.getProjectInfo(oldState.yApiServerUrl, item.getToken());
                    if (null != yApiProject) {
                        item.setProject(yApiProject);
                        String projectId = yApiProject.get_id().toString();
                        item.setProjectId(projectId);
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
        List<YApiProjectConfigInfo> newList = new ArrayList<>();
        for (YApiProjectConfigInfo item : items) {
            newList.add(item.clone());
        }
        oldState.yApiProjectConfigInfoList = newList;
    }

    public void reset() {
        isMultipleModuleProjectCheckBox.setSelected(oldState.isMultiModule);
        isUseDefaultTokenCheckBox.setSelected(oldState.isUseDefaultToken);
        matchWithModuleNameCheckBox.setSelected(oldState.matchWithModuleName);
        projectConfigListTableWithButtons.setValues(oldState.yApiProjectConfigInfoList);
    }

    public JPanel getPanel() {
//        listTablePanel.add(projectConfigListTableWithButtons.getComponent());
        return myPanel;
    }

    public boolean compareProjectConfigInfoList(List<YApiProjectConfigInfo> var1, List<YApiProjectConfigInfo> var2) {
        Gson gson = new Gson();
        return gson.toJson(var1).equals(gson.toJson(var2));
    }

    public static class ProjectConfigListTableWithButtons extends ListTableWithButtons<YApiProjectConfigInfo> {
        @Override
        protected ListTableModel createListModel() {
            return new ListTableModel(new TokenColumnInfo(), new PackageNameColumnInfo(), new ModuleNameColumnInfo(),
                    new BasePathColumnInfo());
        }

        @Override
        protected YApiProjectConfigInfo createElement() {
            return new YApiProjectConfigInfo();
        }

        @Override
        protected boolean isEmpty(YApiProjectConfigInfo element) {
            return false;
        }

        @Override
        protected YApiProjectConfigInfo cloneElement(YApiProjectConfigInfo variable) {
            return variable.clone();
        }

        @Override
        protected boolean canDeleteElement(YApiProjectConfigInfo selection) {
            return true;
        }

        protected static class TokenColumnInfo extends ElementsColumnInfoBase<YApiProjectConfigInfo> {
            protected TokenColumnInfo() {
                super("项目Token");
            }

            @Override
            public boolean isCellEditable(YApiProjectConfigInfo projectConfigInfo) {
                return true;
            }

            @Override
            public void setValue(YApiProjectConfigInfo projectConfigInfo, String value) {
                if (projectConfigInfo != null) {
                    projectConfigInfo.setToken(value);
                }
            }

            @Nullable
            @Override
            protected String getDescription(YApiProjectConfigInfo element) {
                return "";
            }

            @Nullable
            @Override
            public String valueOf(YApiProjectConfigInfo projectConfigInfo) {
                return projectConfigInfo == null ? "" : projectConfigInfo.getToken();
            }
        }

        protected static class BasePathColumnInfo extends ElementsColumnInfoBase<YApiProjectConfigInfo> {
            protected BasePathColumnInfo() {
                super("接口基本路径");
            }

            @Nullable
            @Override
            protected String getDescription(YApiProjectConfigInfo element) {
                return "自动拼接在每个接口之前";
            }

            @Override
            public boolean isCellEditable(YApiProjectConfigInfo projectConfigInfo) {
                return true;
            }

            @Override
            public void setValue(YApiProjectConfigInfo projectConfigInfo, String value) {
                if (projectConfigInfo != null) {
                    projectConfigInfo.setBasePath(value);
                }
            }

            @Nullable
            @Override
            public String valueOf(YApiProjectConfigInfo projectConfigInfo) {
                return projectConfigInfo == null ? "" : projectConfigInfo.getBasePath();
            }
        }

        protected static class PackageNameColumnInfo extends ElementsColumnInfoBase<YApiProjectConfigInfo> {
            protected PackageNameColumnInfo() {
                super("模块包名");
            }

            @Nullable
            @Override
            protected String getDescription(YApiProjectConfigInfo element) {
                return "多模块项目中模块包名";
            }

            @Override
            public boolean isCellEditable(YApiProjectConfigInfo projectConfigInfo) {
                return true;
            }

            @Override
            public void setValue(YApiProjectConfigInfo projectConfigInfo, String value) {
                if (projectConfigInfo != null) {
                    projectConfigInfo.setPackageName(value);
                }
            }

            @Nullable
            @Override
            public String valueOf(YApiProjectConfigInfo projectConfigInfo) {
                return projectConfigInfo == null ? "" : projectConfigInfo.getPackageName();
            }
        }

        protected static class ProjectIdColumnInfo extends ElementsColumnInfoBase<YApiProjectConfigInfo> {
            protected ProjectIdColumnInfo() {
                super("Project Id");
            }

            @Nullable
            @Override
            protected String getDescription(YApiProjectConfigInfo element) {
                return "YApi项目id";
            }

            @Override
            public void setValue(YApiProjectConfigInfo projectConfigInfo, String value) {
                if (projectConfigInfo != null) {
                    projectConfigInfo.setProjectId(value);
                }
            }

            @Nullable
            @Override
            public String valueOf(YApiProjectConfigInfo projectConfigInfo) {
                return projectConfigInfo == null ? "" : projectConfigInfo.getProjectId();
            }
        }

        protected static class ModuleNameColumnInfo extends ElementsColumnInfoBase<YApiProjectConfigInfo>{

            protected ModuleNameColumnInfo() {
                super("Module Name");
            }

            @Nullable
            @Override
            protected String getDescription(YApiProjectConfigInfo element) {
                return null;
            }

            @Nullable
            @Override
            public String valueOf(YApiProjectConfigInfo info) {
                return info == null ? "" : info.getModuleName();
            }

            @Override
            public void setValue(YApiProjectConfigInfo info, String value) {
                info.setModuleName(value);
            }

            @Override
            public boolean isCellEditable(YApiProjectConfigInfo info) {
                return true;
            }
        }


    }

}
