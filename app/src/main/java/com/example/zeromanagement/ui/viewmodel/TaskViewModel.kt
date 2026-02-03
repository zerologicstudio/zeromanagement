package com.example.zeromanagement.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zeromanagement.data.Subtask
import com.example.zeromanagement.data.Task
import com.example.zeromanagement.data.TaskRepository
import com.example.zeromanagement.ui.widgets.TaskWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _theme = MutableStateFlow("System")
    val theme: StateFlow<String> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _tasks.value = tasks
                updateWidget()
            }
        }
        viewModelScope.launch {
            _theme.value = withContext(Dispatchers.IO) { repository.loadTheme() }
        }
    }

    private suspend fun updateWidget() {
        val context = getApplication<Application>().applicationContext
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(TaskWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                val pinnedTasks = tasks.value.filter { it.isPinned }
                prefs[stringPreferencesKey("pinned_tasks")] = Json.encodeToString(ListSerializer(Task.serializer()), pinnedTasks)
            }
            TaskWidget().update(context, glanceId)
        }
    }

    fun getTaskById(id: String): Flow<Task?> = repository.getTaskById(id)

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun onTaskCompletedChange(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.updateTask(updatedTask)
        }
    }

    fun onSubtaskCompletedChange(subtask: Subtask, isCompleted: Boolean) {
        viewModelScope.launch {
            tasks.value.find { task -> task.subtasks.any { it.id == subtask.id} }?.let { task ->
                val updatedSubtasks = task.subtasks.map {
                    if (it.id == subtask.id) {
                        it.copy(isCompleted = isCompleted)
                    } else {
                        it
                    }
                }
                val updatedTask = task.copy(subtasks = updatedSubtasks)
                repository.updateTask(updatedTask)
            }
        }
    }

    fun togglePin(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isPinned = !task.isPinned)
            repository.updateTask(updatedTask)
        }
    }

    fun changeTheme() {
        viewModelScope.launch {
            val newTheme = when (_theme.value) {
                "Light" -> "Dark"
                "Dark" -> "System"
                else -> "Light"
            }
            _theme.value = newTheme
            withContext(Dispatchers.IO) { repository.saveTheme(newTheme) }
        }
    }
}
