package com.example.mediaplayer.exoplayer.callback

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.mediaplayer.exoplayer.MusicServices
import com.example.mediaplayer.other.Constants
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener(
    private val musicService: MusicServices
): PlayerNotificationManager.NotificationListener {
    // когда пользоваетель уберает уведомление (отменяет)
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        musicService.apply {
            stopForeground(true) // удаление уведомление
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
            // если службы не переднего плана
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                //сервенсая службы переднего плана
                startForeground(Constants.NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}