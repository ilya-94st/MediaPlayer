package com.example.mediaplayer.di

import android.content.Context
import com.example.mediaplayer.date.remote.MusicDatabase
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton


@Module
@InstallIn(ServiceComponent::class)
object ServicesModule {

    @Provides
    @ServiceScoped
    fun provideMusicDatabase() = MusicDatabase()

    @Provides
    @ServiceScoped
fun provideAudioAttribute() = AudioAttributes .Builder() // воспроизведение нашей музыки
    .setContentType(C.CONTENT_TYPE_MUSIC)
    .setUsage(C.USAGE_MEDIA)
    .build()

    @Provides
    @ServiceScoped
    fun provideExoPlayer( // проигрователь нашей музыки
        @ApplicationContext app: Context,
        audioAttributes: AudioAttributes
    ) = SimpleExoPlayer.Builder(app).build().apply {
        setAudioAttributes(audioAttributes, true) // для обработки звука
        setHandleAudioBecomingNoisy(true) // приостанавливать плеер в том случае когда пользователь подключит наушники или вынимит их чтобы небыло так громко
    }

    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(
        @ApplicationContext app: Context
    ) = DefaultDataSourceFactory(app, Util.getUserAgent(app, "media app")) // имя пользовательского агента

}