package com.wahidiqubal.ytforge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wahidiqubal.ytforge.YtForgeApp
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.update.UpdateInfo
import com.wahidiqubal.ytforge.data.update.UpdateState
import com.wahidiqubal.ytforge.ui.chat.ChatScreen
import com.wahidiqubal.ytforge.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as YtForgeApp
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf("chat") }
    var settings by remember { mutableStateOf(app.secureSettings.load()) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }

    fun checkUpdates(showUpToDate: Boolean) {
        if (updateState is UpdateState.Checking || updateState is UpdateState.Downloading) return
        scope.launch {
            updateState = UpdateState.Checking
            val info = app.updateChecker.check()
            updateState = when {
                info != null -> UpdateState.Available(info)
                showUpToDate -> UpdateState.UpToDate
                else -> UpdateState.Idle
            }
        }
    }

    LaunchedEffect(Unit) { checkUpdates(showUpToDate = false) }

    BackHandler(enabled = screen != "chat") { screen = "chat" }

    when (screen) {
        "settings" -> SettingsScreen(
            settings = settings,
            updateState = updateState,
            onBack = { screen = "chat" },
            onSave = { updated ->
                app.secureSettings.save(updated)
                settings = updated
                screen = "chat"
            },
            onPersist = { updated ->
                app.secureSettings.save(updated)
                settings = updated
            },
            onCheckUpdates = { checkUpdates(showUpToDate = true) },
        )

        else -> ChatScreen(
            settings = settings,
            updateAvailable = updateState is UpdateState.Available,
            onOpenSettings = { screen = "settings" },
            onCheckUpdates = { checkUpdates(showUpToDate = true) },
        )
    }

    val available = updateState as? UpdateState.Available
    if (available != null) {
        UpdateAvailableDialog(
            info = available.info,
            onUpdate = {
                val info = available.info
                if (!app.updateChecker.canInstallPackages()) {
                    updateState = UpdateState.NeedsInstallPermission
                    app.updateChecker.openInstallPermissionSettings()
                    return@UpdateAvailableDialog
                }
                scope.launch {
                    updateState = UpdateState.Downloading(0f)
                    runCatching {
                        app.updateChecker.download(info) { progress ->
                            updateState = UpdateState.Downloading(progress)
                        }
                    }.onSuccess { file ->
                        updateState = UpdateState.Idle
                        app.updateChecker.install(file)
                    }.onFailure { error ->
                        updateState = UpdateState.Failed(error.message ?: "Update failed.")
                    }
                }
            },
            onDismiss = { updateState = UpdateState.Idle },
        )
    }

    when (val state = updateState) {
        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Downloading update") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Keep the app open while the APK downloads.")
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${(state.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                }
            },
            confirmButton = {},
        )

        is UpdateState.NeedsInstallPermission -> AlertDialog(
            onDismissRequest = { updateState = UpdateState.Idle },
            title = { Text("Allow app installs") },
            text = { Text("Android needs permission to install updates from YTForge. Enable it, then check for updates again.") },
            confirmButton = {
                TextButton(onClick = {
                    app.updateChecker.openInstallPermissionSettings()
                    updateState = UpdateState.Idle
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { updateState = UpdateState.Idle }) { Text("Later") }
            },
        )

        is UpdateState.Failed -> AlertDialog(
            onDismissRequest = { updateState = UpdateState.Idle },
            title = { Text("Update failed") },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = { updateState = UpdateState.Idle }) { Text("OK") }
            },
        )

        else -> Unit
    }
}

@Composable
private fun UpdateAvailableDialog(
    info: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update ${info.versionName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A newer YTForge build is available.")
                if (info.changelog.isNotBlank()) {
                    Text(info.changelog, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Installed:", style = MaterialTheme.typography.labelMedium)
                    Text(
                        com.wahidiqubal.ytforge.BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdate) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        },
    )
}
