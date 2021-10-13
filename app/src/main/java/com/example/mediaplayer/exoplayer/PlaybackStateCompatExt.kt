package com.example.mediaplayer.exoplayer

import android.support.v4.media.session.PlaybackStateCompat

// extension подготовка для воспроизведения песни
inline val PlaybackStateCompat.isPrepared
           get() = state == PlaybackStateCompat.STATE_BUFFERING ||
                   state == PlaybackStateCompat.STATE_PLAYING ||
                   state == PlaybackStateCompat.STATE_PAUSED
// игра песни
inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING
// песня действительно включена
inline val PlaybackStateCompat.isEnabledPlaying
           get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
                   (actions and  PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                           state == PlaybackStateCompat.STATE_PAUSED)
