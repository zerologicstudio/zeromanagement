package com.example.zeromanagement.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.zeromanagement.Constants
import com.example.zeromanagement.MainActivity
import com.example.zeromanagement.data.Priority
import com.example.zeromanagement.data.Task
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

class TaskWidget : GlanceAppWidget() {

    private val pinnedTasksKey = stringPreferencesKey("pinned_tasks")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val tasksJson = currentState(key = pinnedTasksKey) ?: "[]"
        val pinnedTasks: List<Task> = try {
            Json.decodeFromString(ListSerializer(Task.serializer()), tasksJson)
        } catch (e: Exception) {
            emptyList()
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = Color.White, night = Color(0xFF242424)))
                .padding(16.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        ) {
            Text(
                text = "Pinned Tasks",
                style = TextStyle(color = ColorProvider(day = Color.Black, night = Color.White), fontWeight = FontWeight.Bold)
            )
            if (pinnedTasks.isEmpty()) {
                Text(
                    text = "No pinned tasks.",
                    style = TextStyle(color = ColorProvider(day = Color.Black, night = Color.White))
                )
            } else {
                LazyColumn(modifier = GlanceModifier.padding(top = 8.dp)) {
                    items(pinnedTasks) { task ->
                        TaskWidgetItem(task = task)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskWidgetItem(task: Task) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dueDateStr = dateFormat.format(task.dueDate)
    val priorityColor = when (task.priority) {
        Priority.HIGH -> Color.Red
        Priority.MEDIUM -> Color.Yellow
        Priority.LOW -> Color.Green
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra(Constants.EXTRA_TASK_ID, task.id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(ColorProvider(day = Color(0x33FFFFFF), night = Color(0x33000000)))
            .cornerRadius(12.dp)
            .padding(16.dp)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(12.dp)
                    .background(priorityColor)
                    .cornerRadius(6.dp),
                content = {}
            )
            Spacer(modifier = GlanceModifier.padding(horizontal = 8.dp))
            Column {
                Text(
                    text = task.title,
                    style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(day = Color.Black, night = Color.White))
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = dueDateStr,
                    style = TextStyle(color = ColorProvider(day = Color.DarkGray, night = Color.LightGray))
                )
            }
        }
    }
}
