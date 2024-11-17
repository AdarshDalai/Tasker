package com.cloudsbay.tasker.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavigationItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
) {
    object Home: BottomNavigationItem(
        title = "Home",
        route = "home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        hasNews = false
    )

    object Profile: BottomNavigationItem(
        title = "Profile",
        route = "profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        hasNews = false
    )

    object TaskList: BottomNavigationItem(
        title = "Tasks",
        route = "tasklist",
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
        hasNews = false
    )
}