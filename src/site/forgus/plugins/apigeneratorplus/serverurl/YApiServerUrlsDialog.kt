package site.forgus.plugins.apigeneratorplus.serverurl

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import site.forgus.plugins.apigeneratorplus.config.entity.YApiServerUrlEntity
import site.forgus.plugins.apigeneratorplus.state.ApiGeneratorPlusAppState
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.table.AbstractTableModel

class YApiServerUrlsDialog(val project: Project, val yApiServerUrls: Collection<YApiServerUrlEntity>) :
        DialogWrapper(project, true, getModalityType()) {

    private val appState = ServiceManager.getService(ApiGeneratorPlusAppState::class.java)
    private val LOG = Logger.getInstance(YApiServerUrlsDialog::class.java)

    private val URL_COLUMN = 0
    private val PADDING = 30
    private val table = JBTable(ServerUrlsTableModel())

    private var nodes = buildNodes(yApiServerUrls)

    init {
        init()
        title = "Server Urls"
        updateTableWidth()
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)

    override fun createCenterPanel(): JComponent? {
        table.selectionModel = DefaultListSelectionModel()
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.intercellSpacing = JBUI.emptySize()
        table.setDefaultRenderer(Any::class.java, MyCellRenderer())

        return ToolbarDecorator.createDecorator(table)
                .setAddAction { addRemote() }
                .setRemoveAction { removeRemote() }
                .setEditAction { editRemote() }
                .setEditActionUpdater { isUrlSelected() }
                .setRemoveActionUpdater { isUrlSelected() }.disableUpDownActions().createPanel()

    }

    private fun addRemote() {
        val urlNode = getSelectedUrl()
        val dialog = YApiDefineServerUrlDialog(project)
//        val repository = getSelectedRepo()
//        val proposedName = if (repository.remotes.any { it.name == ORIGIN }) "" else ORIGIN
//        val dialog = GitDefineRemoteDialog(repository, git, proposedName, "")
        if (dialog.showAndGet()) {
            runInModalTask("Adding Remote...",
                    "Add Remote", "Couldn't add remote ${dialog.url} ") {
//                git.addRemote(repository, dialog.remoteName, dialog.remoteUrl)
                appState.addUrl(dialog.url)
            }
        }
    }

    private fun removeRemote() {
        val urlNode = getSelectedUrl()!!
        if (Messages.YES == Messages.showYesNoDialog(rootPane, "Remove server url ${urlNode.getPresentableString()}?", "Remove Server Url", Messages.getQuestionIcon())) {
            runInModalTask("Removing Remote...", "Remove Remote", "Couldn't remove remote $urlNode") {
                appState.removeUrl(urlNode.yApiServerUrlEntity)
            }
        }
    }

    private fun editRemote() {
        val urlNode = getSelectedUrl()
        val oldUrl = urlNode?.yApiServerUrlEntity?.serverUrl
//        val remoteNode = getSelectedUrl()!!
//        val remote = remoteNode.remote
//        val repository = remoteNode.repository
//        val oldName = remote.name
//        val oldUrl = getUrl(remote)
//
        val dialog = YApiDefineServerUrlDialog(project, oldUrl!!)
        if (dialog.showAndGet()) {
            val id = urlNode.yApiServerUrlEntity.id
            val newUrl = dialog.url
            if (newUrl == oldUrl) return
            runInModalTask("Changing Remote...",
                    "Change Remote", "Couldn't change remote $oldUrl to $id '$newUrl'") {
                appState.changeUrl(id, newUrl)
            }
        }
    }

//    private fun changeRemote(repo: GitRepository, oldName: String, oldUrl: String, newName: String, newUrl: String): GitCommandResult {
//        var result : GitCommandResult? = null
//        if (newName != oldName) {
//            result = git.renameRemote(repo, oldName, newName)
//            if (!result.success()) return result
//        }
//        if (newUrl != oldUrl) {
//            result = git.setRemoteUrl(repo, newName, newUrl) // NB: remote name has just been changed
//        }
//        return result!! // at least one of two has changed
//    }

    private fun updateTableWidth() {
        var maxNameWidth = 30
        var maxUrlWidth = 250
        for (node in nodes) {
            val fontMetrics = table.getFontMetrics(UIManager.getFont("Table.font").deriveFont(Font.BOLD))
            val urlWidth = fontMetrics.stringWidth(node.getPresentableString())
            if (maxUrlWidth < urlWidth) maxUrlWidth = urlWidth
        }
        maxNameWidth += PADDING + UIUtil.DEFAULT_HGAP

        table.columnModel.getColumn(URL_COLUMN).preferredWidth = maxUrlWidth

        val defaultPreferredHeight = table.rowHeight * (table.rowCount + 3)
        table.preferredScrollableViewportSize = Dimension(maxUrlWidth + UIUtil.DEFAULT_HGAP, defaultPreferredHeight)
    }

    private fun buildNodes(yApiServerUrls: Collection<YApiServerUrlEntity>): List<Node> {
        val nodes = mutableListOf<Node>()
        for (yApiServerUrl in yApiServerUrls) {
            nodes.add(UrlNode(yApiServerUrl))
        }
        return nodes;
    }

    private fun rebuildTable() {
        nodes = buildNodes(yApiServerUrls)
        (table.model as ServerUrlsTableModel).fireTableDataChanged()
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

    private fun isUrlSelected() = getSelectedUrl() != null

    private fun getSelectedUrl(): UrlNode? {
        val selectedRow = table.selectedRow
        if (selectedRow < 0) return null
        return nodes[selectedRow] as? UrlNode
    }

    private abstract class Node {
        abstract fun getPresentableString(): String
    }

    private class UrlNode(val yApiServerUrlEntity: YApiServerUrlEntity) : Node() {
        override fun toString() = yApiServerUrlEntity.serverUrl
        override fun getPresentableString() = yApiServerUrlEntity.serverUrl
    }

    private inner class ServerUrlsTableModel() : AbstractTableModel() {
        override fun getRowCount() = nodes.size

        override fun getColumnCount() = 1

        override fun getColumnName(column: Int): String {
            return "URL"
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val node = nodes[rowIndex]
            return node.getPresentableString()
        }

    }

    private inner class MyCellRenderer : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(table: JTable?, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
            if (value is UrlNode) {
                append(value.getPresentableString(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            } else if (value is String) {
                append(value)
            }
            border = null
        }
    }
}

private fun getModalityType() = if (Registry.`is`("ide.perProjectModality")) DialogWrapper.IdeModalityType.PROJECT else DialogWrapper.IdeModalityType.IDE
