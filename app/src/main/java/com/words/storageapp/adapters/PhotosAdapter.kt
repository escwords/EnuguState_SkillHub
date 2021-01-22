package com.words.storageapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.words.storageapp.databinding.ItemAlbumListBinding
import com.words.storageapp.domain.Photo

class PhotosAdapter : ListAdapter<Photo, PhotosAdapter.PhotoViewHolder>(itemUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class PhotoViewHolder(val binding: ItemAlbumListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoItem: Photo) {
            binding.photo = photoItem

            Glide.with(itemView.context)
                .load(photoItem.photoUrl)
                .into(binding.imageItem)

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): PhotoViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return PhotoViewHolder(ItemAlbumListBinding.inflate(inflater, parent, false))
            }
        }
    }

    companion object {
        val itemUtil = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem.photoId == newItem.photoId
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }

        }
    }
}