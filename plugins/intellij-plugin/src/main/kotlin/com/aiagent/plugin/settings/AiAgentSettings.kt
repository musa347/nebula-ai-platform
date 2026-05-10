package com.aiagent.plugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Settings for the AI Agent Platform plugin (project-level).
 */
@Service
@State(
    name = "AiAgentSettings",
    storages = [Storage("AiAgentSettings.xml")]
)
class AiAgentSettings : PersistentStateComponent<AiAgentSettings.State> {
    
    data class State(
        var enableProjectIndexing: Boolean = true,
        var allowedPaths: MutableList<String> = mutableListOf(),
        var customPrompts: MutableMap<String, String> = mutableMapOf(),
        var lastUsedTask: String = ""
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): AiAgentSettings {
            return AiAgentSettings()
        }
    }
}
