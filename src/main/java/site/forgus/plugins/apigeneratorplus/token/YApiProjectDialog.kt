package site.forgus.plugins.apigeneratorplus.token

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import site.forgus.plugins.apigeneratorplus.config.entity.YApiProjectEntity
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity
import site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.table.AbstractTableModel

public class YApiProjectDialog(val project: Project,
                               val yApiProjectEntities: Collection<YApiProjectEntity>,
                               val yApiServerUrlEntities: Collection<YApiServerUrlEntity>) :
        DialogWrapper(project, true, getModalityType()) {

    private val appState = ServiceManager.getService(ApiGeneratorPlusAppState::class.java)

    private val LOG = Logger.getInstance(YApiProjectDialog::class.java)

    private val URL_COLUMN = 0
    private val NAME_COLUMN = 1
    private val PROJECT_ID_COLUMN = 2
    private val PADDING = 1
    private val table = JBTable(ProjectsTableModel())

    private var nodes = buildNodes(yApiProjectEntities)

    init {
        init()
        title = "YApi Project"
        updateTableWidth()
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)

    override fun getPreferredFocusedComponent(): JBTable = table



    private fun updateTableWidth() {
        var maxNameWidth = 120
        var maxUrlWidth = 250
        for (node in nodes) {
            val fontMetrics = table.getFontMetrics(UIManager.getFont("Table.font").deriveFont(Font.BOLD))
            val nameWidth = fontMetrics.stringWidth(node.projectName())
            val urlWidth = fontMetrics.stringWidth(node.url)
            if (maxNameWidth < nameWidth) maxNameWidth = nameWidth
            if (maxUrlWidth < urlWidth) maxUrlWidth = urlWidth
        }
        maxNameWidth += PADDING + UIUtil.DEFAULT_HGAP

        table.columnModel.getColumn(NAME_COLUMN).preferredWidth = maxNameWidth
        table.columnModel.getColumn(URL_COLUMN).preferredWidth = maxUrlWidth
        table.columnModel.getColumn(PROJECT_ID_COLUMN).preferredWidth = maxNameWidth

        val defaultPreferredHeight = table.rowHeight * (table.rowCount + 3)
        table.preferredScrollableViewportSize = Dimension(maxNameWidth + maxUrlWidth + UIUtil.DEFAULT_HGAP, defaultPreferredHeight)
    }

    private fun buildNodes(yApiProjectEntities: Collection<YApiProjectEntity>): List<Node> {
        var nodes = mutableListOf<Node>()
        for (yApiProjectEntity in yApiProjectEntities) {
            nodes.add(Node(yApiProjectEntity))
        }
        return nodes;
    }

    override fun createCenterPanel(): JComponent? {
        table.selectionModel = DefaultListSelectionModel()
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.intercellSpacing = JBUI.emptySize()
        table.setDefaultRenderer(Any::class.java, MyCellRenderer())

        return ToolbarDecorator.createDecorator(table).
                setAddAction { addProject() }.
                setRemoveAction { removeRemote() }.
                setEditAction { editRemote() }.
                setEditActionUpdater { isRemoteSelected() }.
                setRemoveActionUpdater { isRemoteSelected() }.
                disableUpDownActions().createPanel()
    }

    private fun isRemoteSelected() = getSelectNode() != null

    private fun editRemote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun removeRemote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addProject() {
        val dialog = YApiDefineProjectDialog(project, yApiServerUrlEntities as MutableList<YApiServerUrlEntity>?, "")
        if (dialog.showAndGet()) {
            runInModalTask("Adding Remote...",
                    "Add Remote", "Couldn't add token ${dialog.token} ") {
                appState.addProject(dialog.serverUrlId, dialog.token, dialog.myProject)
            }
        }
    }


    private fun runInModalTask(title: String,
                               errorTitle: String,
                               errorMessage: String,
                               operation: () -> Unit) {
        ProgressManager.getInstance().run(object : Task.Modal(project, title, true) {
            private var result: String? = null

            override fun run(indicator: ProgressIndicator) {
                operation()
            }

            override fun onSuccess() {
                rebuildTable()
//                if (result == null) {
//                    val errorDetails = result
//                    val message = "$errorMessage :\n$errorDetails"
//                    LOG.warn(message)
//                    Messages.showErrorDialog(myProject, message, errorTitle)
//                }
            }
        })
    }

    private fun rebuildTable() {
        nodes = buildNodes(yApiProjectEntities)
        (table.model as YApiProjectDialog.ProjectsTableModel).fireTableDataChanged()
    }

    private fun getSelectNode(): Node? {
        val selectedRow = table.selectedRow
        if (selectedRow < 0) return null
        return nodes[selectedRow]
    }

    private inner class Node(val yApiProjectEntity: YApiProjectEntity) {

        public lateinit var url: String;

        init {
            for (yApiServerUrlEntity in yApiServerUrlEntities) {
                if (yApiServerUrlEntity.id.equals(yApiProjectEntity.serverUrlId)) {
                    url = yApiServerUrlEntity.serverUrl
                }
            }
        }

        override fun toString(): String {
            return yApiProjectEntity.project.name
        }

        fun projectName() = yApiProjectEntity.project.name

        fun projectId(): String = yApiProjectEntity.project._id.toString()
    }

    private inner class ProjectsTableModel() : AbstractTableModel() {
        override fun getRowCount(): Int {
            return nodes.size
        }

        override fun getColumnCount(): Int {
            return 3
        }

        override fun getColumnName(column: Int): String {
            if (column == NAME_COLUMN) return "Project Name"
            if (column == PROJECT_ID_COLUMN) return "Project ID"
            else return "Server Url"
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val node = nodes[rowIndex]
            when {
                NAME_COLUMN == columnIndex -> return node.projectName()
                URL_COLUMN == columnIndex -> return node.url
                PROJECT_ID_COLUMN == columnIndex -> return node.projectId()
                else -> {
                    LOG.error("Unexpected position at row $rowIndex and column $columnIndex")
                    return ""
                }
            }
        }

    }

    private inner class MyCellRenderer : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(table: JTable?, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
            if (value is String) {
                append(value)
            }
            border = null
        }
    }

}

private fun getModalityType() = if (Registry.`is`("ide.perProjectModality")) DialogWrapper.IdeModalityType.PROJECT else DialogWrapper.IdeModalityType.IDE
