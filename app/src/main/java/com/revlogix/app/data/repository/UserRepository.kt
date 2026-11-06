package com.revlogix.app.data.repository

import android.util.Log
import com.revlogix.app.data.local.User
import com.revlogix.app.data.local.UserDao

class UserRepository(private val userDao: UserDao) {

    /**
     * Finds the local user for a Firebase account, creating the row the first time that
     * account signs in on this phone. The Firebase uid is stored as identityProviderId.
     */
    suspend fun getOrCreate(providerId: String, name: String, email: String): User {
        userDao.getUserByProviderId(providerId)?.let { return it }

        val newUser = User(
            fullName = name.ifBlank { email },
            email = email,
            identityProviderId = providerId
        )
        val id = userDao.insert(newUser).toInt()
        Log.i(TAG, "Created local user $id for new sign-in")
        return newUser.copy(userId = id)
    }

    private companion object {
        const val TAG = "UserRepository"
    }
}
