package com.cloudsbay.tasker.ui.screen.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cloudsbay.tasker.data.Task
import com.cloudsbay.tasker.ui.screen.Authentication.AuthenticationViewModel
import com.cloudsbay.tasker.ui.screen.tasklist.TaskViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

fun String.toUUID(): UUID {
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(this.toByteArray(StandardCharsets.UTF_8))
    return UUID.nameUUIDFromBytes(hash)
}
@Composable
fun TaskInputForm(
    navController: NavController,
    authViewModel: AuthenticationViewModel,
    taskViewModel: TaskViewModel = viewModel()
) {
    // Define state variables for task inputs
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskDeadline by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("") }
    val highestPriorityTask by taskViewModel.highestPriorityTask.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val currentUser = authViewModel.auth.currentUser

    if (currentUser != null) {
        Log.d("TaskInputForm", "User ID: ${currentUser.uid}")
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            taskViewModel.loadMostPrioritizedTask()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Task Details", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Task Description (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = taskDeadline,
            onValueChange = { taskDeadline = it },
            label = { Text("Deadline") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = taskPriority,
            onValueChange = { taskPriority = it },
            label = { Text("Priority (High/Medium/Low)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val userId = Firebase.auth.currentUser?.uid?.toUUID()?.toString() ?: UUID.randomUUID().toString()

            // Add the task to the ViewModel
            taskViewModel.addTask(
                Task(
                    taskName = taskName,
                    description = taskDescription.ifBlank { null }, // Nullable if blank
                    deadline = taskDeadline,
                    priority = taskPriority,
                    status = "Pending",
                    userId = userId
                )
            )

            // Clear form fields after submission
            taskName = ""
            taskDescription = ""
            taskDeadline = ""
            taskPriority = ""
        }) {
            Text("Submit")
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... other composables ...

            LaunchedEffect(Unit) {
                taskViewModel.loadMostPrioritizedTask()
            }

            highestPriorityTask?.let { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Most Priority Task", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Task Name: ${task.taskName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Priority: ${task.priority}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskInputFormPreview() {
    TaskInputForm(
        navController = NavController(LocalContext.current),
        authViewModel = AuthenticationViewModel(),
        taskViewModel = viewModel()
    )
}