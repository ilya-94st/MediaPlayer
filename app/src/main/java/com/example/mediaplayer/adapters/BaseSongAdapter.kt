package com.example.mediaplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.date.entinities.Song

abstract class BaseSongAdapter(private val layoutId: Int) : RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

   protected abstract var differ: AsyncListDiffer<Song>

    fun submitList(list: List<Song>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    protected var onItemClickListener: (Song)->Unit = { song: Song -> Unit }

    fun setItemClickListener(listener: (Song) ->Unit) {
        onItemClickListener = listener
    }
}