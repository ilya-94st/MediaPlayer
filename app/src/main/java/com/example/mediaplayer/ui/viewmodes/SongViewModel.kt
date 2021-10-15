package com.example.mediaplayer.ui.viewmodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.exoplayer.MusicServiceConnection
import com.example.mediaplayer.exoplayer.MusicServices
import com.example.mediaplayer.exoplayer.currentPlaybackPosition
import com.example.mediaplayer.other.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(private var musicServiceConnection: MusicServiceConnection): ViewModel(){
    private var playbackState = musicServiceConnection.playbackState()

    private var _curSongDuration: MutableLiveData<Long> = MutableLiveData()

    init {
        updateCurrentPlayerPosition()
    }

    fun curSongDuration(): LiveData<Long> {
        return _curSongDuration
    }

    private var _curSongPosition: MutableLiveData<Long> = MutableLiveData()

    fun curSongPosition(): LiveData<Long> {
        return _curSongPosition
    }

    private fun updateCurrentPlayerPosition() {
     viewModelScope.launch {
         while (true) {
          val pos = playbackState.value?.currentPlaybackPosition
             if(curSongPosition().value != pos) {
                 _curSongPosition.postValue(pos)
                 _curSongDuration.postValue(MusicServices.curDuration)
             }
             delay(Constants.UPDATE_INTERVAL_SONG)
         }
     }
    }
}