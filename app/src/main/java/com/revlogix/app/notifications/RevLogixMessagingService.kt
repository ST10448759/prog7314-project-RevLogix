package com.revlogix.app.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.revlogix.app.R
import com.revlogix.app.data.sync.SyncScheduler
import com.revlogix.app.util.LocaleHelper

/**
 * Receives Firebase Cloud Messaging pushes sent by the RevLogix API.
 * When the app is closed or in the background Android shows the notification itself;
 * onMessageReceived only runs for pushes that arrive while the app is open.
 */
class RevLogixMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i(TAG, "New FCM token received")
        // The sync worker registers the token with the API next time it runs
        SyncScheduler.requestSync(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val fallbackTitle = LocaleHelper.wrap(this).getString(R.string.app_name)
        val title = message.notification?.title ?: message.data["title"] ?: fallbackTitle
        val body = message.notification?.body ?: message.data["body"] ?: return
        Log.i(TAG, "Push received: $title")
        NotificationHelper.show(this, title, body)
    }

    private companion object {
        const val TAG = "RevLogixMessaging"
    }
}
