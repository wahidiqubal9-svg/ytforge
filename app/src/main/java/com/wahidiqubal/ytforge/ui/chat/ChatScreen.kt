package com.wahidiqubal.ytforge.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wahidiqubal.ytforge.YtForgeApp
import com.wahidiqubal.ytforge.data.chat.ChatMessage
import com.wahidiqubal.ytforge.data.chat.ChatMode
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.settings.Providers
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep
import com.wahidiqubal.ytforge.ui.theme.SurfaceRaised
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    settings: ApiSettings,
    updateAvailable: Boolean,
    onOpenSettings: () -> Unit,
    onCheckUpdates: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as YtForgeApp
    val scope = rememberCoroutineScope()
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "assistant",
                content = "Tell me the video you want. I can plan the hook, write the script, " +
                    "direct the scenes, and build a 720p preview.",
                mode = ChatMode.PLAN,
            ),
        )
    }
    var input by rememberSaveable { mutableStateOf("") }
    var modeName by rememberSaveable { mutableStateOf(ChatMode.PLAN.name) }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val mode = runCatching { ChatMode.valueOf(modeName) }.getOrDefault(ChatMode.PLAN)
    val listState = rememberLazyListState()
    val provider = Providers.byId(settings.providerId)

    LaunchedEffect(messages.size, isSending) {
        val index = if (isSending) messages.size else messages.lastIndex
        if (index >= 0) listState.animateScrollToItem(index)
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || isSending) return
        if (settings.apiKey.isBlank()) {
            error = "Add your AI provider key in Settings first."
            return
        }
        error = null
        input = ""
        messages += ChatMessage("user", text, mode)
        isSending = true
        scope.launch {
            runCatching {
                app.chatClient.complete(settings, mode, messages.toList())
            }.onSuccess { reply ->
                messages += ChatMessage("assistant", reply, mode)
            }.onFailure { failure ->
                error = failure.message ?: "The provider request failed."
            }
            isSending = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDeep)
            .statusBarsPadding(),
    ) {
        ChatTopBar(
            providerName = provider.name,
            model = settings.model,
            updateAvailable = updateAvailable,
            onOpenSettings = onOpenSettings,
        )

        if (settings.apiKey.isBlank()) {
            ConnectBanner(onOpenSettings)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (messages.size == 1) {
                item { HeroCard(onPick = { input = it }) }
            }
            items(messages) { message ->
                MessageBubble(message)
            }
            if (isSending) {
                item { ThinkingBubble(mode) }
            }
            error?.let { message ->
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2A1219),
                        border = BorderStroke(1.dp, Color(0xFF5A2433)),
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFFFF9AA9),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }

        Composer(
            input = input,
            onInputChange = { input = it },
            mode = mode,
            onModeChange = { modeName = it.name },
            isSending = isSending,
            onSend = ::send,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
private fun ChatTopBar(
    providerName: String,
    model: String,
    updateAvailable: Boolean,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Accent,
            contentColor = Color(0xFF04110C),
        ) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text("YF", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("YTForge", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                text = if (model.isBlank()) providerName else "$providerName · $model",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                maxLines = 1,
            )
        }
        if (updateAvailable) {
            Surface(
                shape = CircleShape,
                color = Accent,
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Box(modifier = Modifier.size(8.dp))
            }
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Ink)
        }
    }
}

@Composable
private fun ConnectBanner(onOpenSettings: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text(
                "Connect an AI provider to start planning.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                "Connect",
                style = MaterialTheme.typography.labelLarge,
                color = Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun HeroCard(onPick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "What are we making?",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Describe a Short, a story, or a product. Plan first — build when you're happy.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(Modifier.height(16.dp))
            listOf(
                "A 30-second explainer about UPI payments",
                "A cinematic product teaser for my app",
                "A Hindi motivation Short with bold typography",
            ).forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onPick(prompt) },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDeep,
                    border = BorderStroke(1.dp, OutlineDark),
                ) {
                    Text(
                        prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    if (message.role == "user") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp),
                color = Accent,
                contentColor = Color(0xFF04110C),
                modifier = Modifier.fillMaxWidth(0.86f),
            ) {
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "YTForge",
                    style = MaterialTheme.typography.labelMedium,
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(6.dp))
                ModeBadge(message.mode)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
        }
    }
}

@Composable
private fun ModeBadge(mode: ChatMode) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
    ) {
        Text(
            text = if (mode == ChatMode.PLAN) "Plan" else "Build",
            style = MaterialTheme.typography.labelSmall,
            color = if (mode == ChatMode.PLAN) Muted else Accent,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ThinkingBubble(mode: ChatMode) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = Accent,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (mode == ChatMode.PLAN) "Planning…" else "Building…",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
    }
}

@Composable
private fun Composer(
    input: String,
    onInputChange: (String) -> Unit,
    mode: ChatMode,
    onModeChange: (ChatMode) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, OutlineDark),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    minLines = 1,
                    maxLines = 5,
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Accent),
                    decorationBox = { inner ->
                        Box {
                            if (input.isEmpty()) {
                                Text(
                                    "Describe your video…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Muted,
                                )
                            }
                            inner()
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModeSwitch(mode = mode, onModeChange = onModeChange)
                    Spacer(Modifier.weight(1f))
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp).padding(8.dp),
                            strokeWidth = 2.dp,
                            color = Accent,
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = if (input.isBlank()) SurfaceRaised else Accent,
                            contentColor = if (input.isBlank()) Muted else Color(0xFF04110C),
                        ) {
                            IconButton(
                                onClick = onSend,
                                enabled = input.isNotBlank(),
                                modifier = Modifier.size(38.dp),
                            ) {
                                Icon(Icons.Rounded.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (mode == ChatMode.PLAN) "Plan drafts. Nothing renders yet." else "Build will render after each step.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                "Settings",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun ModeSwitch(mode: ChatMode, onModeChange: (ChatMode) -> Unit) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = SurfaceDeep,
        border = BorderStroke(1.dp, OutlineDark),
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            ModeOption(
                label = "Plan",
                selected = mode == ChatMode.PLAN,
                onClick = { onModeChange(ChatMode.PLAN) },
                icon = { Icon(Icons.Outlined.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) },
            )
            ModeOption(
                label = "Build",
                selected = mode == ChatMode.BUILD,
                onClick = { onModeChange(ChatMode.BUILD) },
                icon = { Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp)) },
            )
        }
    }
}

@Composable
private fun ModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (selected) Accent else Color.Transparent,
        contentColor = if (selected) Color(0xFF04110C) else Muted,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon()
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
