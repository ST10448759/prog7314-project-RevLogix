package com.revlogix.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a registered RevLogix user.
 * Maps to the User entity in the Part 1 data model (Section 4).
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val fullName: String,
    val email: String,
    val identityProviderId: String,
    val preferredLanguage: String = "en",
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)