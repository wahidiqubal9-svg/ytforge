package com.wahidiqubal.ytforge.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wahidiqubal.ytforge.BuildConfig
import com.wahidiqubal.ytforge.data.chat.ModelCatalogClient
import com.wahidiqubal.ytforge.data.render.RenderClient
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.settings.Providers
import com.wahidiqubal.ytforge.data.update.UpdateState
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun SettingsScreen(
    settings: ApiSettings,
    updateState: UpdateState,
    onBack: () -> Unit,
    onSave: (ApiSettings) -> Unit,
    onCheckUpdates: () -> Unit,
) {
    val context = LocalContext.current
    var providerId by rememberSaveable { mutableStateOf(settings.providerId) }
    var baseUrl by rememberSaveable { mutableStateOf(settings.baseUrl) }
    var model by rememberSaveable { mutableStateOf(settings.model) }
    var apiKey by rememberSaveable { mutableStateOf(settings.apiKey) }
    var sarvamKey by rememberSaveable { mutableStateOf(settings.sarvamKey) }
    var youtubeClientId by rememberSaveable { mutableStateOf(settings.youtubeClientId) }
    var renderRepo by rememberSaveable { mutableStateOf(settings.renderRepo) }
    var renderToken by rememberSaveable { mutableStateOf(settings.renderToken) }
    var modelOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showModels by remember { mutableStateOf(false) }
    var isFetchingModels by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var videoRatio by rememberSaveable { mutableStateOf(settings.videoRatio) }
    var videoQuality by rememberSaveable { mutableStateOf(settings.videoQuality) }
    var previewFirst by rememberSaveable { mutableStateOf(settings.previewFirst) }
    var previewQuality by rememberSaveable { mutableStateOf(settings.previewQuality) }
    var maxDurationSec by rememberSaveable { mutableStateOf(settings.maxDurationSec) }
    var showLlmKey by remember { mutableStateOf(false) }
    var showSarvamKey by remember { mutableStateOf(false) }
    var showRenderToken by remember { mutableStateOf(false) }
    var isRendering by remember { mutableStateOf(false) }
    var renderStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val renderClient = remember { RenderClient(context) }
    val modelCatalog = remember { ModelCatalogClient() }

    fun fetchModels() {
        if (isFetchingModels) return
        scope.launch {
            isFetchingModels = true
            modelError = null
            val current = ApiSettings(
                providerId = providerId,
                baseUrl = baseUrl.trim(),
                model = model,
                apiKey = apiKey.trim(),
            )
            runCatching { modelCatalog.fetch(current) }
                .onSuccess { fetched ->
                    modelOptions = fetched
                    showModels = fetched.isNotEmpty()
                }
                .onFailure { failure ->
                    modelError = failure.message ?: "Could not fetch models."
                    val fallback = modelCatalog.fallback(providerId)
                    if (fallback.isNotEmpty()) {
                        modelOptions = fallback
                        showModels = true
                    }
                }
            isFetchingModels = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Ink)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Keys stay on this device", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Text(
                "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SectionCard(
                    title = "AI provider",
                    subtitle = "One key for chatting and planning. DeepSeek, OpenAI, Claude, OpenRouter, Gemini or any compatible API.",
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Providers.all) { preset ->
                            FilterChip(
                                selected = providerId == preset.id,
                                onClick = {
                                    providerId = preset.id
                                    modelOptions = emptyList()
                                    showModels = false
                                    modelError = null
                                    if (preset.id != "custom") {
                                        baseUrl = preset.baseUrl
                                        model = preset.model
                                    }
                                },
                                label = { Text(preset.name) },
                                leadingIcon = if (providerId == preset.id) {
                                    {
                                        Icon(
                                            Icons.Outlined.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor = Color(0xFF04110C),
                                    selectedLeadingIconColor = Color(0xFF04110C),
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    SecretField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = "API key",
                        visible = showLlmKey,
                        onToggleVisible = { showLlmKey = !showLlmKey },
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    )
                    Spacer(Modifier.height(10.dp))
                    Box {
                        OutlinedTextField(
                            value = model,
                            onValueChange = {
                                model = it
                                modelError = null
                            },
                            label = { Text("Model") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { fetchModels() }, enabled = !isFetchingModels) {
                                    if (isFetchingModels) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Accent,
                                        )
                                    } else {
                                        Icon(
                                            Icons.Outlined.Refresh,
                                            contentDescription = "Fetch models",
                                            tint = Accent,
                                        )
                                    }
                                }
                            },
                            supportingText = {
                                Text(
                                    modelError ?: "${Providers.byId(providerId).hint} · Tap refresh to load models.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (modelError != null) Color(0xFFFF9AA9) else Muted,
                                )
                            },
                        )
                        DropdownMenu(
                            expanded = showModels && modelOptions.isNotEmpty(),
                            onDismissRequest = { showModels = false },
                        ) {
                            modelOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        model = option
                                        showModels = false
                                        modelError = null
                                    },
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { fetchModels() },
                            enabled = !isFetchingModels && apiKey.isNotBlank(),
                        ) {
                            Text("Fetch models")
                        }
                        Spacer(Modifier.width(8.dp))
                        if (modelError != null) {
                            Text(
                                "Using suggested models",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Sarvam voiceover",
                    subtitle = "A separate Sarvam key generates the voice track. It never goes to the render service.",
                ) {
                    SecretField(
                        value = sarvamKey,
                        onValueChange = { sarvamKey = it },
                        label = "Sarvam API key",
                        visible = showSarvamKey,
                        onToggleVisible = { showSarvamKey = !showSarvamKey },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Voice and language are selected per project in Build mode.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }

            item {
                SectionCard(
                    title = "YouTube",
                    subtitle = "Uploads use Google sign-in, not an API key. Create an Android OAuth client for this app.",
                ) {
                    OutlinedTextField(
                        value = youtubeClientId,
                        onValueChange = { youtubeClientId = it },
                        label = { Text("OAuth client ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                "Sign-in setup comes with the YouTube milestone.",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }

            item {
                SectionCard(
                    title = "Video output",
                    subtitle = "Defaults for Build mode. Final output is 720p; previews are fast and lower resolution.",
                ) {
                    Text("Aspect ratio", style = MaterialTheme.typography.labelLarge, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("9:16" to "Shorts", "16:9" to "Landscape", "1:1" to "Square")) { (value, label) ->
                            FilterChip(
                                selected = videoRatio == value,
                                onClick = { videoRatio = value },
                                label = { Text("$value $label") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor = Color(0xFF04110C),
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Final quality", style = MaterialTheme.typography.labelLarge, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("720p30" to "720p · 30fps", "720p60" to "720p · 60fps")) { (value, label) ->
                            FilterChip(
                                selected = videoQuality == value,
                                onClick = { videoQuality = value },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor = Color(0xFF04110C),
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Preview before final", style = MaterialTheme.typography.labelLarge, color = Ink)
                            Text(
                                "Render a low-quality preview first, then upgrade on demand.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                        Switch(checked = previewFirst, onCheckedChange = { previewFirst = it })
                    }
                    if (previewFirst) {
                        Spacer(Modifier.height(10.dp))
                        Text("Preview quality", style = MaterialTheme.typography.labelLarge, color = Ink)
                        Spacer(Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("360p", "480p")) { value ->
                                FilterChip(
                                    selected = previewQuality == value,
                                    onClick = { previewQuality = value },
                                    label = { Text(value) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Accent,
                                        selectedLabelColor = Color(0xFF04110C),
                                    ),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Maximum length", style = MaterialTheme.typography.labelLarge, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(15, 30, 60)) { seconds ->
                            FilterChip(
                                selected = maxDurationSec == seconds,
                                onClick = { maxDurationSec = seconds },
                                label = { Text("${seconds}s") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor = Color(0xFF04110C),
                                ),
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Render backend",
                    subtitle = "Optional. Use any GitHub account: create a private repo and copy render-backend/.github/workflows/render.yml into it.",
                ) {
                    OutlinedTextField(
                        value = renderRepo,
                        onValueChange = { renderRepo = it },
                        label = { Text("Render repo (owner/repo)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                "Example: your-account/ytforge-render",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    SecretField(
                        value = renderToken,
                        onValueChange = { renderToken = it },
                        label = "Render GitHub token",
                        visible = showRenderToken,
                        onToggleVisible = { showRenderToken = !showRenderToken },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Fine-grained token: Contents Read/Write + Actions Read/Write on the render repo only. " +
                            "The token never goes to the render job.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            if (isRendering) return@OutlinedButton
                            isRendering = true
                            renderStatus = "Preparing test project…"
                            val jobId = "test-${UUID.randomUUID()}"
                            val current = ApiSettings(
                                providerId = providerId,
                                baseUrl = baseUrl.trim(),
                                model = model.trim(),
                                apiKey = apiKey.trim(),
                                sarvamKey = sarvamKey.trim(),
                                youtubeClientId = youtubeClientId.trim(),
                                githubToken = settings.githubToken,
                                renderRepo = renderRepo.trim(),
                                renderToken = renderToken.trim(),
                                videoRatio = videoRatio,
                                videoQuality = videoQuality,
                                previewFirst = previewFirst,
                                previewQuality = previewQuality,
                                maxDurationSec = maxDurationSec,
                            )
                            scope.launch {
                                runCatching {
                                    renderClient.render(current, sampleProjectZip(), jobId) { status ->
                                        renderStatus = status
                                    }
                                }.onSuccess { output ->
                                    renderStatus = "Opening test render…"
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        output.video,
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(uri, "video/mp4")
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(Intent.createChooser(intent, "Open test render"))
                                }.onFailure { error ->
                                    renderStatus = error.message ?: "Test render failed."
                                }
                                isRendering = false
                            }
                        },
                        enabled = renderRepo.isNotBlank() && renderToken.isNotBlank() && !isRendering,
                    ) {
                        Text(if (isRendering) "Rendering…" else "Test render")
                    }
                    renderStatus?.let { status ->
                        Spacer(Modifier.height(8.dp))
                        Text(status, style = MaterialTheme.typography.labelSmall, color = if (isRendering) Muted else Ink)
                    }
                }
            }

            item {
                SectionCard(
                    title = "Updates",
                    subtitle = "The APK updates from GitHub Releases. The first install is manual.",
                ) {
                    Text(
                        "Installed v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    when (val state = updateState) {
                        is UpdateState.Checking -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Checking…", style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }

                        is UpdateState.UpToDate -> Text(
                            "You're on the latest build.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Accent,
                        )

                        is UpdateState.Available -> Text(
                            "Update ${state.info.versionName} is ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Accent,
                        )

                        is UpdateState.Downloading -> Text(
                            "Downloading ${(state.progress * 100).toInt()}%…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )

                        is UpdateState.Failed -> Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9AA9),
                        )

                        else -> Text(
                            "Check for a newer build.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onCheckUpdates) {
                        Text("Check for updates")
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDeep,
                    border = BorderStroke(1.dp, OutlineDark),
                ) {
                    Text(
                        "API keys are encrypted with Android Keystore, excluded from backups, " +
                            "and never logged or sent to the render backend. Only the signing " +
                            "keystore lives in GitHub Secrets.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        onSave(
                            ApiSettings(
                                providerId = providerId,
                                baseUrl = baseUrl.trim(),
                                model = model.trim(),
                                apiKey = apiKey.trim(),
                                sarvamKey = sarvamKey.trim(),
                                youtubeClientId = youtubeClientId.trim(),
                                githubToken = settings.githubToken,
                                renderRepo = renderRepo.trim(),
                                renderToken = renderToken.trim(),
                                videoRatio = videoRatio,
                                videoQuality = videoQuality,
                                previewFirst = previewFirst,
                                previewQuality = previewQuality,
                                maxDurationSec = maxDurationSec,
                            ),
                        )
                        Toast.makeText(context, "Saved on this device", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Hide key" else "Show key",
                )
            }
        },
    )
}


private fun sampleProjectZip(): ByteArray {
    val html = """
        <!doctype html>
        <html lang="en" data-resolution="portrait">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=720, height=1280" />
          <script src="https://cdn.jsdelivr.net/npm/gsap@3.14.2/dist/gsap.min.js"></script>
          <style>
            * { margin:0; padding:0; box-sizing:border-box; }
            html, body { width:720px; height:1280px; overflow:hidden; background:#07111f; font-family:system-ui,sans-serif; }
            #root { position:relative; width:720px; height:1280px; overflow:hidden;
              background:radial-gradient(900px 700px at 80% 10%, #14395c 0%, #07111f 60%, #030812 100%); }
            .clip { position:absolute; inset:0; visibility:hidden; display:flex; align-items:center; justify-content:center; padding:72px; text-align:center; }
            h1 { color:#f7fbff; font-size:78px; line-height:.98; letter-spacing:-.04em; font-weight:800; }
            h1 span { color:#3ee6b0; }
            p { color:#8aa0b8; font-size:28px; line-height:1.35; margin-top:24px; }
            .kicker { color:#3ee6b0; font-size:22px; letter-spacing:.22em; text-transform:uppercase; font-weight:700; margin-bottom:20px; }
          </style>
        </head>
        <body>
          <div id="root" data-composition-id="main" data-start="0" data-duration="5" data-width="720" data-height="1280">
            <div id="s1" class="clip" data-start="0" data-duration="2.4" data-track-index="0">
              <div><div id="k" class="kicker">YTForge</div><h1 id="t1">Render <span>online.</span><br/>Publish anywhere.</h1></div>
            </div>
            <div id="s2" class="clip" data-start="2.4" data-duration="2.6" data-track-index="1">
              <div><h1 id="t2" style="font-size:66px">720p preview<br/><span>ready.</span></h1><p id="p2">HyperFrames on GitHub Actions.</p></div>
            </div>
          </div>
          <script>
            const tl = gsap.timeline({ paused: true });
            tl.fromTo("#k", {opacity:0,y:20},{opacity:1,y:0,duration:.4,ease:"power3.out"},0.1);
            tl.fromTo("#t1", {opacity:0,y:40},{opacity:1,y:0,duration:.6,ease:"power4.out"},0.25);
            tl.fromTo("#t2", {opacity:0,scale:.95},{opacity:1,scale:1,duration:.6,ease:"power3.out"},2.55);
            tl.fromTo("#p2", {opacity:0},{opacity:1,duration:.4},2.9);
            window.__timelines = window.__timelines || {};
            window.__timelines.main = tl;
            tl.seek(0);
          </script>
        </body>
        </html>
    """.trimIndent()
    val bytes = ByteArrayOutputStream()
    ZipOutputStream(bytes).use { zip ->
        zip.putNextEntry(ZipEntry("index.html"))
        zip.write(html.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
    return bytes.toByteArray()
}
