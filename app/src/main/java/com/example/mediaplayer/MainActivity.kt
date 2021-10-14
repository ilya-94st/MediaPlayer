package com.example.mediaplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.RequestManager
import com.example.mediaplayer.adapters.SwipeSongAdapter
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.example.mediaplayer.date.entinities.Song
import com.example.mediaplayer.exoplayer.toSong
import com.example.mediaplayer.other.Resource
import com.example.mediaplayer.ui.viewmodes.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var curPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.vpSong.adapter = swipeSongAdapter
        subscribeToObserves()
    }

    private fun switchViewPagerTorrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObserves() {
        mainViewModel.mediaItems().observe(this, Observer {
            it?.let {
                result->
                when(result.status){
                    Resource.Status.SUCCESSES -> {
                     result.data?.let {
                         songs->
                         swipeSongAdapter.songs = songs
                         if (songs.isNotEmpty()) {
                             glide.load((curPlayingSong ?: songs[0]).imageUrl).into(binding.ivCurSongImage)
                         }
                         switchViewPagerTorrentSong(curPlayingSong?: return@Observer)
                     }
                    }
                    Resource.Status.ERROR -> Unit
                    Resource.Status.LOADING -> Unit
                }
            }
        })
        mainViewModel.curPlaySong.observe(this, Observer {
            if(it == null) return@Observer
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerTorrentSong(curPlayingSong?: return@Observer)
        })
    }

}