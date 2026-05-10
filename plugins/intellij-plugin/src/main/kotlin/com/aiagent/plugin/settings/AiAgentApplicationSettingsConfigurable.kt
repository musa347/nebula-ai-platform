package com.aiagent.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

/**
 * Application-level settings configurable for the AI Agent Platform plugin.
 */
class AiAgentApplicationSettingsConfigurable : Configurable {
    
    private var settingsComponent: SettingsComponent? = null
    
    override fun getDisplayName(): @NlsContexts.ConfigurableName String = "AI Agent Platform"
    
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsComponent()
        return settingsComponent!!.panel
    }
    
    override fun isModified(): Boolean {
        val settings = AiAgentApplicationComponent.getInstance()
        return settingsComponent!!.serverUrl != settings.state.serverUrl ||
               settingsComponent!!.orchestratorUrl != settings.state.orchestratorUrl ||
               settingsComponent!!.enableAutoConnect != settings.state.enableAutoConnect ||
               settingsComponent!!.logLevel != settings.state.logLevel
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = AiAgentApplicationComponent.getInstance()
        settings.state.serverUrl = settingsComponent!!.serverUrl
        settings.state.orchestratorUrl = settingsComponent!!.orchestratorUrl
        settings.state.enableAutoConnect = settingsComponent!!.enableAutoConnect
        settings.state.logLevel = settingsComponent!!.logLevel
    }
    
    override fun reset() {
        val settings = AiAgentApplicationComponent.getInstance()
        settingsComponent!!.serverUrl = settings.state.serverUrl
        settingsComponent!!.orchestratorUrl = settings.state.orchestratorUrl
        settingsComponent!!.enableAutoConnect = settings.state.enableAutoConnect
        settingsComponent!!.logLevel = settings.state.logLevel
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
    
    private class SettingsComponent {
        val panel: JComponent
        val serverUrl = JBTextField(30)
        val orchestratorUrl = JBTextField(30)
        val enableAutoConnect = JBCheckBox("Enable Auto Connect")
        val logLevel = JBTextField(10)
        
        init {
            panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("MCP Server URL:", serverUrl, 1, false)
                .addLabeledComponent("Agent Orchestrator URL:", orchestratorUrl, 1, false)
                .addComponent(enableAutoConnect, 1)
                .addLabeledComponent("Log Level:", logLevel, 1, false)
                .panel
        }
    }
}
