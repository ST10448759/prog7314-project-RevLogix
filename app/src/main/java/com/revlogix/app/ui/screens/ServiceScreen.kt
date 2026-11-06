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
import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.ui.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(viewModel: MaintenanceViewModel?, modifier: Modifier = Modifier) {
    if (viewModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.common_add_vehicle_first))
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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.service_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.service_add))
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.service_health_score, healthScore), style = MaterialTheme.typography.titleMedium)
                    Text(badgeLabel(badge), style = MaterialTheme.typography.bodyMedium)
                }
            }

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.service_tab_upcoming)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.service_tab_overdue)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.service_tab_completed)) })
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
            Text(stringResource(R.string.service_done_line, record.odometer, record.cost.toString()), style = MaterialTheme.typography.bodySmall)
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
        title = { Text(stringResource(R.string.service_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text(stringResource(R.string.service_field_type)) })
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text(stringResource(R.string.fuel_field_odometer)) })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text(stringResource(R.string.service_field_cost)) })
                OutlinedTextField(value = months, onValueChange = { months = it }, label = { Text(stringResource(R.string.service_field_next_months)) })
                OutlinedTextField(value = dueOdo, onValueChange = { dueOdo = it }, label = { Text(stringResource(R.string.service_field_next_odometer)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(type, odometer.toIntOrNull() ?: 0, cost.toDoubleOrNull() ?: 0.0, months.toIntOrNull(), dueOdo.toIntOrNull())
            }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

// HealthScoreCalculator returns the badge as a fixed English name (it is unit tested that way),
// so it is mapped to a translated string here for display.
@Composable
private fun badgeLabel(badge: String): String = stringResource(
    when (badge) {
        "Master Tuner" -> R.string.badge_master
        "Regular Servicer" -> R.string.badge_regular
        else -> R.string.badge_novice
    }
)
