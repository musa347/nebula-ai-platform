package com.aiagent.plugin.actions

import com.aiagent.plugin.diff.DiffViewerDialog
import com.aiagent.plugin.diff.PatchProposal
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager

class ShowDiffAction : AnAction("Show AI Diff") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val editor  = e.getData(CommonDataKeys.EDITOR) ?: return
        val file    = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        val currentContent = editor.document.text
        val proposal = PatchProposal(
            filePath    = file.path,
            oldContent  = currentContent,
            newContent  = currentContent, // placeholder — real patch comes from orchestrator
            description = "Preview: no pending AI patch for this file"
        )

        DiffViewerDialog.show(
            project  = project,
            proposal = proposal,
            onAccept = {},
            onReject = {},
            onRetry  = {}
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            e.getData(CommonDataKeys.PROJECT) != null && e.getData(CommonDataKeys.EDITOR) != null
    }
}
