package com.revlogix.app.ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Fuel : Screen("fuel", "Fuel")
    object Service : Screen("service", "Service")
    object Build : Screen("build", "Build")
    object Settings : Screen("settings", "Settings")
}