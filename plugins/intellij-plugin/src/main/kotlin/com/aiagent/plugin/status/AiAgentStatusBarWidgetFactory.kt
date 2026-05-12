package com.aiagent.plugin.status

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer

/**
 * Factory for creating the AI Agent status bar widget.
 */
class AiAgentStatusBarWidgetFactory : StatusBarWidgetFactory {
    
    override fun getId(): String = "AiAgentStatusBar"
    
    override fun getDisplayName(): String = "AI Agent Status"
    
    override fun isAvailable(project: Project): Boolean = true
    
    override fun createWidget(project: Project): StatusBarWidget {
        return AiAgentStatusBarWidget(project)
    }
    
    override fun disposeWidget(widget: StatusBarWidget) {}
    
    override fun isConfigurable(): Boolean = false
}
