package com.example.mediaplayer.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.mediaplayer.exoplayer.callback.MusicPlaybackPrepare
import com.example.mediaplayer.exoplayer.callback.MusicPlayerEventListener
import com.example.mediaplayer.exoplayer.callback.MusicPlayerNotificationListener
import com.example.mediaplayer.other.Constants
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "music_service"
@Suppress("DEPRECATION")
@AndroidEntryPoint
class MusicServices: MediaBrowserServiceCompat()  { // унаследуемся от специального класа который поможет создать список песен

    var isForegroundService = false // будет ли сервесная служба переднего плана или нет

    private var isPlayerInitialized: Boolean = false // чтобы пемня не воспроизводилась сразу

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer // проигрователь

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource
   // чтобы сервис работал асинхронно
    private val serviceJob = Job()
    // кастомная coroutine
    private val serviceScope =  CoroutineScope(Dispatchers.Main + serviceJob)
// нужен для связи со сервисом
    private lateinit var mediaSession: MediaSessionCompat
    // для подключения к медиаСесии
    private lateinit var mediaSessionConnector: MediaSessionConnector
    // создаем экземпляр класа MusicNotificationManager
    private lateinit var musicNotificationManager: MusicNotificationManager
// создаем экземпляр класа MusicPlayerEventListener
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private  var curPlayingSong: MediaMetadataCompat? = null // текущая играющая песня

    companion object {
        var curDuration = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()
        // реализации функции по считыванию данных
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }
        // создаем намерение, нужно чтобы при нажатии на уведомление заходило в приложение
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        // связываем наше намерение с MediaSession
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        // связка происходит по токену
        sessionToken = mediaSession.sessionToken

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer) // получаем наш плеер

        // уведомление
        musicNotificationManager = MusicNotificationManager(
            this, // этот контекс так это наша сервисная служба
            mediaSession.sessionToken, // передаем токен сесии чтобы связывать с сервисом
            MusicPlayerNotificationListener(this) // слушатель который скипывает уведомление или нет
        ){
            // обновлять текущую продолжительность воспроизведения музыки
          curDuration = exoPlayer.duration
        }
        // подготовка музыки к воспроизведению
        val musicPreparePlayback = MusicPlaybackPrepare(firebaseMusicSource){ song->
            curPlayingSong = song
            preparePlayMusic(
                firebaseMusicSource.songs,
                song, true
            )
        }
        // в наш медия Конектор устанавливаем подготовку музыки
        mediaSessionConnector.setPlaybackPreparer(musicPreparePlayback)
        // для обработки событий
        musicPlayerEventListener = MusicPlayerEventListener(this)
        // показывать уведомление
        musicNotificationManager.showNotification(exoPlayer)
        // для изименение информации о музыке во фрагменте в уведомление
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        // добавляем слушатель событий в наш плеер
        exoPlayer.addListener(musicPlayerEventListener)
    }

    // функция для подготовки воспроизведения музыки
    private fun preparePlayMusic(songs: List<MediaMetadataCompat>,
    itemToPlay: MediaMetadataCompat?,
    playNow: Boolean){
        // если не установлена текущая песня то ставим первую из списка, иначе воспроизодим itemToPlay
    val curSongsIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory)) // в наш плеер закидываем подготовку
        exoPlayer.seekTo(curSongsIndex, 0L) // убеждаемся что песня начинается с начала
        exoPlayer.playWhenReady = playNow // когда плаер готов к воспроизведению то playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
       return BrowserRoot(Constants.MEDIA_ROOT_ID, null)
    }

    // список песен на загрузку
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId){
           //индификатор подписки
           Constants.MEDIA_ROOT_ID -> {
               val resultsSets = firebaseMusicSource.whenReady { isInitialized->
                   if(isInitialized){
                       result.sendResult(firebaseMusicSource.asMediaItems())
                       if(!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                           // чтобы песня не воспроизводилась сама
                        preparePlayMusic(firebaseMusicSource.songs, firebaseMusicSource.songs[0],false)
                        isPlayerInitialized= true
                       }
                   } else {
                       // иначе если наша музыка готова но не инецелизирована тогда мы получим просто null
                       result.sendResult(null)
                       mediaSession.sendSessionEvent(Constants.NETWORK_ERROR, null)
                   }
               }
               // если результат не подготовлен
               if(!resultsSets) {
                   result.detach()
               }
           }
       }
    }
// класс который нужен для отображение такой же информации как в фрагменте в уведомление
    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    // когда задача плеера будет удалено то отснавливаем плеер
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop() // останвливаем плеер
    }

    // чтобы отменены куротины при разрушении сервиса
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.release() // чтобы освободить ресурсы плейра exoPlayer
        exoPlayer.removeListener(musicPlayerEventListener) // удаляем наш слушатель событей
    }
}