package site.forgus.plugins.apigeneratorplus.config;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.util.ui.ListTableModel;

/**
 * @author lmx 2021/6/5 20:15
 **/

public class YApiProjectListTableWithButtons extends ListTableWithButtons<YApiProjectConfigInfo> {
    @Override
    protected ListTableModel createListModel() {
        return null;
    }

    @Override
    protected YApiProjectConfigInfo createElement() {
        return null;
    }

    @Override
    protected boolean isEmpty(YApiProjectConfigInfo element) {
        return false;
    }

    @Override
    protected YApiProjectConfigInfo cloneElement(YApiProjectConfigInfo variable) {
        return null;
    }

    @Override
    protected boolean canDeleteElement(YApiProjectConfigInfo selection) {
        return false;
    }
}
