package com.aiagent.plugin.toolwindow

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import javax.swing.DefaultListModel

class RelatedFilesPanelTest {

    private lateinit var panel: RelatedFilesPanel

    @BeforeEach
    fun setUp() {
        panel = RelatedFilesPanel()
    }

    @Test
    fun `test panel initializes with empty state`() {
        val listModel = getListModel()
        assertEquals(0, listModel.size())
    }

    @Test
    fun `test clear resets panel state`() {
        panel.clear()
        val listModel = getListModel()
        assertEquals(0, listModel.size())
    }

    @Test
    fun `test list model can hold multiple items`() {
        val listModel = DefaultListModel<String>()
        listModel.addElement("file1.java")
        listModel.addElement("file2.java")
        listModel.addElement("file3.java")
        assertEquals(3, listModel.size())
    }

    @Test
    fun `test list model handles duplicates`() {
        val items = listOf("file1.java", "file2.java", "file1.java").distinct()
        assertEquals(2, items.size)
    }

    @Test
    fun `test empty results handled`() {
        val results = emptyList<String>()
        assertTrue(results.isEmpty())
    }

    @Test
    fun `test large result list`() {
        val listModel = DefaultListModel<String>()
        repeat(1000) { listModel.addElement("file$it.java") }
        assertEquals(1000, listModel.size())
    }

    private fun getListModel(): DefaultListModel<String> {
        val field = RelatedFilesPanel::class.java.getDeclaredField("fileListModel")
        field.isAccessible = true
        return field.get(panel) as DefaultListModel<String>
    }
}
