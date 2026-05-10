package com.aiagent.plugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Project-level component for the AI Agent Platform plugin.
 * Manages project-specific settings and state.
 */
@State(
    name = "AiAgentProjectComponent",
    storages = [Storage("AiAgentProjectComponent.xml")]
)
class AiAgentProjectComponent(private val project: Project) : PersistentStateComponent<AiAgentProjectComponent.State> {
    
    data class State(
        var sessionId: String = "",
        var lastUsedTask: String = "",
        var enableProjectIndexing: Boolean = true,
        var allowedPaths: MutableList<String> = mutableListOf(),
        var customPrompts: MutableMap<String, String> = mutableMapOf()
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    fun getProject(): Project = project
    
    fun getSessionId(): String = state.sessionId
    
    fun setSessionId(sessionId: String) {
        state.sessionId = sessionId
    }
    
    companion object {
        fun getInstance(project: Project): AiAgentProjectComponent {
            return project.getService(AiAgentProjectComponent::class.java)
        }
    }
}
