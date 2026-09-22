package com.revlogix.app.domain.model

import com.revlogix.app.data.local.MaintenanceRecord

// FR-06 / FR-08: dual-trigger (time OR mileage, whichever comes first)
// status calculation, plus the overall vehicle health score and badge tier.
object HealthScoreCalculator {

    fun calculateStatus(record: MaintenanceRecord, currentOdometer: Int): String {
        val now = System.currentTimeMillis()
        val dueByDate = record.nextDueDate != null && now >= record.nextDueDate
        val dueByOdometer = record.nextDueOdometer != null && currentOdometer >= record.nextDueOdometer

        return when {
            record.nextDueDate == null && record.nextDueOdometer == null -> "completed"
            dueByDate || dueByOdometer -> "overdue"
            else -> "upcoming"
        }
    }

    fun calculateHealthScore(records: List<MaintenanceRecord>, currentOdometer: Int): Int {
        var score = 100
        records.forEach { record ->
            when (calculateStatus(record, currentOdometer)) {
                "overdue" -> score -= 15
                "upcoming" -> score -= 5
            }
        }
        return score.coerceIn(0, 100)
    }

    fun badgeForScore(score: Int): String = when {
        score >= 80 -> "Master Tuner"
        score >= 50 -> "Regular Servicer"
        else -> "Novice Mechanic"
    }
}