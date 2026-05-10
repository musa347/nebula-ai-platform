package com.aiagent.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

/**
 * Main panel for the AI Agent tool window.
 */
class AiAgentPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {
    
    private val taskInput = JBTextField()
    private val responseArea = JBTextArea()
    private val executeButton = JButton("Execute Task")
    private val statusLabel = JBLabel("Ready")
    
    init {
        setupUI()
        setupEventHandlers()
    }
    
    private fun setupUI() {
        responseArea.isEditable = false
        responseArea.lineWrap = true
        responseArea.wrapStyleWord = true
        
        val inputPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Task:", taskInput)
            .addComponent(executeButton)
            .panel
        
        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(statusLabel)
        }
        
        val mainPanel = JPanel(BorderLayout()).apply {
            add(inputPanel, BorderLayout.NORTH)
            add(JScrollPane(responseArea), BorderLayout.CENTER)
            add(statusPanel, BorderLayout.SOUTH)
        }
        
        setContent(mainPanel)
        toolbar = createToolbar()
    }
    
    private fun createToolbar(): JComponent {
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(5), 0))
        
        val connectButton = JButton("Connect").apply {
            addActionListener {
                // TODO: Implement connection logic
                statusLabel.text = "Connecting..."
            }
        }
        
        val clearButton = JButton("Clear").apply {
            addActionListener {
                responseArea.text = ""
                taskInput.text = ""
                statusLabel.text = "Ready"
            }
        }
        
        toolbar.add(connectButton)
        toolbar.add(clearButton)
        
        return toolbar
    }
    
    private fun setupEventHandlers() {
        executeButton.addActionListener {
            executeTask()
        }
        
        taskInput.addActionListener {
            executeTask()
        }
    }
    
    private fun executeTask() {
        val task = taskInput.text.trim()
        if (task.isEmpty()) {
            statusLabel.text = "Please enter a task"
            return
        }
        
        statusLabel.text = "Executing task..."
        executeButton.isEnabled = false
        
        // TODO: Implement actual task execution
        SwingUtilities.invokeLater {
            responseArea.append("Task: $task\n")
            responseArea.append("Status: Executing...\n\n")
            statusLabel.text = "Task executed"
            executeButton.isEnabled = true
        }
    }
}
