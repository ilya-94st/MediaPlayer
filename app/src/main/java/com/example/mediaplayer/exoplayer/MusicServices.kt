package com.example.mediaplayer.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.mediaplayer.date.entinities.Song
import com.example.mediaplayer.exoplayer.callback.MusicPlaybackPrepare
import com.example.mediaplayer.exoplayer.callback.MusicPlayerEventListener
import com.example.mediaplayer.exoplayer.callback.MusicPlayerNotificationListener
import com.example.mediaplayer.other.Constants
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "music_service"
class MusicServices: MediaBrowserServiceCompat()  {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()

    private var isPlayerInitialized: Boolean = false

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private val serviceScope =  CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediassision: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private  var curPlayingSong: MediaMetadataCompat? = null

    var isForegroundService = false

    companion object {
        var curDuration = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediassision = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediassision.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediassision.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
          curDuration = exoPlayer.duration
        }

        val musicPlayback = MusicPlaybackPrepare(firebaseMusicSource){
            curPlayingSong = it
            preparePlayMusic(
                firebaseMusicSource.songs,
                it, true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediassision)
        mediaSessionConnector.setPlaybackPreparer(musicPlayback)
        mediaSessionConnector.setPlayer(exoPlayer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayMusic(songs: List<MediaMetadataCompat>,
    itemToPlay: MediaMetadataCompat?,
    playNow: Boolean){
    val curSongsIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongsIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediassision) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
       return BrowserRoot(Constants.MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId){
           Constants.MEDIA_ROOT_ID -> {
               val resultsSets = firebaseMusicSource.whenReady { isInitialized->
                   if(isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                       result.sendResult(firebaseMusicSource.asMediaItems())
                       if(!isPlayerInitialized){
                        preparePlayMusic(firebaseMusicSource.songs, firebaseMusicSource.songs[0],false)
                        isPlayerInitialized= true
                       }
                   } else {
                       result.sendResult(null)
                   }
               }
               if(!resultsSets) {
                   result.detach()
               }
           }
       }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.release()
        exoPlayer.removeListener(musicPlayerEventListener)
    }
}