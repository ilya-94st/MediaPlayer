package com.example.mediaplayer.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.other.Constants.NETWORK_ERROR
import com.example.mediaplayer.other.Event
import com.example.mediaplayer.other.Resource

// класс который являеся связующим звеном между сервисом и актививити
class MusicServiceConnection(context: Context) {
    private var _isConnected: MutableLiveData<Event<Resource<Boolean>>> = MutableLiveData()

    fun isConnected() : LiveData<Event<Resource<Boolean>>> {
        return _isConnected
    }

    private var _networkError: MutableLiveData<Event<Resource<Boolean>>> = MutableLiveData()

    fun networkError() : LiveData<Event<Resource<Boolean>>> {
        return _networkError
    }

    private var _curPlaySong: MutableLiveData<MediaMetadataCompat?> = MutableLiveData()

    fun curPlaySong() : LiveData<MediaMetadataCompat?> {
        return _curPlaySong
    }
// живые данные текущего состояния воспроизведения музыки
    private var _playBackState: MutableLiveData<PlaybackStateCompat?> = MutableLiveData()

    fun playbackState() : LiveData<PlaybackStateCompat?> {
        return _playBackState
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private var mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    lateinit var mediaController: MediaControllerCompat

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private var mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicServices::class.java),
        mediaBrowserConnectionCallback,null
    ).apply { connect() }

    // мы не можем экземляр медиаКонтролера сдесь потомучто на нужен доступ с сеансу токена нашей сервес службы
    val transportController: MediaControllerCompat.TransportControls // служит для пропуска песен, перехода к предыдущей, приостоновке возбновления плейра
    get() = mediaController.transportControls

    // функция подписки на конкретный id доступ к нашем данным из firebase
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun subscribe(parentId: String, callBack: MediaBrowserCompat.SubscriptionCallback){
         mediaBrowser.subscribe(parentId, callBack)
    }
// функция отписки по id
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun unSubscribe(parentId: String, callBack: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId, callBack)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class MediaBrowserConnectionCallback(
        private var context: Context
    ): MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallBack())
            }
            _isConnected.postValue(Event(Resource.successes(true)))
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            _isConnected.postValue(Event(Resource.error("connection was suspend", false)))
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            _isConnected.postValue(Event(Resource.error("Could not connect to media browser", false)))
        }
    }

    private inner class MediaControllerCallBack(): MediaControllerCompat.Callback() {
        // чтобы можно было наблюдать за изменениями из фрагментов
        // приостоновка и возбновление музыки
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playBackState.postValue(state)
        }
        // данные о новой песне
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlaySong.postValue(metadata)
        }

        // когда произошла сетевая ошибка
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR ->{
                _networkError.postValue(
                    Event(
                        Resource.error("no internet", null)
                    )
                )
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}