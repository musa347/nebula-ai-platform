package com.aiagent.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action to execute an AI task from the Tools menu.
 */
class ExecuteAiTaskAction : AnAction("Execute AI Task") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        
        val task = Messages.showInputDialog(
            project,
            "Enter the task you want the AI to perform:",
            "Execute AI Task",
            Messages.getQuestionIcon()
        )
        
        if (!task.isNullOrBlank()) {
            executeTask(project, task)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        e.presentation.isEnabledAndVisible = project != null
    }
    
    private fun executeTask(project: Project, task: String) {
        // Show the AI Agent tool window
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("AI Agent")
        toolWindow?.show {
        }
    }
}
