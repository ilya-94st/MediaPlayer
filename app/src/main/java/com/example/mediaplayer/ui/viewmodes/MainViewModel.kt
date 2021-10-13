package com.example.mediaplayer.ui.viewmodes

import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mediaplayer.date.entinities.Song
import com.example.mediaplayer.exoplayer.MusicServiceConnection
import com.example.mediaplayer.exoplayer.isEnabledPlaying
import com.example.mediaplayer.exoplayer.isPrepared
import com.example.mediaplayer.other.Constants
import com.example.mediaplayer.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.FieldPosition
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@HiltViewModel
class MainViewModel @Inject constructor(private var musicServiceConnection: MusicServiceConnection): ViewModel() {

    private var _mediaItems: MutableLiveData<Resource<List<Song>>> = MutableLiveData()

    fun mediaItems() : LiveData<Resource<List<Song>>> {
        return _mediaItems
    }

    val isConnected = musicServiceConnection.isConnected()
    val networkError = musicServiceConnection.networkError()
    val curPlaySong = musicServiceConnection.curPlaySong()
    val playbackState = musicServiceConnection.playbackState()

    init {
        _mediaItems.postValue(Resource.landing(null))
        musicServiceConnection.subscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.successes(items))
            }
        })
    }
// следующая песня
    fun skipNextSong() {
        musicServiceConnection.transportController.skipToNext()
    }
// предыдущая песня
    fun skipPreviousSong() {
        musicServiceConnection.transportController.skipToPrevious()
    }
// для поиска в определенной позиции песни
    fun seekTo(position: Long) {
        musicServiceConnection.transportController.seekTo(position)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == curPlaySong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let{playbackState->
                when{
                    playbackState.isPrepared -> if(toggle) musicServiceConnection.transportController.pause()
                    playbackState.isEnabledPlaying -> musicServiceConnection.transportController.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportController.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // очищаем от подписки
        musicServiceConnection.unSubscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}