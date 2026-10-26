package com.wahidiqubal.ytforge.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.wahidiqubal.ytforge.data.github.GitHubDeviceAuth
import com.wahidiqubal.ytforge.data.github.GitHubDeviceCode
import com.wahidiqubal.ytforge.data.github.GitHubSetupClient
import com.wahidiqubal.ytforge.data.github.GitHubSetupResult
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import kotlinx.coroutines.launch

@Composable
fun GitHubConnectDialog(
    clientId: String,
    onConnected: (GitHubSetupResult, token: String, usedClientId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val auth = remember { GitHubDeviceAuth() }
    val setup = remember { GitHubSetupClient(context) }
    var device by remember { mutableStateOf<GitHubDeviceCode?>(null) }
    var status by remember { mutableStateOf("Preparing GitHub login…") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(true) }
    var attempt by remember { mutableStateOf(0) }

    fun openGitHub(url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    LaunchedEffect(attempt) {
        busy = true
        error = null
        if (clientId.isBlank()) {
            error = "GitHub OAuth Client ID is missing. Add it under Advanced, or rebuild with GITHUB_OAUTH_CLIENT_ID."
            busy = false
            return@LaunchedEffect
        }
        runCatching {
            status = "Requesting GitHub login code…"
            val code = auth.start(clientId)
            device = code
            status = "Opening GitHub…"
            openGitHub(code.verificationUrl)
            status = "Waiting for GitHub login / OTP…"
            val token = auth.poll(clientId, code) { message -> status = message }
            status = "Setting up private render repo…"
            val result = setup.setup(token.accessToken) { message -> status = message }
            status = "Connected."
            onConnected(result, token.accessToken, clientId)
        }.onFailure {
            error = when (it) {
                is java.net.UnknownHostException ->
                    "Could not resolve github.com. Check internet/VPN/DNS, then tap Connect again."
                else -> it.message ?: "GitHub setup failed."
            }
        }
        busy = false
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Connect GitHub") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Log in to GitHub and approve YTForge. The app will create the private render repo and install the workflow automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                device?.let { code ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            code.userCode,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Accent,
                        )
                        Spacer(Modifier.width(10.dp))
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(code.userCode))
                        }) { Text("Copy code") }
                    }
                    Text(
                        "The code is pre-filled in the GitHub page. If it is not, paste this code.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    OutlinedButton(onClick = { openGitHub(code.verificationUrl) }) {
                        Text("Open GitHub again")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Accent,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF9AA9))
                    Text(
                        "If DNS keeps failing, turn off VPN/Private DNS or set Private DNS to dns.google.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }
        },
        confirmButton = {
            if (error != null) {
                Button(onClick = {
                    device = null
                    status = "Retrying GitHub login…"
                    error = null
                    busy = true
                    attempt++
                }) { Text("Retry") }
            } else {
                Button(onClick = {}, enabled = false) { Text("Waiting…") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (error != null) "Close" else "Cancel") }
        },
    )
}
