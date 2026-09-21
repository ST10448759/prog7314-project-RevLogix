package com.revlogix.app.data.repository

import com.revlogix.app.data.local.User
import com.revlogix.app.data.local.UserDao

class UserRepository(private val userDao: UserDao) {
    // Creates one fixed placeholder user the first time the app runs,
    // so vehicles have a valid userId to attach to before SSO (FR-01) exists.
    suspend fun ensurePlaceholderUser() {
        val existing = userDao.getUserByProviderId("local-placeholder")
        if (existing == null) {
            userDao.insert(
                User(
                    userId = 1,
                    fullName = "Local Test User",
                    email = "test@revlogix.local",
                    identityProviderId = "local-placeholder"
                )
            )
        }
    }
}