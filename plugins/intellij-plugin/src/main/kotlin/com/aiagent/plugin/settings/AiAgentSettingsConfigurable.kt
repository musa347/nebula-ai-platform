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
 * Project-level settings configurable for the AI Agent Platform plugin.
 */
class AiAgentSettingsConfigurable : Configurable {
    
    private var settingsComponent: SettingsComponent? = null
    
    override fun getDisplayName(): @NlsContexts.ConfigurableName String = "AI Agent Platform"
    
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsComponent()
        return settingsComponent!!.panel
    }
    
    override fun isModified(): Boolean {
        val settings = AiAgentSettings.getInstance()
        return settingsComponent!!.enableProjectIndexing != settings.enableProjectIndexing ||
               settingsComponent!!.allowedPaths != settings.allowedPaths.joinToString("\n")
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = AiAgentSettings.getInstance()
        settings.enableProjectIndexing = settingsComponent!!.enableProjectIndexing
        settings.allowedPaths = settingsComponent!!.allowedPaths.split("\n").filter { it.isNotBlank() }.toMutableList()
    }
    
    override fun reset() {
        val settings = AiAgentSettings.getInstance()
        settingsComponent!!.enableProjectIndexing = settings.enableProjectIndexing
        settingsComponent!!.allowedPaths = settings.allowedPaths.joinToString("\n")
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
    
    private class SettingsComponent {
        val panel: JComponent
        val enableProjectIndexing = JBCheckBox("Enable Project Indexing")
        val allowedPaths = JBTextField(30)
        
        init {
            panel = FormBuilder.createFormBuilder()
                .addComponent(enableProjectIndexing, 1)
                .addLabeledComponent("Allowed Paths (one per line):", allowedPaths, 1, false)
                .panel
        }
    }
}
