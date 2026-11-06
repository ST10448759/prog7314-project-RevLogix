package com.revlogix.app.data.sync

/** Names used for the API collections and as the entityType in the sync queue. */
object SyncTypes {
    const val VEHICLES = "vehicles"
    const val FUEL_LOGS = "fuelLogs"
    const val EXPENSES = "expenses"
    const val MAINTENANCE = "maintenanceRecords"
    const val PARTS = "customParts"

    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}
