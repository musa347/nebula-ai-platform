package com.aiagent.plugin.toolwindow

import com.aiagent.plugin.client.OrchestratorClient
import com.aiagent.plugin.diff.DiffViewerDialog
import com.aiagent.plugin.diff.PatchEventParser
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

class AiAgentPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {

    private val client = OrchestratorClient()
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

        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(statusLabel) }

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
                statusLabel.text = "Connecting..."
                client.connect(
                    onEvent = { msg -> handleEvent(msg) },
                    onDone  = { SwingUtilities.invokeLater { statusLabel.text = "Done"; executeButton.isEnabled = true } },
                    onError = { err -> SwingUtilities.invokeLater { statusLabel.text = "Error: $err"; executeButton.isEnabled = true } }
                )
                statusLabel.text = if (client.isConnected()) "Connected" else "Failed to connect"
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
        executeButton.addActionListener { executeTask() }
        taskInput.addActionListener { executeTask() }
    }

    private fun executeTask() {
        val task = taskInput.text.trim()
        if (task.isEmpty()) { statusLabel.text = "Please enter a task"; return }
        if (!client.isConnected()) { statusLabel.text = "Not connected — click Connect first"; return }

        statusLabel.text = "Running..."
        executeButton.isEnabled = false
        appendResponse(">>> $task\n")
        client.submitTask(task)
    }

    private fun handleEvent(eventJson: String) {
        // Try to parse as a patch proposal first
        val proposal = PatchEventParser.parse(eventJson)
        if (proposal != null) {
            appendResponse("[PATCH] ${proposal.filePath} — ${proposal.description}")
            DiffViewerDialog.show(
                project  = project,
                proposal = proposal,
                onAccept = { p ->
                    appendResponse("✅ Accepted patch for ${p.filePath}")
                    client.submitTask("APPLY_PATCH:${p.filePath}")
                },
                onReject = {
                    appendResponse("❌ Rejected patch for ${proposal.filePath}")
                    client.submitTask("REJECT_PATCH:${proposal.filePath}")
                },
                onRetry  = {
                    appendResponse("🔄 Retrying patch for ${proposal.filePath}")
                    client.submitTask("RETRY_PATCH:${proposal.filePath}")
                }
            )
            return
        }

        // All other events — append to log
        appendResponse(formatEvent(eventJson))
    }

    private fun formatEvent(eventJson: String): String {
        return try {
            val node = com.fasterxml.jackson.databind.ObjectMapper().readTree(eventJson)
            val type = node.path("type").asText()
            val msg  = node.path("message").asText()
            "[$type] $msg"
        } catch (_: Exception) {
            eventJson
        }
    }

    private fun appendResponse(text: String) {
        SwingUtilities.invokeLater { responseArea.append("$text\n") }
    }
}
