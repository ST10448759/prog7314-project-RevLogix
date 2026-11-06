package com.revlogix.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.revlogix.app.MainActivity
import com.revlogix.app.R
import com.revlogix.app.util.LocaleHelper

object NotificationHelper {
    const val CHANNEL_ID = "maintenance_alerts"
    private const val TAG = "NotificationHelper"

    /** Channels are required from Android 8 (our minSdk), so this is always safe to call. */
    fun createChannel(context: Context) {
        val localised = LocaleHelper.wrap(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            localised.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = localised.getString(R.string.channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Shows a notification for a push that arrived while the app is open. */
    fun show(context: Context, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission not granted, skipping")
            return
        }

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_revlogix)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "Notification shown")
    }
}
