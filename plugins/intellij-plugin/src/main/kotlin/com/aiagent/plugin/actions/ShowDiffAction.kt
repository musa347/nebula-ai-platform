package com.aiagent.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * Action to show diff for AI-proposed changes.
 */
class ShowDiffAction : AnAction("Show AI Diff") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        
        Messages.showInfoMessage(
            project,
            "AI Diff functionality will be implemented in the next phase.\n\nThis will show:\n- Proposed code changes\n- Before/after comparison\n- Accept/reject options",
            "AI Diff Viewer"
        )
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        e.presentation.isEnabledAndVisible = project != null
    }
}
