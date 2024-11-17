package com.cloudsbay.tasker.ui

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun InitialContent() {
    Text(text = "Welcome! Please log in or sign up.")
}

@Composable
fun LoadingContent() {
    CircularProgressIndicator()
}

@Composable
fun SuccessContent(outputText: String) {
    Text(text = "Success: $outputText")
}

@Composable
fun ErrorContent(errorMessage: String) {
    Text(text = "Error: $errorMessage", color = Color.Red)
}