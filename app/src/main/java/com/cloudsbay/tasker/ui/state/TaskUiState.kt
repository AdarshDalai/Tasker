package com.cloudsbay.tasker.ui.state

import com.cloudsbay.tasker.data.Task

sealed class TaskUiState {
    object Initial : TaskUiState()
    object Loading : TaskUiState()
    data class Success(val prioritizedTasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}