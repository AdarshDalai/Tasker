package com.cloudsbay.tasker

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cloudsbay.tasker.nav.BottomNavigationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavigationItem.Home,
        BottomNavigationItem.Profile,
        BottomNavigationItem.TaskList
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var firstPressed by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
// Handle back button press
    BackHandler(enabled = true) {
        // Log the current route

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        Log.d("NavBackStack", "Current Route before Back Press: $currentRoute")

        if (currentRoute == BottomNavigationItem.Home.route) {
            if (firstPressed) {
                val activity = (context as? Activity)
                activity?.finish()
            } else {
                firstPressed = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    delay(2000L)  // set delay here as wished
                    firstPressed = false
                }
            }
        } else {

            if (
                currentRoute in listOf(
                    BottomNavigationItem.Home.route,
                    BottomNavigationItem.Profile.route,
                    BottomNavigationItem.TaskList.route
                )) {
                if (currentRoute != BottomNavigationItem.Home.route) {
                    navController.navigate(BottomNavigationItem.Home.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    // Log the new current destination after navigating to Home
                    Log.d("NavBackStack", "Navigated to: ${BottomNavigationItem.Home.route}")
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    BottomAppBar {
        screens.forEach { screen ->
            AddItem(screen = screen, currentDestination = currentDestination, navController = navController)
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomNavigationItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val isSelected = currentDestination?.route == screen.route

    NavigationBarItem(
        icon = {
            if (screen.badgeCount != null && screen.badgeCount > 0) {
                BadgedBox(badge = { Badge { Text(text = screen.badgeCount.toString()) } }) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.route
                    )
                }
            } else {
                Icon(
                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                    contentDescription = screen.route
                )
            }
        },
        label = { Text(text = screen.route.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) },
        selected = isSelected,
        onClick = {
            if (!isSelected) {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                // Log the new current destination after navigation
                Log.d("NavBackStack", "Navigated to: ${screen.route}")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    BottomBar(navController = NavHostController(LocalContext.current))
}