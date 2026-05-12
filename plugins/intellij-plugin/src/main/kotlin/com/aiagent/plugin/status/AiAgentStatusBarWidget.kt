package com.aiagent.plugin.status

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class AiAgentStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    private var statusText = "AI Agent: Disconnected"
    private var statusBar: StatusBar? = null

    override fun ID(): String = "AiAgentStatusBar"

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {}

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    // TextPresentation
    override fun getText(): String = statusText
    override fun getTooltipText(): String = "AI Agent connection status"
    override fun getClickConsumer(): Consumer<MouseEvent>? = null
    override fun getAlignment(): Float = 0f

    fun setConnected(connected: Boolean) {
        statusText = if (connected) "AI Agent: Connected" else "AI Agent: Disconnected"
        statusBar?.updateWidget(ID())
    }

    companion object {
        val KEY: Key<AiAgentStatusBarWidget> = Key.create("AiAgentStatusBar")
    }
}
