package com.revlogix.app

import com.revlogix.app.data.local.MaintenanceRecord
import com.revlogix.app.domain.model.HealthScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthScoreCalculatorTest {

    @Test
    fun `no records gives perfect score`() {
        val score = HealthScoreCalculator.calculateHealthScore(emptyList(), currentOdometer = 100000)
        assertEquals(100, score)
    }

    @Test
    fun `overdue record by odometer reduces score`() {
        val overdue = MaintenanceRecord(
            vehicleId = 1, serviceType = "Oil change", serviceDate = 0,
            odometer = 90000, cost = 500.0, nextDueDate = null, nextDueOdometer = 95000
        )
        val score = HealthScoreCalculator.calculateHealthScore(listOf(overdue), currentOdometer = 100000)
        assertEquals(85, score)
    }

    @Test
    fun `upcoming record reduces score less than overdue`() {
        val upcoming = MaintenanceRecord(
            vehicleId = 1, serviceType = "Brake check", serviceDate = 0,
            odometer = 90000, cost = 300.0, nextDueDate = null, nextDueOdometer = 150000
        )
        val score = HealthScoreCalculator.calculateHealthScore(listOf(upcoming), currentOdometer = 100000)
        assertEquals(95, score)
    }

    @Test
    fun `badge tiers match score ranges`() {
        assertEquals("Master Tuner", HealthScoreCalculator.badgeForScore(90))
        assertEquals("Regular Servicer", HealthScoreCalculator.badgeForScore(60))
        assertEquals("Novice Mechanic", HealthScoreCalculator.badgeForScore(20))
    }
}