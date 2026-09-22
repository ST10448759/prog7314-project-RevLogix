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
import androidx.compose.ui.unit.dp
import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.ui.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(viewModel: MaintenanceViewModel?, modifier: Modifier = Modifier) {
    if (viewModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Add a vehicle on the Home tab first.")
        }
        return
    }

    var tab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val records by viewModel.records.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val badge by viewModel.badge.collectAsState()

    val filtered = when (tab) {
        0 -> records.filter { viewModel.statusFor(it) == "upcoming" }
        1 -> records.filter { viewModel.statusFor(it) == "overdue" }
        else -> records.filter { viewModel.statusFor(it) == "completed" }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add maintenance record")
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Health Score: $healthScore / 100", style = MaterialTheme.typography.titleMedium)
                    Text(badge, style = MaterialTheme.typography.bodyMedium)
                }
            }

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Upcoming") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Overdue") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Completed") })
            }

            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { record -> MaintenanceCard(record) }
            }
        }
    }

    if (showAddDialog) {
        AddMaintenanceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, odo, cost, months, dueOdo ->
                viewModel.addRecord(type, odo, cost, months, dueOdo)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MaintenanceCard(record: MaintenanceRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(record.serviceType, style = MaterialTheme.typography.titleSmall)
            Text("Done at ${record.odometer} km - R${record.cost}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaintenanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, odometer: Int, cost: Double, nextDueMonths: Int?, nextDueOdometer: Int?) -> Unit
) {
    var type by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var dueOdo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Service type") })
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text("Odometer (km)") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost (R)") })
                OutlinedTextField(value = months, onValueChange = { months = it }, label = { Text("Next due in (months, optional)") })
                OutlinedTextField(value = dueOdo, onValueChange = { dueOdo = it }, label = { Text("Next due odometer (optional)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    type,
                    odometer.toIntOrNull() ?: 0,
                    cost.toDoubleOrNull() ?: 0.0,
                    months.toIntOrNull(),
                    dueOdo.toIntOrNull()
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}