package com.example.zeromanagement.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.zeromanagement.data.Priority
import com.example.zeromanagement.data.Subtask
import com.example.zeromanagement.data.Task
import com.example.zeromanagement.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TaskViewModel = viewModel()
    val task by viewModel.getTaskById(taskId).collectAsState(initial = null)

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        EditTaskContent(
            task = task!!,
            onDismiss = onDismiss,
            onSaveTask = onSaveTask,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskContent(
    task: Task,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(task.title) }
    var fromDate by remember { mutableStateOf<Date?>(task.fromDate) }
    var dueDate by remember { mutableStateOf<Date?>(task.dueDate) }
    var description by remember { mutableStateOf(task.description) }
    var references by remember { mutableStateOf(task.references.joinToString(", ")) }
    var priority by remember { mutableStateOf(task.priority) }
    var imageUris by remember { mutableStateOf(task.imageUris.map { Uri.parse(it) }) }
    var subtasks by remember { mutableStateOf(task.subtasks) }
    var subtaskTitle by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            imageUris = uris.take(3)
        }
    )

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF242424))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit task",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { showFromDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(text = fromDate?.let { dateFormat.format(it) } ?: "From date")
                    }
                    Button(onClick = { showDueDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(text = dueDate?.let { dateFormat.format(it) } ?: "Due date")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.values().forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Select Images")
                }

                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = subtaskTitle,
                    onValueChange = { subtaskTitle = it },
                    label = { Text("Add a subtask") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (subtaskTitle.isNotBlank()) {
                            subtasks = subtasks + Subtask(id = UUID.randomUUID().toString(), title = subtaskTitle)
                            subtaskTitle = ""
                        }
                    })
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .height(150.dp) // Set a fixed height for the LazyColumn
                ) {
                    items(subtasks, key = { it.id }) { subtask ->
                        SubtaskItem(
                            subtask = subtask,
                            onCheckedChange = {
                                subtasks = subtasks.map {
                                    if (it.id == subtask.id) it.copy(isCompleted = !subtask.isCompleted) else it
                                }
                            },
                            onDelete = { subtasks = subtasks - subtask }
                        )
                    }
                }

                OutlinedTextField(
                    value = references,
                    onValueChange = { references = it },
                    label = { Text("References (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && fromDate != null && dueDate != null) {
                            onSaveTask(
                                task.copy(
                                    title = title,
                                    fromDate = fromDate!!,
                                    dueDate = dueDate!!,
                                    description = description,
                                    references = references.split(",").map { it.trim() },
                                    priority = priority,
                                    imageUris = imageUris.map { it.toString() },
                                    subtasks = subtasks
                                )
                            )
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }

        if (showFromDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showFromDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        fromDate = datePickerState.selectedDateMillis?.let { Date(it) }
                        showFromDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFromDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showDueDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDueDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dueDate = datePickerState.selectedDateMillis?.let { Date(it) }
                        showDueDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDueDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun SubtaskItem(subtask: Subtask, onCheckedChange: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = subtask.isCompleted, onCheckedChange = { onCheckedChange() })
        Text(text = subtask.title, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete subtask")
        }
    }
}
