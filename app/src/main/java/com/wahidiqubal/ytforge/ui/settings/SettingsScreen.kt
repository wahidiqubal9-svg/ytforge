package com.wahidiqubal.ytforge.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.settings.Providers
import com.wahidiqubal.ytforge.data.update.UpdateState
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep

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
    var showLlmKey by remember { mutableStateOf(false) }
    var showSarvamKey by remember { mutableStateOf(false) }

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
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(Providers.byId(providerId).hint, style = MaterialTheme.typography.labelSmall)
                        },
                    )
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
