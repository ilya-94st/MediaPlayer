package com.example.mediaplayer.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.net.toUri
import com.example.mediaplayer.date.remote.MusicDatabase
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// получаем  все песни из firestore
class FirebaseMusicSource @Inject constructor(var musicDatabase: MusicDatabase) {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>() // слушатель нужен для того чтобы понять загружены ли песни и потом только выполнять последующий код
// список песене
    var songs = emptyList<MediaMetadataCompat>()

    // функция по считыванию данных из firebase
    suspend fun fetchMediaData() = withContext(Dispatchers.IO){ // функция которая получает все объекты из firebase
state = State.STATE_INITIALIZING // устанавливаем состояние на иницилизирование т.к наша музыка будет загружаться
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map {song->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }
        state = State.STATE_INITIALIZED // иницилизировано
    }

    // чтобы каждый раз песня переключалась на следующую когда она закончиться
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach{song->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()


// процесс определения состояние музыки при ее загрузки из firestore
    private var state: State = State.STATE_CREATED
    set(value) {
        if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) { // если наша музыка иницилизирована или произошла ошибка значит процесс завершен и больше нечего не произайдет
           // если первое условие будет выполнено то мы знаем что находимся в одном потоке
            synchronized(onReadyListener){
                field = value // поле ссылается на текущее состояние и мы ему присваем новое состояние
                onReadyListener.forEach{listener->
                    listener(state == State.STATE_INITIALIZED) // если состояние иницилизировано то вызываем true иначе error и вызываем false
                }
            }
        } else { // иначе наше состояние музыки будет  STATE_INITIALIZING будет иницилизироваться
            field = value
        }
    }
    fun whenReady(action : (Boolean) -> Unit) : Boolean {
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            // когда источник музыки state == State.STATE_CREATED
            onReadyListener += action // мы добавляем его
            return false // в этот момент наш источник музыки не готов
        } else {
            // переходим в этот блок в случае если лябда функция получила false
            action(state == State.STATE_INITIALIZED)
            return true // сдесь источник музыки готов
        }
    }

}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}