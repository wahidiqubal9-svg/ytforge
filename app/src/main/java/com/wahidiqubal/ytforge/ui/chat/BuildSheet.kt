package com.wahidiqubal.ytforge.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.wahidiqubal.ytforge.data.chat.AttachmentStore
import com.wahidiqubal.ytforge.data.chat.ChatAttachment
import com.wahidiqubal.ytforge.data.render.RenderClient
import com.wahidiqubal.ytforge.data.render.RenderOutput
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.video.PromoProjectBuilder
import com.wahidiqubal.ytforge.data.video.PromoSpec
import com.wahidiqubal.ytforge.data.voice.SarvamTtsClient
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun BuildSheet(
    settings: ApiSettings,
    initialImages: List<ChatAttachment>,
    lastUserMessage: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val renderClient = remember { RenderClient(context) }
    val sarvam = remember { SarvamTtsClient() }

    var headline by rememberSaveable { mutableStateOf("Special Offer") }
    var subheadline by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var cta by rememberSaveable { mutableStateOf("") }
    var narration by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var images by remember { mutableStateOf(initialImages) }
    var isBuilding by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<RenderOutput?>(null) }
    var useFinal by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(6),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val remaining = 6 - images.size
        if (remaining <= 0) return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
            runCatching {
                uris.take(remaining).forEachIndexed { index, uri ->
                    AttachmentStore.import(context, uri, images.size + index)?.let { images = images + it }
                }
            }.onFailure { error = it.message ?: "Could not import image." }
            isImporting = false
        }
    }

    fun build() {
        if (isBuilding) return
        val script = narration.trim()
        error = when {
            script.isBlank() -> "Add the narration script."
            settings.sarvamKey.isBlank() -> "Add your Sarvam key in Settings."
            settings.renderRepo.isBlank() || settings.renderToken.isBlank() ->
                "Set the Render backend repo and token in Settings."
            else -> null
        }
        if (error != null) return
        isBuilding = true
        result = null
        scope.launch {
            runCatching {
                status = "Generating Sarvam voiceover…"
                val voice = sarvam.synthesize(settings.sarvamKey, script)
                if (voice.durationMs > settings.maxDurationSec * 1000L + 1000L) {
                    throw IllegalStateException("Voiceover is ${voice.durationMs / 1000}s, above your ${settings.maxDurationSec}s limit. Shorten the script.")
                }
                status = "Preparing video project…"
                val spec = PromoSpec(
                    headline = headline,
                    subheadline = subheadline,
                    code = code,
                    cta = cta,
                    narration = script,
                    ratio = settings.videoRatio,
                    imagePaths = images.map { it.path },
                    title = title.ifBlank { headline },
                    description = description,
                    tags = tags,
                )
                val zip = PromoProjectBuilder.build(spec, voice)
                status = "Uploading to the render backend…"
                renderClient.render(settings, zip, "build-${UUID.randomUUID()}") { message ->
                    status = message
                }
            }.onSuccess {
                result = it
                status = "Ready."
            }.onFailure {
                error = it.message ?: "Build failed."
                status = null
            }
            isBuilding = false
        }
    }

    Dialog(
        onDismissRequest = { if (!isBuilding) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isBuilding,
            dismissOnClickOutside = !isBuilding,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 760.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDeep,
            border = BorderStroke(1.dp, OutlineDark),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Movie, contentDescription = null, tint = Accent)
                    Spacer(Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Build video", style = MaterialTheme.typography.titleLarge, color = Ink)
                        Text(
                            "${settings.videoRatio} · 720p · ${settings.maxDurationSec}s max",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isBuilding) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Ink)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (result == null) {
                        item {
                            Text(
                                "Attach the image, paste the voiceover script, then build. The app generates Sarvam audio and renders on your GitHub backend.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = headline,
                                onValueChange = { headline = it },
                                label = { Text("Headline") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = subheadline,
                                onValueChange = { subheadline = it },
                                label = { Text("Subheadline") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = code,
                                    onValueChange = { code = it },
                                    label = { Text("Coupon code") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = cta,
                                    onValueChange = { cta = it },
                                    label = { Text("CTA / link") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Images", style = MaterialTheme.typography.labelLarge, color = Ink)
                                Spacer(Modifier.weight(1f))
                                if (lastUserMessage.isNotBlank()) {
                                    TextButton(onClick = { narration = lastUserMessage }) {
                                        Text("Use last message")
                                    }
                                }
                                IconButton(
                                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    enabled = !isImporting && images.size < 6,
                                ) {
                                    if (isImporting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Accent)
                                    } else {
                                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Add images", tint = Accent)
                                    }
                                }
                            }
                        }
                        if (images.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    images.forEach { attachment ->
                                        Box(modifier = Modifier.size(62.dp)) {
                                            AsyncImage(
                                                model = File(attachment.path),
                                                contentDescription = attachment.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(9.dp)),
                                            )
                                            Icon(
                                                Icons.Outlined.Close,
                                                contentDescription = "Remove image",
                                                tint = Ink,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(18.dp)
                                                    .background(Color(0xCC07111F), CircleShape)
                                                    .clickable { images = images - attachment }
                                                    .padding(2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = narration,
                                onValueChange = { narration = it },
                                label = { Text("Narration script") },
                                minLines = 5,
                                maxLines = 10,
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = {
                                    Text("${narration.length}/2500 characters · Sarvam hi-IN", style = MaterialTheme.typography.labelSmall)
                                },
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("YouTube title (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("YouTube description (optional)") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = tags,
                                onValueChange = { tags = it },
                                label = { Text("Tags (comma-separated)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            )
                        }
                        status?.let { text ->
                            item {
                                Text(text, style = MaterialTheme.typography.bodyMedium, color = Accent)
                            }
                        }
                        error?.let { text ->
                            item {
                                Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF9AA9))
                            }
                        }
                    } else {
                        val output = result!!
                        val file = if (!useFinal && output.preview != null) output.preview else output.video
                        val aspect = when (settings.videoRatio) {
                            "16:9" -> 16f / 9f
                            "1:1" -> 1f
                            else -> 9f / 16f
                        }
                        item {
                            Text("Your video is ready.", style = MaterialTheme.typography.titleLarge, color = Ink)
                        }
                        item {
                            VideoFilePlayer(file = file, aspect = aspect)
                        }
                        if (output.preview != null) {
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { useFinal = false }) {
                                        Text(if (!useFinal) "✓ Preview" else "Preview")
                                    }
                                    OutlinedButton(onClick = { useFinal = true }) {
                                        Text(if (useFinal) "✓ 720p final" else "720p final")
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                buildString {
                                    append(title.ifBlank { headline }.ifBlank { "Untitled video" })
                                    if (description.isNotBlank()) append("\n\n$description")
                                    if (tags.isNotBlank()) append("\n\n${tags.split(',').joinToString(" ") { "#" + it.trim().replace(" ", "") }}")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { shareVideo(context, file) }) {
                                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share / YouTube")
                                }
                                OutlinedButton(
                                    onClick = {
                                        copyMetadata(context, title.ifBlank { headline }, description, tags)
                                        Toast.makeText(context, "Metadata copied", Toast.LENGTH_SHORT).show()
                                    },
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy metadata")
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (result == null) {
                        OutlinedButton(onClick = onDismiss, enabled = !isBuilding) { Text("Cancel") }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = ::build, enabled = !isBuilding && !isImporting) {
                            if (isBuilding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = SurfaceDeep,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Rendering…")
                            } else {
                                Text("Build")
                            }
                        }
                    } else {
                        OutlinedButton(onClick = { onDismiss() }) { Text("Close") }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = {
                            result = null
                            status = null
                            error = null
                        }) { Text("Build another") }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoFilePlayer(file: File, aspect: Float) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoPath(file.absolutePath)
                val controller = MediaController(context)
                controller.setAnchorView(this)
                setMediaController(controller)
                setOnPreparedListener { player ->
                    player.isLooping = true
                    start()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black),
    )
}

private fun shareVideo(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("YTForge video", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, "Share video").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

private fun copyMetadata(context: Context, title: String, description: String, tags: String) {
    val text = buildString {
        appendLine("TITLE")
        appendLine(title)
        appendLine()
        appendLine("DESCRIPTION")
        appendLine(description)
        if (tags.isNotBlank()) {
            appendLine()
            appendLine("TAGS")
            append(tags)
        }
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("YouTube metadata", text))
}
