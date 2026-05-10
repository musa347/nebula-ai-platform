package com.aiagent.plugin.toolwindow

import com.aiagent.plugin.AiAgentProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the AI Agent tool window.
 */
class AiAgentToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val aiAgentPanel = AiAgentPanel(project)
        val content = contentFactory.createContent(aiAgentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
