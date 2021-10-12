package com.example.mediaplayer.exoplayer


import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mediaplayer.R
import com.example.mediaplayer.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.mediaplayer.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager



@Suppress("DEPRECATION")
class MusicNotificationManager(
     val context: Context,
     sessionToken: MediaSessionCompat.Token,
     notificationListener: PlayerNotificationManager.NotificationListener,
     private val newSongCallback: () -> Unit // обнавить текущуюю песню
) {
     private val notificationManager: PlayerNotificationManager

     // функция которая показывает наше уведомление
     fun showNotification(player: Player) {
          notificationManager.setPlayer(player)
     }

     init {
          val mediaController = MediaControllerCompat(context, sessionToken) // нужен для работы с песней в уведомлении, остановить, продолжить и.п
          // создаем уведомление
          notificationManager = PlayerNotificationManager.createWithNotificationChannel(
               context,
               NOTIFICATION_CHANNEL_ID,
               R.string.notification_channel_name,
               R.string.notification_channel_description,
               NOTIFICATION_ID,
               DescriptionAdapter(mediaController), // адаптер для музыки
               notificationListener

          ).apply {
               setSmallIcon(R.drawable.ic_baseline_music_note_24)
               setMediaSessionToken(sessionToken) // свзяка с сервисом
          }
     }

    private inner class DescriptionAdapter(
         private val mediaControllerCompat: MediaControllerCompat
    ): PlayerNotificationManager.MediaDescriptionAdapter {
         override fun getCurrentContentTitle(player: Player): CharSequence {
              // получаем заголовок воспроизводимой песни
              return mediaControllerCompat.metadata.description.title.toString()
         }

         override fun createCurrentContentIntent(player: Player): PendingIntent? {
              // при нажатии на уведомление возвращает нас к маинактивити
             return mediaControllerCompat.sessionActivity
         }

         override fun getCurrentContentText(player: Player): CharSequence {
             return mediaControllerCompat.metadata.description.subtitle.toString()
         }

         override fun getCurrentLargeIcon(
              player: Player,
              callback: PlayerNotificationManager.BitmapCallback
         ): Bitmap? {
              // изображение песни
              Glide.with(context).asBitmap().load(mediaControllerCompat.metadata.description.iconUri)
                   .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(
                             resource: Bitmap,
                             transition: Transition<in Bitmap>?
                        ) {
                            callback.onBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                   })
              return null
         }
    }

}