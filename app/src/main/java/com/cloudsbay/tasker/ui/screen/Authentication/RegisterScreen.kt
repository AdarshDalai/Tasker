package com.cloudsbay.tasker.ui.screen.Authentication

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cloudsbay.tasker.nav.NavigationRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController,
                   viewModel: AuthenticationViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Tasker", fontSize = 50.sp)
                }
            )
        }
    ) { innerPadding ->
        val authState by viewModel.authState.collectAsState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(30.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row {
                Text(text = "Register", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RegisterCard(navController, viewModel)
                LaunchedEffect(key1 = authState) {
                    if (authState is AuthenticationViewModel.AuthState.Authenticated) {
                        navController.navigate(NavigationRoute.Login.route) { // Navigate to LoginScreen
                            popUpTo(NavigationRoute.Register.route) { inclusive = true } // Clear registration screen from back stack
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun RegisterCard(navController: NavHostController, viewModel: AuthenticationViewModel = viewModel()) {
    var email = remember { mutableStateOf("") }
    var phoneNumber = remember { mutableStateOf("") }
    var password = remember { mutableStateOf("") }
    var username = remember { mutableStateOf("") }
    var name = remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPhoneNumberValid by remember { mutableStateOf(true) }
    var selectedCountryCode = remember { mutableStateOf("+1") } // Default to US
    var expanded by remember { mutableStateOf(false) }
    val countryCodes = listOf("1", "44", "91", /* Add more country codes */)
    var showError by remember {
        mutableStateOf(false)
    }
    var errorMessage by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(500.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    email.value = it
                    isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                },
                label = { Text("Email") },
                isError = !isEmailValid, // Highlight if invalid
                modifier = Modifier.fillMaxWidth()
            )
            if (!isEmailValid) {
                Text(
                    text = "Invalid email format",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Full Name") },modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCountryCode.value,
                    onValueChange = { selectedCountryCode.value = it },
                    label = { Text("Code") },
                    modifier = Modifier.width(100.dp),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, "Country Code")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    countryCodes.forEach { code ->
                        DropdownMenuItem(text = { Text(text = "+$code") },
                            onClick = {
                                code.also { selectedCountryCode.value = it }
                                expanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = phoneNumber.value,
                    onValueChange = { phoneNumber.value = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.weight(1f) // Fill remaining space
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (showError) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(onClick = {
                showError = false // Reset error state

                // 1. Validate input
                if (email.value.isBlank() || phoneNumber.value.isBlank() || password.value.isBlank() || username.value.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    showError = true
                    return@Button
                }
                if (password.value.length < 6) {
                    errorMessage = "Password must be at least 6 characters long"
                    showError = true
                    return@Button
                }
                if (!isEmailValid || !isPhoneNumberValid /* ... other validations */) {
                    // Don't proceed, fields are highlighted
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                    errorMessage = "Please enter a valid email address"
                    showError = true
                    return@Button
                }
                if (phoneNumber.value.length < 10) {
                    errorMessage = "Please enter a valid phone number"
                    showError = true
                    return@Button
                }
                viewModel.registerWithData(
                    email = email.value,
                    password = password.value,
                    selectedCountryCode = selectedCountryCode.value,
                    phoneNumber = phoneNumber.value,
                    username = username.value,
                    name =  name.value
                )
            }) {
                Text("Register")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                TextButton(onClick = { navController.navigate(NavigationRoute.Login.route){
                    popUpTo(NavigationRoute.Login.route){
                        inclusive = true
                    }
                } }) {
                    Text("Already have an account? Login")
                }
            }

        }
    }
}
