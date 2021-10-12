package com.example.mediaplayer.exoplayer.callback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.mediaplayer.exoplayer.FirebaseMusicSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

// класс нужен для подготовки мызыки из fireBase для уведомления
class MusicPlaybackPrepare(
   private val firebaseMusicSource: FirebaseMusicSource,
    val playerPrepare: (MediaMetadataCompat?) -> Unit // это функция нужна для того чтобы ее потом вызывать когда наш источник музыки будет готов
): MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false
    override fun getSupportedPrepareActions(): Long {
        // готовим музыку по ее id и воспроизвести ее по id
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        // готовим(запускаем) источник музыки тогда когда он готов загружен
        firebaseMusicSource.whenReady {
            // находим музыку
            val itemPlay = firebaseMusicSource.songs.find {
                mediaId == it.description.mediaId
            }
            playerPrepare(itemPlay)
        }
    }

    // управление голосом
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

}