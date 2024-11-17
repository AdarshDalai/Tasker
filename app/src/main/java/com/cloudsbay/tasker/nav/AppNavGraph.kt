package com.cloudsbay.tasker.nav

import android.util.Log
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cloudsbay.tasker.BottomBar
import com.cloudsbay.tasker.ui.screen.Authentication.LoginScreen
import com.cloudsbay.tasker.ui.screen.Authentication.RegisterScreen
import com.cloudsbay.tasker.ui.screen.home.TaskInputForm
import com.cloudsbay.tasker.ui.screens.profile.ProfilePage
import com.cloudsbay.tasker.ui.screen.Authentication.AuthenticationViewModel
import com.cloudsbay.tasker.ui.MainScaffoldViewModel
import com.cloudsbay.tasker.ui.screen.tasklist.TaskListScreen
import com.cloudsbay.tasker.ui.screen.tasklist.TaskViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val taskViewModel: TaskViewModel= viewModel()
    val authViewModel: AuthenticationViewModel = viewModel()
    val mainScaffoldViewModel: MainScaffoldViewModel = viewModel(LocalViewModelStoreOwner.current!!)
    val showBottomBar = mainScaffoldViewModel.showBottomBar.collectAsState().value
    val showTopBar = mainScaffoldViewModel.showTopBar.collectAsState().value

    // Get the current back stack entry to extract the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the current screen title based on the route
    val currentScreenTitle = when (currentRoute) {
        BottomNavigationItem.Home.route -> BottomNavigationItem.Home.title
        BottomNavigationItem.Profile.route -> BottomNavigationItem.Profile.title
        BottomNavigationItem.TaskList.route -> BottomNavigationItem.TaskList.title
        else -> ""
    }

    Scaffold(
        topBar = {

            if(showTopBar) {

                TopAppBar(
                    title = {
                        if (currentRoute == BottomNavigationItem.Home.route) {
                            Text(
                                text = currentScreenTitle,
                                style = TextStyle(
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            Text(text = currentScreenTitle,
                                style = TextStyle(
                                    fontSize = 35.sp,
                                )
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        LaunchedEffect(authViewModel.auth.currentUser) {
            if (authViewModel.auth.currentUser != null) { // User logged in
                // Reload tasks for the current user
                Log.d("AppNavGraph", "Reloading tasks for user: ${authViewModel.auth.currentUser?.uid}")
                taskViewModel.loadAllTasks() // Or loadMostPrioritizedTask()
                Log.d("AppNavGraph", "Tasks reloaded for user: ${taskViewModel.tasks.value}")
                Log.d("AppNavGraph", "Most prioritised task: ${taskViewModel.highestPriorityTask.value}")
            } else {
                // ... your existing logout handling ...
            }
        }
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = NavigationRoute.Login.route) {
                composable(NavigationRoute.Login.route) {
                    LoginScreen(navController, authViewModel)
                    mainScaffoldViewModel.setShowBottomBar(false)
                    mainScaffoldViewModel.setShowTopBar(false)
                }
                composable(NavigationRoute.Register.route) {
                    RegisterScreen(navController, authViewModel)
                    mainScaffoldViewModel.setShowBottomBar(false)
                    mainScaffoldViewModel.setShowTopBar(false)
                }
                composable(BottomNavigationItem.Home.route) {
                    TaskInputForm(navController, authViewModel, taskViewModel)
                    mainScaffoldViewModel.setShowBottomBar(true)
                    mainScaffoldViewModel.setShowTopBar(true)
                }
                composable(BottomNavigationItem.Profile.route) {
                    ProfilePage(navController, authViewModel)
                    mainScaffoldViewModel.setShowBottomBar(true)
                    mainScaffoldViewModel.setShowTopBar(true)
                }
                composable(BottomNavigationItem.TaskList.route) {
                    TaskListScreen(navController, taskViewModel, authViewModel)
                    mainScaffoldViewModel.setShowBottomBar(true)
                    mainScaffoldViewModel.setShowTopBar(true)
                }
            }
        }
    }
}

sealed class NavigationRoute(val route: String) {
    object Login : NavigationRoute("login")
    object Register : NavigationRoute("register")
}