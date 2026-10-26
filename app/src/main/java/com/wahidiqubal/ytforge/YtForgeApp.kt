package com.wahidiqubal.ytforge

import android.app.Application
import com.wahidiqubal.ytforge.data.chat.ChatClient
import com.wahidiqubal.ytforge.data.settings.SecureSettings
import com.wahidiqubal.ytforge.data.update.UpdateChecker

class YtForgeApp : Application() {
    val secureSettings by lazy { SecureSettings(this) }
    val chatClient by lazy { ChatClient() }
    val updateChecker by lazy { UpdateChecker(this, secureSettings) }
}
