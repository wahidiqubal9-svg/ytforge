package com.wahidiqubal.ytforge.data.chat

/**
 * LLMs sometimes put HTML/CSS/keyframe code into fields that are supposed to be plain text.
 * Never render that code into the video. Strip code aggressively and keep readable copy.
 */
object TextSanitizer {
    private val fencedCode = Regex("(?s)```.*?```")
    private val styleBlock = Regex("(?is)<style[^>]*>.*?</style>")
    private val scriptBlock = Regex("(?is)<script[^>]*>.*?</script>")
    private val htmlTag = Regex("(?s)<[^>]+>")
    private val cssComment = Regex("(?s)/\\*.*?\\*/")
    private val keyframes = Regex("(?s)@keyframes\\s+[^{]+\\{(?:[^{}]|\\{[^{}]*\\})*\\}")
    private val cssRule = Regex("(?s)(^|\\n)\\s*[.#@]?[A-Za-z0-9_\\-\\s,>:#.]+\\s*\\{(?:[^{}]|\\{[^{}]*\\})*\\}")
    private val cssDeclarationLine = Regex(
        "(?im)^\\s*(?:[-a-z]+\\s*:\\s*[^;{}]+;?|animation\\s*:|transform\\s*:|opacity\\s*:|@keyframes|from\\s*\\{|to\\s*\\{).*$",
    )
    private val codeLine = Regex(
        "(?im)^\\s*(?:/\\*|//|\\*|\\{|\\}|\\.?[A-Za-z0-9_-]+\\s*\\{|@keyframes|animation:|transform:|opacity:|color:|font-size:).*$",
    )

    fun short(value: String): String =
        multiline(value)
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .take(180)

    fun multiline(value: String): String {
        var text = value
            .replace("\r\n", "\n")
            .replace(fencedCode, " ")
            .replace(styleBlock, " ")
            .replace(scriptBlock, " ")
            .replace(cssComment, " ")
            .replace(keyframes, " ")
            .replace(cssRule, " ")
        text = text.lineSequence()
            .filterNot { line ->
                val trimmed = line.trim()
                trimmed.isBlank() ||
                    trimmed.endsWith("{") ||
                    trimmed.endsWith("}") ||
                    (trimmed.contains("{") && trimmed.contains("}")) ||
                    trimmed.endsWith(";") ||
                    trimmed.startsWith("/*") ||
                    trimmed.startsWith("//") ||
                    trimmed.startsWith("@keyframes") ||
                    trimmed.startsWith(".") ||
                    trimmed.contains("animation:") ||
                    trimmed.contains("transform:") ||
                    trimmed.contains("opacity:") ||
                    trimmed.contains("translate") ||
                    trimmed.contains("scale(")
            }
            .joinToString("\n")
        text = text.replace(htmlTag, " ")
        text = text.replace(Regex("[ \\t]+"), " ")
        text = text.replace(Regex("\\n{3,}"), "\n\n")
        return text.trim()
    }

    fun coupon(value: String): String {
        val cleaned = short(value)
        val match = Regex("[A-Za-z0-9][A-Za-z0-9_-]{2,19}").find(cleaned)
        return match?.value.orEmpty()
    }

    fun tags(value: String): String =
        value.split(',', '\n')
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() && !it.contains("{") && !it.contains(";") && !it.contains(":") }
            .joinToString(", ")
}

fun BuildProject.sanitized(): BuildProject {
    val headline = TextSanitizer.short(this.headline)
    val title = TextSanitizer.short(this.title).ifBlank { headline }
    return copy(
        headline = headline.ifBlank { title },
        subheadline = TextSanitizer.short(subheadline),
        code = TextSanitizer.coupon(code),
        cta = TextSanitizer.short(cta),
        narration = TextSanitizer.multiline(narration),
        title = title,
        description = TextSanitizer.multiline(description),
        tags = TextSanitizer.tags(tags),
        ratio = ratio.takeIf { it in setOf("9:16", "16:9", "1:1") } ?: "9:16",
    )
}
