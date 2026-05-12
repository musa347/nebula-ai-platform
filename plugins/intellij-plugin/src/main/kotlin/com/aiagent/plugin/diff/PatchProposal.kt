package com.aiagent.plugin.diff

data class PatchProposal(
    val filePath: String,
    val oldContent: String,
    val newContent: String,
    val description: String = "",
    val retryAttempt: Int = 0
)
