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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.wahidiqubal.ytforge.data.chat.AttachmentStore
import com.wahidiqubal.ytforge.data.chat.BuildProject
import com.wahidiqubal.ytforge.data.chat.ChatAttachment
import com.wahidiqubal.ytforge.data.chat.sanitized
import com.wahidiqubal.ytforge.data.render.RenderClient
import com.wahidiqubal.ytforge.data.render.RenderOutput
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.video.PromoProjectBuilder
import com.wahidiqubal.ytforge.data.video.VideoStyles
import com.wahidiqubal.ytforge.data.video.PromoSpec
import com.wahidiqubal.ytforge.data.video.PublicLinkClient
import com.wahidiqubal.ytforge.data.video.ThumbnailGenerator
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
    initialProject: BuildProject? = null,
    onProjectChange: (BuildProject) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val renderClient = remember { RenderClient(context) }
    val sarvam = remember { SarvamTtsClient() }

    val safeProject = initialProject?.sanitized()
    var headline by rememberSaveable { mutableStateOf(safeProject?.headline?.ifBlank { "Special Offer" } ?: "Special Offer") }
    var subheadline by rememberSaveable { mutableStateOf(safeProject?.subheadline.orEmpty()) }
    var code by rememberSaveable { mutableStateOf(safeProject?.code.orEmpty()) }
    var cta by rememberSaveable { mutableStateOf(safeProject?.cta.orEmpty()) }
    var narration by rememberSaveable { mutableStateOf(safeProject?.narration.orEmpty()) }
    var title by rememberSaveable { mutableStateOf(safeProject?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(safeProject?.description.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(safeProject?.tags.orEmpty()) }
    var ratio by rememberSaveable { mutableStateOf(safeProject?.ratio?.takeIf { it in setOf("9:16", "16:9", "1:1") } ?: settings.videoRatio) }
    var videoStyle by rememberSaveable { mutableStateOf(safeProject?.videoStyle ?: "kinetic") }
    var images by remember { mutableStateOf(initialImages) }
    var thumbnail by remember { mutableStateOf<File?>(null) }
    var thumbnailBusy by remember { mutableStateOf(false) }
    var thumbnailError by remember { mutableStateOf<String?>(null) }
    var thumbnailVariant by remember { mutableStateOf(0) }
    var customThumbnail by remember {
        mutableStateOf(initialProject?.thumbnailPath?.let(::File)?.takeIf { it.isFile })
    }
    var thumbnailImporting by remember { mutableStateOf(false) }
    var step by rememberSaveable { mutableStateOf(0) }
    var isBuilding by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<RenderOutput?>(null) }
    var useFinal by remember { mutableStateOf(false) }
    var publicLink by remember { mutableStateOf<String?>(null) }
    var linkBusy by remember { mutableStateOf(false) }
    var linkError by remember { mutableStateOf<String?>(null) }

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

    val thumbnailPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            thumbnailImporting = true
            thumbnailError = null
            runCatching {
                val attachment = AttachmentStore.import(context, uri, 0)
                    ?: throw IllegalStateException("Could not read that image.")
                ThumbnailGenerator.normalize(context, attachment.path)
                    ?: throw IllegalStateException("Could not prepare that thumbnail.")
            }.onSuccess { file ->
                customThumbnail = file
                safeProject?.copy(thumbnailPath = file.absolutePath)?.let(onProjectChange)
            }.onFailure {
                thumbnailError = it.message ?: "Could not import thumbnail."
            }
            thumbnailImporting = false
        }
    }

    fun generateThumbnail(nextVariant: Boolean = false) {
        if (thumbnailBusy) return
        if (nextVariant) thumbnailVariant += 1
        scope.launch {
            thumbnailBusy = true
            thumbnailError = null
            thumbnail = null
            runCatching {
                ThumbnailGenerator.generate(
                    context = context,
                    imagePath = images.firstOrNull()?.path,
                    headline = headline,
                    subheadline = subheadline,
                    code = code,
                    cta = cta,
                    variant = thumbnailVariant,
                ) ?: throw IllegalStateException("Thumbnail generator returned no file.")
            }.onSuccess { thumbnail = it }
                .onFailure { thumbnailError = it.message ?: "Could not generate thumbnail." }
            thumbnailBusy = false
        }
    }

    LaunchedEffect(step) {
        if (step == 0 || step == 5) generateThumbnail()
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
                status = "Preparing video project…"
                val spec = PromoSpec(
                    headline = headline,
                    subheadline = subheadline,
                    code = code,
                    cta = cta,
                    narration = script,
                    ratio = ratio,
                    imagePaths = images.map { it.path },
                    title = title.ifBlank { headline },
                    description = description,
                    tags = tags,
                    videoStyle = videoStyle,
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
                        Text(if (result == null) "Build video" else "Your video is ready", style = MaterialTheme.typography.titleLarge, color = Ink)
                        Text(
                            if (result == null) "Step ${step + 1} of 6 · $ratio · 720p"
                            else "Step 6 of 6 · $ratio · 720p",
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
                            Text(stepTitle(step), style = MaterialTheme.typography.headlineMedium, color = Ink)
                            Text(stepHint(step), style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }
                        when (step) {
                            0 -> {
                                item {
                                    ThumbnailPreview(
                                        thumbnail = customThumbnail ?: thumbnail,
                                        busy = thumbnailBusy || thumbnailImporting,
                                        error = thumbnailError,
                                        onRegenerate = { generateThumbnail(nextVariant = true) },
                                    )
                                    Text(
                                        if (customThumbnail != null) "Using your uploaded thumbnail"
                                        else "Generated style: ${ThumbnailGenerator.styleName(thumbnailVariant)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Muted,
                                    )
                                }
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                thumbnailPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                                )
                                            },
                                            enabled = !thumbnailImporting && !thumbnailBusy,
                                        ) {
                                            if (thumbnailImporting) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
                                            } else {
                                                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(17.dp))
                                                Spacer(Modifier.width(5.dp))
                                                Text("Use my thumbnail")
                                            }
                                        }
                                        if (customThumbnail != null) {
                                            TextButton(onClick = {
                                                customThumbnail = null
                                                safeProject?.copy(thumbnailPath = null)?.let(onProjectChange)
                                            }) { Text("Use generated") }
                                        }
                                    }
                                }
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Images", style = MaterialTheme.typography.labelLarge, color = Ink)
                                        Spacer(Modifier.weight(1f))
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
                                    Text("Aspect ratio", style = MaterialTheme.typography.labelLarge, color = Ink)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("9:16", "16:9", "1:1").forEach { value ->
                                            if (ratio == value) {
                                                Button(onClick = { ratio = value }) { Text(value) }
                                            } else {
                                                OutlinedButton(onClick = { ratio = value }) { Text(value) }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Text("Video style", style = MaterialTheme.typography.labelLarge, color = Ink)
                                    Text(VideoStyles.byId(videoStyle).description, style = MaterialTheme.typography.labelSmall, color = Muted)
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        VideoStyles.all.forEach { style ->
                                            if (videoStyle == style.id) {
                                                Button(onClick = {
                                                    videoStyle = style.id
                                                    safeProject?.copy(videoStyle = style.id)?.let(onProjectChange)
                                                }) { Text(style.name) }
                                            } else {
                                                OutlinedButton(onClick = {
                                                    videoStyle = style.id
                                                    safeProject?.copy(videoStyle = style.id)?.let(onProjectChange)
                                                }) { Text(style.name) }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                item {
                                    OutlinedTextField(
                                        value = headline,
                                        onValueChange = { headline = it },
                                        label = { Text("Headline") },
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            2 -> {
                                item {
                                    OutlinedTextField(
                                        value = subheadline,
                                        onValueChange = { subheadline = it },
                                        label = { Text("Subheadline") },
                                        maxLines = 3,
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
                            }
                            3 -> {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("${narration.length}/2500 characters · Sarvam hi-IN", style = MaterialTheme.typography.labelSmall, color = Muted)
                                        Spacer(Modifier.weight(1f))
                                        if (lastUserMessage.isNotBlank()) {
                                            TextButton(onClick = { narration = lastUserMessage }) { Text("Use last message") }
                                        }
                                    }
                                }
                                item {
                                    OutlinedTextField(
                                        value = narration,
                                        onValueChange = { narration = it },
                                        label = { Text("Narration script") },
                                        minLines = 6,
                                        maxLines = 14,
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = {
                                            Text("Duration follows this script. No fixed cap.", style = MaterialTheme.typography.labelSmall)
                                        },
                                    )
                                }
                            }
                            4 -> {
                                item {
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("YouTube title") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                item {
                                    OutlinedTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = { Text("YouTube description") },
                                        minLines = 4,
                                        maxLines = 8,
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
                            }
                            5 -> {
                                item {
                                    ThumbnailPreview(
                                        thumbnail = customThumbnail ?: thumbnail,
                                        busy = thumbnailBusy || thumbnailImporting,
                                        error = thumbnailError,
                                        onRegenerate = { generateThumbnail(nextVariant = true) },
                                    )
                                }
                                item {
                                    Text("${VideoStyles.byId(videoStyle).name} · $ratio · 720p · ${narration.length} script characters · ${images.size} image(s)", style = MaterialTheme.typography.bodyMedium, color = Ink)
                                    Text("Title: ${title.ifBlank { headline }}", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                    if (code.isNotBlank()) Text("Code: $code", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                    if (cta.isNotBlank()) Text("CTA: $cta", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                }
                            }
                        }
                        status?.let { text ->
                            item { Text(text, style = MaterialTheme.typography.bodyMedium, color = Accent) }
                        }
                        error?.let { text ->
                            item { Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF9AA9)) }
                        }
                    } else {
                        val output = result!!
                        val file = if (!useFinal && output.preview != null) output.preview else output.video
                        val aspect = when (ratio) {
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
                        val resultThumbnail = customThumbnail ?: thumbnail
                        if (resultThumbnail != null) {
                            item {
                                AsyncImage(
                                    model = resultThumbnail,
                                    contentDescription = "YouTube thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(12.dp)),
                                )
                                Text(
                                    if (customThumbnail != null) "Your thumbnail · 1280×720" else "Generated thumbnail · 1280×720",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Muted,
                                )
                            }
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
                        item {
                            OutlinedButton(
                                onClick = {
                                    if (linkBusy) return@OutlinedButton
                                    linkBusy = true
                                    linkError = null
                                    publicLink = null
                                    scope.launch {
                                        runCatching { PublicLinkClient.upload(output.video) }
                                            .onSuccess { publicLink = it }
                                            .onFailure { linkError = it.message ?: "Could not create link." }
                                        linkBusy = false
                                    }
                                },
                                enabled = !linkBusy,
                            ) {
                                if (linkBusy) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Uploading…")
                                } else {
                                    Text("Create public link")
                                }
                            }
                            Text(
                                "Optional: uploads the final MP4 to catbox.moe, a public file host.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        publicLink?.let { link ->
                            item {
                                SelectionContainer {
                                    Text(link, style = MaterialTheme.typography.bodyMedium, color = Accent)
                                }
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("YTForge video link", link))
                                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                                    },
                                ) { Text("Copy link") }
                            }
                        }
                        linkError?.let { message ->
                            item {
                                Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF9AA9))
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
                        if (step == 0) {
                            TextButton(onClick = onDismiss, enabled = !isBuilding) { Text("Cancel") }
                        } else {
                            OutlinedButton(onClick = { step-- }, enabled = !isBuilding) { Text("Back") }
                        }
                        Spacer(Modifier.weight(1f))
                        if (step < 5) {
                            Button(
                                onClick = { step++ },
                                enabled = !isBuilding && (step != 0 || !thumbnailBusy),
                            ) { Text("Next") }
                        } else {
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
                                    Text("Final Build")
                                }
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


@Composable
private fun ThumbnailPreview(
    thumbnail: File?,
    busy: Boolean,
    error: String?,
    onRegenerate: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            if (thumbnail != null) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = "Generated thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            busy -> "Generating thumbnail…"
                            error != null -> "Thumbnail failed"
                            else -> "Thumbnail"
                        },
                        color = Muted,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    OutlinedButton(onClick = onRegenerate, enabled = !busy) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
        } else {
            Text("Regenerate thumbnail")
        }
    }
    error?.let {
        Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9AA9))
    }
}

private fun stepTitle(step: Int): String = when (step) {
    0 -> "1 · Thumbnail & style"
    1 -> "2 · Headline"
    2 -> "3 · Subheadline & offer"
    3 -> "4 · Voiceover script"
    4 -> "5 · YouTube metadata"
    else -> "6 · Final review"
}

private fun stepHint(step: Int): String = when (step) {
    0 -> "Generate the thumbnail and choose the video style, then move on."
    1 -> "The main line on the video and thumbnail."
    2 -> "Supporting line, coupon code and call to action."
    3 -> "The exact script Sarvam will speak. Duration follows this."
    4 -> "Title, description and tags for YouTube."
    else -> "Everything is ready. Build the video."
}
