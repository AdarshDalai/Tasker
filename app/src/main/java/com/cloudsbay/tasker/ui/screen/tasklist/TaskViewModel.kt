package com.cloudsbay.tasker.ui.screen.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudsbay.tasker.BuildConfig
import com.cloudsbay.tasker.data.Task
import com.cloudsbay.tasker.repository.TaskRepository
import com.cloudsbay.tasker.ui.state.TaskUiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

fun String.toUUID(): UUID {
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(this.toByteArray(StandardCharsets.UTF_8))
    return UUID.nameUUIDFromBytes(hash)
}

class TaskViewModel : ViewModel() {

    private val _taskUiState: MutableStateFlow<TaskUiState> = MutableStateFlow(TaskUiState.Initial)
    val taskUiState: StateFlow<TaskUiState> = _taskUiState


    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    private val repository = TaskRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    private val userId = FirebaseAuth.getInstance().currentUser?.uid?.toUUID() ?: UUID.randomUUID()


    init {
        // Start a coroutine to periodically update tasks
        viewModelScope.launch {
            while (true) {
                loadTasks() // Or loadMostPrioritizedTask() as needed
                delay(2 * 60 * 1000) // Delay for 2 minutes
            }
        }
    }

    private val defaultPrompt = """
        You are a helpful AI assistant that helps prioritize tasks.
        Please select the most urgent task from the following list based on priority and deadline:

        {tasks}

        Return only the task name of the most urgent task.
    """.trimIndent()
    private val _highestPriorityTask = MutableStateFlow<Task?>(null)
    val highestPriorityTask: StateFlow<Task?> = _highestPriorityTask.asStateFlow()


    private fun prioritizeTasksWithGemini(tasks: List<Task>, callback: (List<Task>) -> Unit) {
        _taskUiState.value = TaskUiState.Loading

        // Filter out completed tasks before prioritizing
        val pendingTasks = tasks.filter { it.status != "Complete" }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val taskDetails = pendingTasks.joinToString("\n") { task ->
                    "- Task: ${task.taskName}, Priority: ${task.priority}, Deadline: ${task.deadline}"
                }
                val prompt = defaultPrompt.replace("{tasks}", taskDetails)

                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )

                response.text?.let { responseText ->
                    val mostUrgentTaskName = responseText.trim()
                    val mostUrgentTask = tasks.find { it.taskName == mostUrgentTaskName }

                    // Update highestPriorityTask using _highestPriorityTask.value
                    _highestPriorityTask.value = mostUrgentTask

                    // You can also update the task list order if needed
                    // val prioritizedTasks = reorderTasks(tasks, mostUrgentTaskName)
                    // callback(prioritizedTasks)
                    // _taskUiState.value = TaskUiState.Success(prioritizedTasks)

                    _taskUiState.value = TaskUiState.Success(tasks) // Or prioritizedTasks
                } ?: run {
                    _taskUiState.value = TaskUiState.Error("No response from Gemini.")
                }
            } catch (e: Exception) {
                _taskUiState.value = TaskUiState.Error(e.localizedMessage ?: "Error prioritizing tasks")
            }
        }
    }


    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.saveTask(task, onSuccess = {
                loadAllTasks() // Refresh the task list after saving
            }, onFailure = {
                // Handle error
            })
        }
    }

    fun loadMostPrioritizedTask() {
        viewModelScope.launch {
            repository.getTasks(userId, onComplete = { tasks ->
                // Filter tasks by userId before prioritizing
                val userTasks = tasks.filter { it.userId == userId.toString() }
                prioritizeTasksWithGemini(userTasks) { prioritizedTasks ->
                    _tasks.value = prioritizedTasks.take(1)
                }
            }, onFailure = {
                // Handle failure
            })
        }
    }

    // Load and prioritize tasks
    fun loadTasks() {
        viewModelScope.launch {
            repository.getTasks(userId, onComplete = { tasks ->
                prioritizeTasksWithGemini(tasks) { prioritizedTasks ->
                    _tasks.value = prioritizedTasks
                }
            }, onFailure = {
                // Handle failure
            })
        }
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            repository.getTasks(userId, onComplete = { tasks ->
                // Filter tasks by userId
                _tasks.value = tasks.filter { it.userId == userId.toString() }
            }, onFailure = {
                // Handle failure
            })
        }
    }

    private fun parseGeminiResponse(responseText: String, tasks: List<Task>): List<Task> {
        val prioritizedOrder = responseText.split("\n")
        return tasks.sortedBy { task -> prioritizedOrder.indexOf(task.taskName) }
    }

    // Mark a task as complete
    fun completeTask(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = "Complete")
            repository.saveTask(updatedTask, onSuccess = {
                loadTasks() // Refresh the task list after updating
            }, onFailure = {
                // Handle error
            })
        }
    }
}