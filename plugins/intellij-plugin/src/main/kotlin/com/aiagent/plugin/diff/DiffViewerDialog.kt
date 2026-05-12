package com.aiagent.plugin.diff

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel

class DiffViewerDialog(
    private val project: Project,
    private val proposal: PatchProposal,
    private val onAccept: (PatchProposal) -> Unit,
    private val onReject: () -> Unit,
    private val onRetry: () -> Unit
) : DialogWrapper(project, true) {

    init {
        title = buildTitle()
        setOKButtonText("Accept")
        setCancelButtonText("Reject")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val contentFactory = DiffContentFactory.getInstance()
        val before = contentFactory.create(project, proposal.oldContent)
        val after  = contentFactory.create(project, proposal.newContent)

        val request = SimpleDiffRequest(
            "Proposed change: ${proposal.filePath}",
            before,
            after,
            "Current",
            "Proposed"
        )

        val panel = JPanel(BorderLayout())

        if (proposal.description.isNotBlank()) {
            val desc = JBLabel("<html><b>AI:</b> ${proposal.description}</html>").apply {
                border = JBUI.Borders.empty(6, 8)
            }
            panel.add(desc, BorderLayout.NORTH)
        }

        // Embed the diff component
        val diffPanel = DiffManager.getInstance().createRequestPanel(project, disposable!!, null)
        diffPanel.setRequest(request)
        panel.add(diffPanel.component, BorderLayout.CENTER)

        return panel
    }

    override fun createSouthAdditionalPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            val retryBtn = javax.swing.JButton("Retry").apply {
                addActionListener {
                    close(CANCEL_EXIT_CODE)
                    onRetry()
                }
            }
            add(retryBtn)
            if (proposal.retryAttempt > 0) {
                add(JBLabel("Attempt ${proposal.retryAttempt}").apply {
                    foreground = java.awt.Color(150, 150, 150)
                })
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        onAccept(proposal)
    }

    override fun doCancelAction() {
        super.doCancelAction()
        onReject()
    }

    private fun buildTitle(): String {
        val file = proposal.filePath.substringAfterLast("/")
        return if (proposal.retryAttempt > 0)
            "AI Patch — $file (retry ${proposal.retryAttempt})"
        else
            "AI Patch — $file"
    }

    companion object {
        fun show(
            project: Project,
            proposal: PatchProposal,
            onAccept: (PatchProposal) -> Unit,
            onReject: () -> Unit,
            onRetry: () -> Unit
        ) {
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                DiffViewerDialog(project, proposal, onAccept, onReject, onRetry).show()
            }
        }
    }
}
