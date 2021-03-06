package com.example.mediaplayer.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.mediaplayer.R
import com.example.mediaplayer.adapters.SwipeSongAdapter
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.example.mediaplayer.date.entinities.Song
import com.example.mediaplayer.exoplayer.isPlaying
import com.example.mediaplayer.exoplayer.toSong
import com.example.mediaplayer.other.Resource
import com.example.mediaplayer.ui.viewmodes.MainViewModel
import com.google.android.material.snackbar.Snackbar
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

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.vpSong.adapter = swipeSongAdapter
        subscribeToObserves()
        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })
        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        swipeSongAdapter.setOnItemClickListener {
            binding.fragmentContainerView.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        binding.fragmentContainerView.findNavController().addOnDestinationChangedListener{_, destination, _ ->
            when(destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
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
        mainViewModel.playbackState.observe(this, Observer {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.play else R.drawable.pause
            )
        })
        mainViewModel.isConnected.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                result ->
                when(result.status){
                    Resource.Status.ERROR -> {
                     Snackbar.make(binding.rootLoyaut,
                         result.message ?: "An know error occurred",
                         Snackbar.LENGTH_SHORT).show()
                    }
                   else -> Unit
                }
            }
        })
        mainViewModel.networkError.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                    result ->
                when(result.status){
                    Resource.Status.ERROR -> {
                        Snackbar.make(binding.rootLoyaut,
                            result.message ?: "An know error occurred",
                            Snackbar.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        })
    }

private fun hideBottomBar() {
    binding.ivPlayPause.isVisible = false
    binding.vpSong.isVisible = false
    binding.rootLoyaut.isVisible = false
}

    private fun showBottomBar() {
        binding.ivPlayPause.isVisible = true
        binding.vpSong.isVisible = true
        binding.rootLoyaut.isVisible = true
    }
}