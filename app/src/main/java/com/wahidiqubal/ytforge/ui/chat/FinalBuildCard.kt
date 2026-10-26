package com.wahidiqubal.ytforge.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wahidiqubal.ytforge.data.chat.BuildProject
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard

@Composable
fun FinalBuildCard(
    project: BuildProject,
    imageCount: Int,
    onReview: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, OutlineDark),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Movie, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Project ready", style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.weight(1f))
                Text("${project.ratio} · 720p", style = MaterialTheme.typography.labelSmall, color = Accent)
            }
            Spacer(Modifier.size(6.dp))
            Text(
                project.title.ifBlank { project.headline }.ifBlank { "Untitled video" },
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
            Text(
                "${project.narration.length} script chars · $imageCount image(s)",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
            Spacer(Modifier.size(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onReview, modifier = Modifier.weight(1f)) {
                    Text("Review & Final Build")
                }
            }
            Text(
                "Edit headline, code, CTA, thumbnail, metadata — then generate.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.size(1.dp))
            Text(
                "Preview first · manual YouTube share",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7FD7FF),
            )
        }
    }
}
