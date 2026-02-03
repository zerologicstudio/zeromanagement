package com.example.zeromanagement

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zeromanagement.ui.components.WhatsNewDialog
import com.example.zeromanagement.ui.screens.EditTaskScreen
import com.example.zeromanagement.ui.screens.HomeScreen
import com.example.zeromanagement.ui.screens.TaskCreatorScreen
import com.example.zeromanagement.ui.screens.TaskDetailScreen
import com.example.zeromanagement.ui.theme.ZeroManagementTheme
import com.example.zeromanagement.ui.viewmodel.TaskViewModel
import com.example.zeromanagement.workers.DailyTaskWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val workRequest = PeriodicWorkRequestBuilder<DailyTaskWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)

        setContent {
            val viewModel: TaskViewModel = viewModel()
            val theme by viewModel.theme.collectAsState()
            val startDestination = intent.getStringExtra(Constants.EXTRA_TASK_ID)?.let {
                "taskDetail/$it"
            } ?: "home"

            val prefs = remember { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            val lastVersion = remember { prefs.getString("last_version", "") }
            val currentVersion = try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                ""
            }
            var showWhatsNewDialog by remember { mutableStateOf(lastVersion != currentVersion) }

            ZeroManagementTheme(darkTheme = when (theme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showWhatsNewDialog) {
                        WhatsNewDialog(onDismiss = {
                            showWhatsNewDialog = false
                            prefs.edit().putString("last_version", currentVersion).apply()
                        })
                    }
                    TaskManagementApp(viewModel, startDestination)
                }
            }
        }
    }
}

@Composable
fun TaskManagementApp(
    viewModel: TaskViewModel = viewModel(),
    startDestination: String = "home"
) {
    val navController = rememberNavController()
    val tasks by viewModel.tasks.collectAsState()

    NavHost(
        navController = navController, 
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } }
    ) {
        composable("home") {
            HomeScreen(
                tasks = tasks,
                onTaskClick = { task -> navController.navigate("taskDetail/${task.id}") },
                onFabClick = { navController.navigate("taskCreator") },
                onTaskDelete = { task -> viewModel.deleteTask(task) },
                onTaskCompletedChange = { task, isCompleted -> viewModel.onTaskCompletedChange(task, isCompleted) },
                onChangeTheme = { viewModel.changeTheme() }
            )
        }
        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                TaskDetailScreen(
                    taskId = taskId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate("editTask/$taskId") }
                )
            }
        }
        composable("taskCreator") {
            TaskCreatorScreen(
                onDismiss = { navController.popBackStack() },
                onCreateTask = { task ->
                    viewModel.addTask(task)
                    navController.popBackStack()
                }
            )
        }
        composable("editTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                EditTaskScreen(
                    taskId = taskId,
                    onDismiss = { navController.popBackStack() },
                    onSaveTask = { updatedTask ->
                        viewModel.saveTask(updatedTask)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
