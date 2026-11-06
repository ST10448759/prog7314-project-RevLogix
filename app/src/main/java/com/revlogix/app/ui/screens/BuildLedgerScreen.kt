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
import com.revlogix.app.data.local.CustomPart
import com.revlogix.app.ui.viewmodel.CustomPartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildLedgerScreen(viewModel: CustomPartViewModel?, modifier: Modifier = Modifier) {
    if (viewModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.common_add_vehicle_first))
        }
        return
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val parts by viewModel.parts.collectAsState()
    val grouped = parts.groupBy { it.category }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.build_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.build_add))
            }
        }
    ) { padding ->
        if (parts.isEmpty()) {
            Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.build_empty))
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (category, categoryParts) ->
                    item {
                        Text("$category (${categoryParts.size})", style = MaterialTheme.typography.titleMedium)
                    }
                    items(categoryParts) { part -> PartCard(part) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPartDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { category, name, brand, partNum, serial, specs, vendor, cost ->
                viewModel.addPart(category, name, brand, partNum, serial, specs, vendor, cost)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PartCard(part: CustomPart) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(part.partName, style = MaterialTheme.typography.titleSmall)
            Text("${part.brand} - ${part.partNumber}", style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.build_serial_line, part.serialNumber), style = MaterialTheme.typography.bodySmall)
            Text("R${part.cost} - ${part.vendor}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPartDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, name: String, brand: String, partNumber: String, serial: String, specs: String, vendor: String, cost: Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var specs by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.build_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.build_field_category)) })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.build_field_name)) })
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text(stringResource(R.string.build_field_brand)) })
                OutlinedTextField(value = partNumber, onValueChange = { partNumber = it }, label = { Text(stringResource(R.string.build_field_part_number)) })
                OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text(stringResource(R.string.build_field_serial)) })
                OutlinedTextField(value = specs, onValueChange = { specs = it }, label = { Text(stringResource(R.string.build_field_specs)) })
                OutlinedTextField(value = vendor, onValueChange = { vendor = it }, label = { Text(stringResource(R.string.build_field_vendor)) })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text(stringResource(R.string.service_field_cost)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(category, name, brand, partNumber, serial, specs, vendor, cost.toDoubleOrNull() ?: 0.0)
            }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}