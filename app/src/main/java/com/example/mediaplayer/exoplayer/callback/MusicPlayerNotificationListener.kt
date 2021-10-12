package com.example.mediaplayer.exoplayer.callback

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.mediaplayer.exoplayer.MusicServices
import com.example.mediaplayer.other.Constants
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener(
    val musicService: MusicServices
): PlayerNotificationManager.NotificationListener {
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        musicService.apply {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(Constants.NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}