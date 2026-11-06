package com.revlogix.app.domain.model

import com.revlogix.app.data.local.FuelLog

// FR-04: litres per 100km using the two most recent full-tank entries.
object FuelEconomyCalculator {
    fun calculateLitresPer100Km(logs: List<FuelLog>): Double? {
        val fullTanks = logs.filter { it.fullTank }.sortedByDescending { it.fillUpDate }
        if (fullTanks.size < 2) return null
        val latest = fullTanks[0]
        val previous = fullTanks[1]
        val distance = latest.odometer - previous.odometer
        if (distance <= 0) return null
        return (latest.litres / distance) * 100
    }
}