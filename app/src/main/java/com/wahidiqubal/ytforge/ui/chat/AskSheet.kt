package com.wahidiqubal.ytforge.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wahidiqubal.ytforge.data.chat.AskOption
import com.wahidiqubal.ytforge.data.chat.AskPrompt
import com.wahidiqubal.ytforge.ui.theme.Accent
import com.wahidiqubal.ytforge.ui.theme.Ink
import com.wahidiqubal.ytforge.ui.theme.Muted
import com.wahidiqubal.ytforge.ui.theme.OutlineDark
import com.wahidiqubal.ytforge.ui.theme.SurfaceCard
import com.wahidiqubal.ytforge.ui.theme.SurfaceDeep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskSheet(
    prompt: AskPrompt,
    onSelect: (AskOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDeep,
        contentColor = Ink,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(prompt.question, style = MaterialTheme.typography.titleMedium, color = Ink)
            prompt.options.forEach { option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) },
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, OutlineDark),
                ) {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    )
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Type my own answer", color = Accent)
            }
            Text(
                "The question stays in the chat. You can type instead.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}
