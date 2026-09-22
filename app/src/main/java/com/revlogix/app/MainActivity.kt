package com.revlogix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.revlogix.app.data.repository.*
import com.revlogix.app.ui.navigation.Screen
import com.revlogix.app.ui.screens.*
import com.revlogix.app.ui.theme.RevLogixTheme
import com.revlogix.app.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevLogixTheme { RevLogixRoot() }
        }
    }
}

@Composable
fun RevLogixRoot() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory(userRepository))
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        LoginScreen(onSignIn = { authViewModel.signInWithSso() })
    } else {
        RevLogixApp(onSignOut = { authViewModel.signOut() })
    }
}

@Composable
fun RevLogixApp(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Fuel, Screen.Service, Screen.Build, Screen.Settings)
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val vehicleRepository = remember { VehicleRepository(database.vehicleDao()) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val fuelRepository = remember { FuelRepository(database.fuelLogDao()) }
    val expenseRepository = remember { ExpenseRepository(database.expenseDao()) }
    val maintenanceRepository = remember { MaintenanceRepository(database.maintenanceRecordDao()) }
    val customPartRepository = remember { CustomPartRepository(database.customPartDao()) }

    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModel.provideFactory(vehicleRepository, userRepository)
    )
    val vehicles by vehicleViewModel.vehicles.collectAsState()
    val activeVehicle = vehicles.firstOrNull()
    val activeVehicleId = activeVehicle?.vehicleId

    val fuelExpenseViewModel: FuelExpenseViewModel? = if (activeVehicleId != null) {
        viewModel(
            key = "fuel_$activeVehicleId",
            factory = FuelExpenseViewModel.provideFactory(fuelRepository, expenseRepository, activeVehicleId)
        )
    } else null

    val maintenanceViewModel: MaintenanceViewModel? = if (activeVehicleId != null) {
        viewModel(
            key = "maint_$activeVehicleId",
            factory = MaintenanceViewModel.provideFactory(maintenanceRepository, activeVehicleId, activeVehicle.currentOdometer)
        )
    } else null

    val customPartViewModel: CustomPartViewModel? = if (activeVehicleId != null) {
        viewModel(
            key = "parts_$activeVehicleId",
            factory = CustomPartViewModel.provideFactory(customPartRepository, activeVehicleId)
        )
    } else null

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(database.userDao())
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
            composable(Screen.Fuel.route) { FuelScreen(viewModel = fuelExpenseViewModel) }
            composable(Screen.Service.route) { ServiceScreen(viewModel = maintenanceViewModel) }
            composable(Screen.Build.route) { BuildLedgerScreen(viewModel = customPartViewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = settingsViewModel, onSignOut = onSignOut) }
        }
    }
}