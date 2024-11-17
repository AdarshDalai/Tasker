package com.cloudsbay.tasker.ui.screen.tasklist

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cloudsbay.tasker.data.Task
import com.cloudsbay.tasker.ui.screen.Authentication.AuthenticationViewModel

@Composable
fun TaskListScreen(navController: NavHostController, taskViewModel: TaskViewModel, authViewModel: AuthenticationViewModel) {
    val tasks by taskViewModel.tasks.collectAsState()
    val currentUser = authViewModel.auth.currentUser

    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            taskViewModel.loadAllTasks()
            Log.d("TaskListScreen", "Tasks loaded for user: ${currentUser.uid}")
            Log.d("TaskListScreen", "Tasks: $tasks")
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No tasks as of now!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(task, taskViewModel)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, taskViewModel: TaskViewModel) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = task.taskName, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Description: ${task.description ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Deadline: ${task.deadline}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Priority: ${task.priority}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${task.status}", style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (task.status != "Complete") {
        Row {
            Button(
                onClick = { taskViewModel.completeTask(task) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Mark as Complete")
            }
        }
    }

}