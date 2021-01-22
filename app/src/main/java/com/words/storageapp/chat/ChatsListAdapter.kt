package com.words.storageapp.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.databinding.ItemChatListBinding
import com.words.storageapp.domain.ChatData


class ChatsListAdapter(private val clickListener: ItemClickListener) :
    ListAdapter<ChatData, ChatsListAdapter.ChatsViewModel>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewModel {
        val inflater = LayoutInflater.from(parent.context)
        return ChatsViewModel(ItemChatListBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: ChatsViewModel, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ChatsViewModel(val binding: ItemChatListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatData, clickListener: ItemClickListener) {
            binding.uiModel = data
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatData>() {
            override fun areItemsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
                return oldItem.clientId == newItem.clientId
            }

            override fun areContentsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
                return oldItem == newItem
            }
        }
    }

}

class ItemClickListener(private val clickListener: (id: String) -> Unit) {
    fun onClick(id: String) = clickListener(id)
}