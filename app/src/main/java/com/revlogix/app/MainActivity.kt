package com.revlogix.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.revlogix.app.auth.GoogleSignInHelper
import com.revlogix.app.data.local.AppDatabase
import com.revlogix.app.data.repository.*
import com.revlogix.app.data.sync.CloudRestore
import com.revlogix.app.data.sync.SyncEnqueuer
import com.revlogix.app.ui.navigation.Screen
import com.revlogix.app.ui.screens.*
import com.revlogix.app.ui.theme.RevLogixTheme
import com.revlogix.app.ui.viewmodel.*
import com.revlogix.app.util.LocaleHelper

// FragmentActivity (instead of ComponentActivity) is required by BiometricPrompt.
// It still extends ComponentActivity, so setContent works the same way.
class MainActivity : FragmentActivity() {

    // Apply the language chosen in Settings before any resources are loaded
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        setContent {
            RevLogixTheme { RevLogixRoot() }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

@Composable
fun RevLogixRoot() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(
            context.applicationContext,
            userRepository,
            GoogleSignInHelper(context),
            CloudRestore(database)
        )
    )
    val authState by authViewModel.state.collectAsState()
    val signingIn by authViewModel.signingIn.collectAsState()
    val errorRes by authViewModel.errorRes.collectAsState()

    when (val state = authState) {
        AuthState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        AuthState.SignedOut -> LoginScreen(
            onSignIn = { authViewModel.signInWithGoogle(context) },
            isSigningIn = signingIn,
            errorRes = errorRes
        )
        is AuthState.Locked -> BiometricLockScreen(
            onUnlocked = { authViewModel.unlock() },
            onSignOut = { authViewModel.signOut() }
        )
        is AuthState.Ready -> RevLogixApp(
            userId = state.user.userId,
            onSignOut = { authViewModel.signOut() }
        )
    }
}

@Composable
fun RevLogixApp(userId: Int, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Fuel, Screen.Service, Screen.Build, Screen.Settings)
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    // Android 13+ needs the user to allow notifications before push alerts can be shown
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> Log.i("RevLogixApp", "Notification permission granted=$granted") }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Every repository queues its changes through this so they reach the API when online
    val syncEnqueuer = remember { SyncEnqueuer(database.syncQueueDao(), context.applicationContext) }

    val vehicleRepository = remember { VehicleRepository(database.vehicleDao(), syncEnqueuer) }
    val fuelRepository = remember { FuelRepository(database.fuelLogDao(), syncEnqueuer) }
    val expenseRepository = remember { ExpenseRepository(database.expenseDao(), syncEnqueuer) }
    val maintenanceRepository = remember { MaintenanceRepository(database.maintenanceRecordDao(), syncEnqueuer) }
    val customPartRepository = remember { CustomPartRepository(database.customPartDao(), syncEnqueuer) }

    val vehicleViewModel: VehicleViewModel = viewModel(
        key = "vehicles_$userId",
        factory = VehicleViewModel.provideFactory(vehicleRepository, userId)
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
        key = "settings_$userId",
        factory = SettingsViewModel.provideFactory(
            context.applicationContext, database.userDao(), database.syncQueueDao(), userId
        )
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
                        icon = { Icon(icon, contentDescription = stringResource(screen.labelRes)) },
                        label = { Text(stringResource(screen.labelRes)) },
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