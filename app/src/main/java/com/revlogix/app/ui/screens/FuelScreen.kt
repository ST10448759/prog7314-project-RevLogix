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
import com.revlogix.app.data.local.Expense
import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.ui.viewmodel.FuelExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(viewModel: FuelExpenseViewModel?, modifier: Modifier = Modifier) {
    if (viewModel == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Add a vehicle on the Home tab first.")
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
        topBar = { TopAppBar(title = { Text("Fuel & Expenses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (tab == 0) showAddFuel = true else showAddExpense = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Fuel") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Expenses") })
            }

            if (tab == 0) {
                economy?.let {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "Average economy: ${"%.1f".format(it)} L/100km",
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
            onConfirm = { category, amount, desc ->
                viewModel.addExpense(category, amount, desc)
                showAddExpense = false
            }
        )
    }
}

@Composable
private fun FuelCard(log: FuelLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${log.litres}L - R${log.totalCost}")
            Text("Odometer: ${log.odometer} km", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${expense.category} - R${expense.amount}")
            Text(expense.description, style = MaterialTheme.typography.bodySmall)
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
        title = { Text("Add Fuel Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = odometer, onValueChange = { odometer = it }, label = { Text("Odometer (km)") })
                OutlinedTextField(value = litres, onValueChange = { litres = it }, label = { Text("Litres") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Total cost (R)") })
                OutlinedTextField(value = fuelType, onValueChange = { fuelType = it }, label = { Text("Fuel type") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fullTank, onCheckedChange = { fullTank = it })
                    Text("Full tank")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(odometer.toIntOrNull() ?: 0, litres.toDoubleOrNull() ?: 0.0, cost.toDoubleOrNull() ?: 0.0, fuelType, fullTank)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double, description: String) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (R)") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(category, amount.toDoubleOrNull() ?: 0.0, description) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}