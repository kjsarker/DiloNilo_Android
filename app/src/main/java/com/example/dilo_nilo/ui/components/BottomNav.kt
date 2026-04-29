package com.example.dilo_nilo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dilo_nilo.ui.navigation.Routes

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Routes.DASHBOARD, Icons.Default.Home),
    BottomNavItem("Loans", Routes.LOANS_OVERVIEW, Icons.Default.ListAlt),
    BottomNavItem("Search", Routes.SEARCH, Icons.Default.Search),
    BottomNavItem("Contacts", Routes.CONNECTIONS, Icons.Default.People),
    BottomNavItem("Profile", Routes.PROFILE, Icons.Default.AccountCircle)
)

@Composable
fun AppBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showRoutes = listOf(
        Routes.DASHBOARD, Routes.LOANS_OVERVIEW, Routes.SEARCH, Routes.CONNECTIONS, Routes.PROFILE
    )
    if (currentRoute !in showRoutes) return

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
