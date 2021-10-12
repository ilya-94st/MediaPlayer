package com.example.mediaplayer.exoplayer.callback

import android.widget.Toast
import com.example.mediaplayer.exoplayer.MusicServices
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import java.util.*

class MusicPlayerEventListener(
    private val musicServices: MusicServices
): Player.EventListener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady){
   musicServices.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicServices, "Error in service", Toast.LENGTH_SHORT).show()
    }
}