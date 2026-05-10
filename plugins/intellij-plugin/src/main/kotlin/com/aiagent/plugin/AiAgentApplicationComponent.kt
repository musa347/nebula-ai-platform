package com.aiagent.plugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Application-level component for the AI Agent Platform plugin.
 * Manages global plugin state and services.
 */
@State(
    name = "AiAgentApplicationComponent",
    storages = [Storage("AiAgentApplicationComponent.xml")]
)
class AiAgentApplicationComponent : PersistentStateComponent<AiAgentApplicationComponent.State> {
    
    data class State(
        var serverUrl: String = "http://localhost:8081",
        var orchestratorUrl: String = "http://localhost:8082",
        var enableAutoConnect: Boolean = true,
        var logLevel: String = "INFO"
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): AiAgentApplicationComponent {
            return ApplicationManager.getApplication().getService(AiAgentApplicationComponent::class.java)
        }
    }
}
