package com.example.zeromanagement.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zeromanagement.R
import com.example.zeromanagement.data.TaskRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DailyTaskWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TaskRepository(applicationContext)
        val tasks = repository.getAllTasks().first()
        val today = Calendar.getInstance()

        val dueTodayTasks = tasks.filter { task ->
            val dueDate = Calendar.getInstance().apply { time = task.dueDate }
            today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)
        }

        if (dueTodayTasks.isNotEmpty()) {
            showNotification("You have ${dueTodayTasks.size} tasks due today!")
        }

        val reminderTasks = tasks.filter { task ->
            task.reminderDate?.let { reminderDateValue ->
                val reminderDate = Calendar.getInstance().apply { time = reminderDateValue }
                today.get(Calendar.YEAR) == reminderDate.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == reminderDate.get(Calendar.DAY_OF_YEAR)
            } ?: false
        }

        if (reminderTasks.isNotEmpty()) {
            showNotification("You have ${reminderTasks.size} tasks with reminders today!")
        }

        return Result.success()
    }

    private fun showNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_task_channel",
                "Daily Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "daily_task_channel")
            .setContentTitle("Task Reminder")
            .setContentText(message)
            .setSmallIcon(R.drawable.zero_management_logo_fg)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
