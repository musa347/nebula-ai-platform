package com.aiagent.plugin.diff

import com.fasterxml.jackson.databind.ObjectMapper

object PatchEventParser {

    private val mapper = ObjectMapper()

    /**
     * Returns a PatchProposal if the event is a PATCH_GENERATED event with embedded patch data,
     * otherwise returns null.
     *
     * Expected message format (JSON inside the "message" field):
     * {
     *   "type": "PATCH_GENERATED",
     *   "message": "{\"filePath\":\"...\",\"oldContent\":\"...\",\"newContent\":\"...\",\"description\":\"...\"}"
     * }
     */
    fun parse(eventJson: String): PatchProposal? {
        return try {
            val node = mapper.readTree(eventJson)
            if (node.path("type").asText() != "PATCH_GENERATED") return null

            val message = node.path("message").asText()
            val patch = mapper.readTree(message)

            PatchProposal(
                filePath    = patch.path("filePath").asText(),
                oldContent  = patch.path("oldContent").asText(),
                newContent  = patch.path("newContent").asText(),
                description = patch.path("description").asText(""),
                retryAttempt = patch.path("retryAttempt").asInt(0)
            )
        } catch (_: Exception) {
            null
        }
    }
}
