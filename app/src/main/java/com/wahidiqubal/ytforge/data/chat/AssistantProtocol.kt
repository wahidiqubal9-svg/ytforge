package com.wahidiqubal.ytforge.data.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AskOption(
    val label: String,
    val value: String = label,
)

@Serializable
data class AskPrompt(
    val question: String,
    val options: List<AskOption>,
)

@Serializable
data class BuildProject(
    val headline: String = "",
    val subheadline: String = "",
    val code: String = "",
    val cta: String = "",
    val narration: String = "",
    val title: String = "",
    val description: String = "",
    val tags: String = "",
    val ratio: String = "9:16",
    val language: String = "hi-IN",
)

data class ParsedReply(
    val displayText: String,
    val ask: AskPrompt? = null,
    val project: BuildProject? = null,
)

object AssistantProtocol {
    private val json = Json { ignoreUnknownKeys = true }
    private val askRegex = Regex("(?s)<ask>(.*?)</ask>")
    private val projectRegex = Regex("(?s)<project>(.*?)</project>")

    fun parse(content: String): ParsedReply {
        val askMatch = askRegex.find(content)
        val projectMatch = projectRegex.find(content)
        val ask = askMatch?.groupValues?.getOrNull(1)?.let(::parseAsk)
        val project = projectMatch?.groupValues?.getOrNull(1)?.let(::parseProject)
        val clean = content
            .replace(askRegex, "")
            .replace(projectRegex, "")
            .trim()
        return ParsedReply(displayText = clean, ask = ask, project = project)
    }

    private fun parseAsk(raw: String): AskPrompt? = runCatching {
        val root = json.parseToJsonElement(raw.trim()).jsonObject
        val question = root["question"]?.jsonPrimitive?.content?.trim().orEmpty()
        val options = root["options"]?.jsonArray
            ?.mapNotNull { element ->
                when (element) {
                    is JsonObject -> {
                        val label = element["label"]?.jsonPrimitive?.content?.trim().orEmpty()
                        if (label.isBlank()) null else AskOption(
                            label = label,
                            value = element["value"]?.jsonPrimitive?.content?.trim().orEmpty().ifBlank { label },
                        )
                    }
                    else -> element.jsonPrimitive.content.trim().takeIf { it.isNotBlank() }?.let { AskOption(it) }
                }
            }
            .orEmpty()
        if (question.isBlank() || options.isEmpty()) null else AskPrompt(question, options.take(8))
    }.getOrNull()

    private fun parseProject(raw: String): BuildProject? = runCatching {
        val root = json.parseToJsonElement(raw.trim()).jsonObject
        fun text(key: String): String = root[key]?.jsonPrimitive?.content?.trim().orEmpty()
        val ratio = text("ratio").takeIf { it in setOf("9:16", "16:9", "1:1") } ?: "9:16"
        val headline = TextSanitizer.short(text("headline"))
        val title = TextSanitizer.short(text("title")).ifBlank { headline }
        val narration = TextSanitizer.multiline(text("narration"))
        val project = BuildProject(
            headline = headline.ifBlank { title },
            subheadline = TextSanitizer.short(text("subheadline")),
            code = TextSanitizer.coupon(text("code")),
            cta = TextSanitizer.short(text("cta")),
            narration = narration,
            title = title,
            description = TextSanitizer.multiline(text("description")),
            tags = TextSanitizer.tags(text("tags")),
            ratio = ratio,
            language = text("language").ifBlank { "hi-IN" },
        )
        if (project.narration.isBlank()) null else project
    }.getOrNull()
}
