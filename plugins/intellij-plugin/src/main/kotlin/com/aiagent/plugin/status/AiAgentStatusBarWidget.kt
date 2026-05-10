package com.aiagent.plugin.status

import com.aiagent.plugin.AiAgentApplicationComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

/**
 * Status bar widget showing AI Agent connection status.
 */
class AiAgentStatusBarWidget(private val project: Project) : StatusBarWidget {
    
    private var presentation: StatusBarWidget.TextPresentation = StatusBarWidget.TextPresentation("AI Agent: Disconnected")
    private var isConnected = false
    
    override fun install(statusBar: StatusBar) {
        updatePresentation()
    }
    
    override fun dispose() {
        // Cleanup
    }
    
    override fun getPresentation(): StatusBarWidget.TextPresentation {
        return presentation
    }
    
    override fun ID(): String = "AiAgentStatusBar"
    
    private fun updatePresentation() {
        val status = if (isConnected) "Connected" else "Disconnected"
        val color = if (isConnected) "#00AA00" else "#AA0000"
        
        presentation = StatusBarWidget.TextPresentation(
            """
            <html>
                <body style="color: $color">
                    <b>AI Agent:</b> $status
                </body>
            </html>
            """.trimIndent()
        )
    }
    
    fun setConnected(connected: Boolean) {
        isConnected = connected
        updatePresentation()
    }
    
    companion object {
        val KEY = Key.create<AiAgentStatusBarWidget>("AiAgentStatusBar")
    }
}
