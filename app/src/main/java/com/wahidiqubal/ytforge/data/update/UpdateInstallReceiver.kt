package com.wahidiqubal.ytforge.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_STATUS) return
        val status = intent.getIntExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE)
        if (status == android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
            confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (confirm != null) context.startActivity(confirm)
        } else if (status != android.content.pm.PackageInstaller.STATUS_SUCCESS) {
            Toast.makeText(context, message ?: "Update install failed.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val ACTION_INSTALL_STATUS = "com.wahidiqubal.ytforge.INSTALL_STATUS"
    }
}
