package com.cloudsbay.tasker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

@Composable
fun EnterPrompt() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(
                value = "",
                onValueChange = { /*TODO*/ },
                label = { Text("Enter your prompt...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /*TODO*/ },
                modifier = Modifier
                    .clip(shape = CircleShape)
            ) {

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnterPromptPreview() {
    EnterPrompt()
}