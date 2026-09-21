package com.revlogix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.revlogix.app.data.local.AppDatabase
import com.revlogix.app.data.repository.UserRepository
import com.revlogix.app.data.repository.VehicleRepository
import com.revlogix.app.ui.navigation.Screen
import com.revlogix.app.ui.screens.*
import com.revlogix.app.ui.theme.RevLogixTheme
import com.revlogix.app.ui.viewmodel.VehicleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevLogixTheme {
                RevLogixApp()
            }
        }
    }
}

@Composable
fun RevLogixApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Fuel, Screen.Service, Screen.Build, Screen.Settings)

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val vehicleRepository = remember { VehicleRepository(database.vehicleDao()) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModel.provideFactory(vehicleRepository, userRepository)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val icon = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Fuel -> Icons.Filled.LocalGasStation
                        Screen.Service -> Icons.Filled.Build
                        Screen.Build -> Icons.Filled.Settings
                        Screen.Settings -> Icons.Filled.Person
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel = vehicleViewModel) }
            composable(Screen.Fuel.route) { FuelScreen() }
            composable(Screen.Service.route) { ServiceScreen() }
            composable(Screen.Build.route) { BuildLedgerScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}