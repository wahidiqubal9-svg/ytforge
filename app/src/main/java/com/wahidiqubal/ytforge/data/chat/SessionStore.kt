package com.wahidiqubal.ytforge.data.chat

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ChatSession(
    val messages: List<ChatMessage> = emptyList(),
    val project: BuildProject? = null,
    val pendingAsk: AskPrompt? = null,
    val mode: String = ChatMode.PLAN.name,
)

class SessionStore(context: Context) {
    private val file = File(context.filesDir, "chat_session.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(): ChatSession = runCatching {
        if (!file.isFile) ChatSession() else json.decodeFromString<ChatSession>(file.readText())
    }.getOrDefault(ChatSession())

    fun save(session: ChatSession) {
        runCatching {
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(json.encodeToString(ChatSession.serializer(), session))
            if (!temp.renameTo(file)) {
                file.writeText(temp.readText())
                temp.delete()
            }
        }
    }
}
