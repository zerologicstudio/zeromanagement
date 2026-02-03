package com.example.zeromanagement.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TaskRepository(context: Context) {
    private val taskDao = AppDatabase.getDatabase(context).taskDao()

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    private val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun saveTheme(theme: String) {
        sharedPreferences.edit().putString("theme", theme).apply()
    }

    fun loadTheme(): String {
        return sharedPreferences.getString("theme", "System") ?: "System"
    }
}
