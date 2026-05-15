package com.aiagent.plugin.toolwindow

import com.aiagent.plugin.client.McpClient
import com.aiagent.plugin.model.RelatedFile
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class RelatedFilesPanel : JPanel(BorderLayout()) {

    private val mcpClient = McpClient()
    private val searchField = JBTextField()
    private val fileListModel = DefaultListModel<String>()
    private val fileList = JBList(fileListModel)
    private val statusLabel = JLabel("Enter query to search")

    init {
        setupUI()
        setupEventHandlers()
    }

    private fun setupUI() {
        val searchPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(JLabel("Search: "), BorderLayout.WEST)
            add(searchField, BorderLayout.CENTER)
        }

        fileList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val scrollPane = JBScrollPane(fileList)

        val statusPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(statusLabel, BorderLayout.WEST)
        }

        add(searchPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(statusPanel, BorderLayout.SOUTH)
    }

    private fun setupEventHandlers() {
        searchField.addActionListener { performSearch() }

        fileList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 1) {
                    val selected = fileList.selectedValue
                    if (selected != null) {
                        println("Selected file: $selected")
                        statusLabel.text = "Selected: $selected"
                    }
                }
            }
        })
    }

    private fun performSearch() {
        val query = searchField.text.trim()
        if (query.isEmpty()) {
            statusLabel.text = "Please enter a query"
            return
        }

        statusLabel.text = "Searching..."
        fileListModel.clear()

        SwingUtilities.invokeLater {
            try {
                val searchResults = mcpClient.search(query)
                val grepResults = mcpClient.grep(query)
                val allResults = (searchResults + grepResults).distinct().sorted()

                if (allResults.isEmpty()) {
                    statusLabel.text = "No results found"
                } else {
                    allResults.forEach { fileListModel.addElement(it) }
                    statusLabel.text = "Found ${allResults.size} file(s)"
                }
            } catch (e: Exception) {
                statusLabel.text = "Error: ${e.message}"
                println("Search error: ${e.message}")
            }
        }
    }

    fun clear() {
        searchField.text = ""
        fileListModel.clear()
        statusLabel.text = "Enter query to search"
    }
}
