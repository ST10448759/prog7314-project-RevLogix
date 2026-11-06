package com.revlogix.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revlogix.app.R
import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.ui.viewmodel.FuelExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(viewModel: FuelExpenseViewModel?, modifier: Modifier = Modifier) {
    if (viewModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.common_add_vehicle_first))
        }
        return
    }

    var tab by remember { mutableStateOf(0) }
    var showAddFuel by remember { mutableStateOf(false) }
    var showAddExpense by remember { mutableStateOf(false) }
    val fuelLogs by viewModel.fuelLogs.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val economy by viewModel.averageEconomy.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.fuel_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (tab == 0) showAddFuel = true else showAddExpense = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(if (tab == 0) R.string.fuel_add_entry else R.string.expense_add))
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.fuel_tab_fuel)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.fuel_tab_expenses)) })
            }

            if (tab == 0) {
                economy?.let {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            stringResource(R.string.fuel_avg_economy, "%.1f".format(it)),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fuelLogs) { log -> FuelCard(log) }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expenses) { expense -> ExpenseCard(expense) }
                }
            }
        }
    }

    if (showAddFuel) {
        AddFuelDialog(
            onDismiss = { showAddFuel = false },
            onConfirm = { odo, litres, cost, type, full ->
                viewModel.addFuelLog(odo, litres, cost, type, full)
                showAddFuel = false
            }
        )
    }
    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { showAddExpense = false },
            onConfirm = { category, amount, desc, receiptPath ->
                viewModel.addExpense(category, amount, desc, receiptPath)
                showAddExpense = false
            }
        )
    }
}

@Composable
private fun FuelCard(log: FuelLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.fuel_entry_line, log.litres.toString(), log.totalCost.toString()))
            Text(stringResource(R.string.odometer_label, log.odometer), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${expense.category} - R${expense.amount}")
            Text(expense.description, style = MaterialTheme.typography.bodySmall)
            if (expense.receiptUri != null) {
                Text(stringResource(R.string.expense_receipt_attached), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFuelDialog(
    onDismiss: () -> Unit,
    onConfirm: (odometer: Int, litres: Double, cost: Double, fuelType: String, fullTank: Boolean) -> Unit
) {
    var odometer by remember { mutableStateOf("") }
    var litres by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Petrol") }
    var fullTank by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fuel_add_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text(stringResource(R.string.fuel_field_odometer)) })
                OutlinedTextField(value = litres, onValueChange = { litres = it }, label = { Text(stringResource(R.string.fuel_field_litres)) })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text(stringResource(R.string.fuel_field_cost)) })
                OutlinedTextField(value = fuelType, onValueChange = { fuelType = it }, label = { Text(stringResource(R.string.fuel_field_type)) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fullTank, onCheckedChange = { fullTank = it })
                    Text(stringResource(R.string.fuel_full_tank))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(odometer.toIntOrNull() ?: 0, litres.toDoubleOrNull() ?: 0.0, cost.toDoubleOrNull() ?: 0.0, fuelType, fullTank)
            }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double, description: String, receiptPath: String?) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var receiptPath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // The picked photo is copied into the app's own storage so it is still there when the
    // sync worker uploads it later (the picker's URI permission is only temporary).
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val folder = File(context.filesDir, "receipts").apply { mkdirs() }
                val target = File(folder, "receipt_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                receiptPath = target.absolutePath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.expense_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.expense_field_category)) })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.expense_field_amount)) })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.expense_field_description)) })
                OutlinedButton(onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(
                        stringResource(
                            if (receiptPath == null) R.string.expense_attach_receipt
                            else R.string.expense_receipt_attached
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(category, amount.toDoubleOrNull() ?: 0.0, description, receiptPath) }) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}
