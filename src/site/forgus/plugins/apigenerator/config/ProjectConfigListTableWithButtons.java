package site.forgus.plugins.apigenerator.config;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProjectConfigListTableWithButtons extends ListTableWithButtons<ProjectConfigInfo> {
    @Override
    protected ListTableModel createListModel() {
        return new ListTableModel(new TokenColumnInfo(), new PathPrefixColumnInfo());
    }

    @Override
    protected ProjectConfigInfo createElement() {
        System.out.println("hello");
        ProjectConfigInfo projectConfigInfo = new ProjectConfigInfo();
        projectConfigInfo.setToken("");
        projectConfigInfo.setPathPrefix("");
        return projectConfigInfo;
    }

    @Override
    protected boolean isEmpty(ProjectConfigInfo element) {
        return false;
    }

    @Override
    protected ProjectConfigInfo cloneElement(ProjectConfigInfo variable) {
        return null;
    }

    @Override
    protected boolean canDeleteElement(ProjectConfigInfo selection) {
        return true;
    }

    protected class TokenColumnInfo extends ElementsColumnInfoBase<ProjectConfigInfo> {
        private final DefaultTableCellRenderer myModifiedRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setEnabled(table.isEnabled() && (hasFocus || isSelected));
                int height = component.getHeight();
                System.out.println(height);
                return component;
            }
        };
        protected TokenColumnInfo() {
            super("Token");
        }

        @Override
        public TableCellRenderer getRenderer(ProjectConfigInfo element) {
            DefaultTableCellHeaderRenderer defaultTableCellHeaderRenderer = new DefaultTableCellHeaderRenderer();
            return new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    table.setRowHeight(4);
                    table.setAutoscrolls(true);
                    Component component = super.getTableCellRendererComponent(table, element.getPathPrefix(), isSelected, hasFocus, row, column);
                    component.setEnabled(table.isEnabled() && (hasFocus || isSelected));
                    int height = component.getHeight();
                    System.out.println(height);
                    return component;
                }
            };
        }

        @Nullable
        @Override
        protected String getDescription(ProjectConfigInfo element) {
            return null;
        }

        @Nullable
        @Override
        public String valueOf(ProjectConfigInfo projectConfigInfo) {
            return projectConfigInfo.getToken();
        }
    }

    protected class PathPrefixColumnInfo extends ElementsColumnInfoBase<ProjectConfigInfo>{
        protected PathPrefixColumnInfo() {
            super("Path Prefix");
        }

        @Nullable
        @Override
        protected String getDescription(ProjectConfigInfo element) {
            return null;
        }

        @Nullable
        @Override
        public String valueOf(ProjectConfigInfo projectConfigInfo) {
            return projectConfigInfo.getPathPrefix();
        }
    }


}
