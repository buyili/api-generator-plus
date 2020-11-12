package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

public class ProjectConfigListTableWithButtons extends ListTableWithButtons<YApiProjectConfigInfo> {
    @Override
    protected ListTableModel createListModel() {
        return new ListTableModel(new TokenColumnInfo(),new PackageNameColumnInfo(), new ProjectIdColumnInfo(),
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


}
