package com.revlogix.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Small wrapper around FirebaseAuth. If google-services.json is missing the Firebase app is
 * never initialised and FirebaseAuth.getInstance() throws, so every caller goes through here
 * and simply sees "nobody signed in" instead of crashing.
 */
object FirebaseSession {
    fun user(): FirebaseUser? = try {
        FirebaseAuth.getInstance().currentUser
    } catch (e: IllegalStateException) {
        null
    }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: IllegalStateException) {
            // Firebase not configured - nothing to sign out of
        }
    }
}
