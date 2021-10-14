package com.example.mediaplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.SwipeItemsBinding
import com.example.mediaplayer.date.entinities.Song

class SwipeSongAdapter(): RecyclerView.Adapter<SwipeSongAdapter.SongViewHolder>() {

    inner class SongViewHolder(var binding: SwipeItemsBinding): RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SwipeItemsBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = differ.currentList[position]
        val text = "${song.title} - ${song.subtitle}"
        holder.binding.tvPrimary.text = text
        holder.itemView.setOnClickListener {
            onItemClickListener(song)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    private var onItemClickListener: (Song)->Unit = { song: Song -> Unit }

    fun setOnItemClickListener(listener: (Song) ->Unit) {
        onItemClickListener = listener
    }
}