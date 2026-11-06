package com.revlogix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revlogix.app.R
import com.revlogix.app.data.local.Vehicle
import com.revlogix.app.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_vehicle))
            }
        }
    ) { innerPadding ->
        if (vehicles.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.home_empty))
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle -> VehicleCard(vehicle) }
            }
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { make, model, year, bodyStyle, reg, odometer ->
                viewModel.addVehicle(make, model, year, bodyStyle, reg, odometer)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${vehicle.year} ${vehicle.make} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
            Text(vehicle.registrationNumber, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.odometer_label, vehicle.currentOdometer))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (make: String, model: String, year: Int, bodyStyle: String, registration: String, odometer: Int) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var bodyStyle by remember { mutableStateOf("") }
    var registration by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_add_vehicle)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text(stringResource(R.string.field_make)) })
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text(stringResource(R.string.field_model)) })
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text(stringResource(R.string.field_year)) })
                OutlinedTextField(value = bodyStyle, onValueChange = { bodyStyle = it }, label = { Text(stringResource(R.string.field_body_style)) })
                OutlinedTextField(value = registration, onValueChange = { registration = it }, label = { Text(stringResource(R.string.field_registration)) })
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text(stringResource(R.string.field_odometer_km)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(make, model, year.toIntOrNull() ?: 0, bodyStyle, registration, odometer.toIntOrNull() ?: 0)
            }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}