package com.revlogix.app.ui.navigation

import androidx.annotation.StringRes
import com.revlogix.app.R

// label is a string resource id so the tab names follow the selected language
sealed class Screen(val route: String, @StringRes val labelRes: Int) {
    object Home : Screen("home", R.string.nav_home)
    object Fuel : Screen("fuel", R.string.nav_fuel)
    object Service : Screen("service", R.string.nav_service)
    object Build : Screen("build", R.string.nav_build)
    object Settings : Screen("settings", R.string.nav_settings)
}
