package site.forgus.plugins.apigenerator.config;

import com.intellij.ui.components.JBList;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * @author lmx 2020/11/11 13:18
 */

public class ProjectConfigListComponent extends JBList<YApiProjectConfigInfo> {


    protected class ProjectConfigListModel implements ListModel{
        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {
            return null;
        }

        @Override
        public void addListDataListener(ListDataListener l) {

        }

        @Override
        public void removeListDataListener(ListDataListener l) {

        }
    }
}
