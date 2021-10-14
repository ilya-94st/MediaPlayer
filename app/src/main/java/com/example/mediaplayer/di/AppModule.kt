package com.example.mediaplayer.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mediaplayer.R
import com.example.mediaplayer.adapters.SwipeSongAdapter
import com.example.mediaplayer.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMusicServiceConnection(
        @ApplicationContext app: Context
    ) = MusicServiceConnection(app)

    @Provides
    @Singleton
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

    @Provides
    @Singleton
    fun provideGlideInstance(
@ApplicationContext app: Context
    ) = Glide.with(app).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.image)
            .error(R.drawable.ic_baseline_error_24)
            .diskCacheStrategy(DiskCacheStrategy.DATA) // наше изображение кэшируется при помощи Glide
    )

}