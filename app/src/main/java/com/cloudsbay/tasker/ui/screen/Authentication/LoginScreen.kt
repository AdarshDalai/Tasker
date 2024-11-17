package com.cloudsbay.tasker.ui.screen.Authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cloudsbay.tasker.nav.BottomNavigationItem
import com.cloudsbay.tasker.nav.NavigationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthenticationViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Tasker", fontSize = 50.sp)
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .padding(30.dp)

                    .clip(RoundedCornerShape(30.dp))
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Login", fontSize = 40.sp)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginCard(navController, viewModel)
                    LaunchedEffect(key1 = authState) {
                        if (authState is AuthenticationViewModel.AuthState.Authenticated) {
                            navController.navigate(BottomNavigationItem.Home.route) {
                                popUpTo(NavigationRoute.Login.route) { inclusive = true }
                            }
                            viewModel.isLoggedIn = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginCard(navController: NavHostController, viewModel: AuthenticationViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(400.dp),
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* Handle forgot password logic */ }) {
                    Text("Forgot Password?")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                showError = false
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email or password cannot be empty"
                    showError = true
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Please enter a valid email address"
                    showError = true
                    return@Button
                }
                viewModel.loginWithEmailAndPassword(email, password)
            }) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate(NavigationRoute.Register.route) }) {
                Text("Don't have an account? Register")
            }

            if (showError) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            when (authState) {
                is AuthenticationViewModel.AuthState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is AuthenticationViewModel.AuthState.Error -> {
                    val error = authState as AuthenticationViewModel.AuthState.Error
                    Text(text = error.message, color = Color.Red, modifier = Modifier.padding(8.dp))
                }
                else -> { /* Do nothing */ }
            }
        }
    }
}
