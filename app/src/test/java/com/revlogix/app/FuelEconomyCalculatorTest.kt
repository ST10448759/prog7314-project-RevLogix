package com.revlogix.app

import com.revlogix.app.data.local.FuelLog
import com.revlogix.app.domain.model.FuelEconomyCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FuelEconomyCalculatorTest {

    @Test
    fun `fewer than two full tanks returns null`() {
        val logs = listOf(
            FuelLog(vehicleId = 1, fillUpDate = 1, odometer = 1000, litres = 40.0, totalCost = 800.0, fuelType = "Petrol", fullTank = true)
        )
        assertNull(FuelEconomyCalculator.calculateLitresPer100Km(logs))
    }

    @Test
    fun `calculates litres per 100km correctly`() {
        val logs = listOf(
            FuelLog(vehicleId = 1, fillUpDate = 2, odometer = 1400, litres = 40.0, totalCost = 800.0, fuelType = "Petrol", fullTank = true),
            FuelLog(vehicleId = 1, fillUpDate = 1, odometer = 1000, litres = 38.0, totalCost = 760.0, fuelType = "Petrol", fullTank = true)
        )
        // distance = 400km, litres used for that stretch = 40L -> 10 L/100km
        val result = FuelEconomyCalculator.calculateLitresPer100Km(logs)
        assertEquals(10.0, result!!, 0.01)
    }

    @Test
    fun `ignores non full tank entries`() {
        val logs = listOf(
            FuelLog(vehicleId = 1, fillUpDate = 2, odometer = 1200, litres = 20.0, totalCost = 400.0, fuelType = "Petrol", fullTank = false),
            FuelLog(vehicleId = 1, fillUpDate = 1, odometer = 1000, litres = 40.0, totalCost = 800.0, fuelType = "Petrol", fullTank = true)
        )
        assertNull(FuelEconomyCalculator.calculateLitresPer100Km(logs))
    }
}