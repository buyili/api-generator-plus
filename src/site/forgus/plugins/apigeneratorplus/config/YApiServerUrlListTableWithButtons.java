package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity;

/**
 * @author lmx 2021/6/6 11:35
 **/

public class YApiServerUrlListTableWithButtons extends ListTableWithButtons<YApiServerUrlEntity> {
    @Override
    protected ListTableModel createListModel() {
        return new ListTableModel(new ServerUrlColumnInfo());
    }

    @Override
    protected YApiServerUrlEntity createElement() {
        return new YApiServerUrlEntity();
    }



    @Override
    protected boolean isEmpty(YApiServerUrlEntity element) {
        return element.getServerUrl() == null || "".equals(element.getServerUrl());
    }

    @Override
    protected YApiServerUrlEntity cloneElement(YApiServerUrlEntity variable) {
        return variable == null ? null : variable.clone();
    }

    @Override
    protected boolean canDeleteElement(YApiServerUrlEntity selection) {
        return true;
    }

    protected class ServerUrlColumnInfo extends ElementsColumnInfoBase<YApiServerUrlEntity>{

        protected ServerUrlColumnInfo() {
            super("Server Url");
        }

        @Override
        public boolean isCellEditable(YApiServerUrlEntity yApiServerUrlEntity) {
            return false;
        }

        @Nullable
        @Override
        protected String getDescription(YApiServerUrlEntity element) {
            return null;
        }

        @Nullable
        @Override
        public String valueOf(YApiServerUrlEntity yApiServerUrlEntity) {
            return yApiServerUrlEntity == null ? "" : yApiServerUrlEntity.getServerUrl();
        }
    }
}
