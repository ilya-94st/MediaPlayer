package com.example.mediaplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.ItemsSongAdapterBinding
import com.example.mediaplayer.date.entinities.Song

class SwipeSongAdapter(): RecyclerView.Adapter<SwipeSongAdapter.SongViewHolder>() {

    inner class SongViewHolder(var binding: ItemsSongAdapterBinding): RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Song>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemsSongAdapterBinding.inflate(LayoutInflater.from(parent.context), parent , false)
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