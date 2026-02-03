package com.example.zeromanagement.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zeromanagement.BuildConfig

@Composable
fun WhatsNewDialog(onDismiss: () -> Unit) {
    val features = mapOf(
        "Home Screen Widget" to "Keep your most important tasks just a glance away.",
        "Pin Critical Tasks" to "Prioritize and pin tasks to keep them at the top of your list, and track them with the widget.",
        "Full Task Editing" to "You can now edit every detail of an existing task.",
        "Powerful Search" to "Instantly find any task with the new search bar.",
        "Custom Reminders" to "Set a specific date for a reminder on any task."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What's New in ${BuildConfig.VERSION_NAME}") },
        text = {
            Column {
                features.forEach { (title, description) ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("GOT IT!")
            }
        }
    )
}
