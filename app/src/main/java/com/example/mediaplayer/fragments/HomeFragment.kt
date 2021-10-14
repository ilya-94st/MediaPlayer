package com.example.mediaplayer.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayer.adapters.SongAdapter
import com.example.mediaplayer.base.BaseFragment
import com.example.mediaplayer.databinding.FragmentHomeBinding
import com.example.mediaplayer.other.Resource
import com.example.mediaplayer.ui.viewmodes.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // чтобы явно указать владелец жизненого цикла будет активити
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        initAdapter()
        subscribe()
        songAdapter.setOnItemClickListener {
            viewModel.playOrToggleSong(it)
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun subscribe() {
        viewModel.mediaItems().observe(viewLifecycleOwner, Observer { result->
            when(result.status){
                Resource.Status.SUCCESSES ->{
                 hideProgressBar()
                    result.data?.let {
                            song->songAdapter.submitList(song)
                    }
                }
                Resource.Status.ERROR -> Unit
                Resource.Status.LOADING -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun initAdapter() {
        binding.rvSong.adapter = songAdapter
    }

    private fun hideProgressBar() {
        binding.allSongsProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.allSongsProgressBar.visibility = View.VISIBLE
    }
}