package com.words.storageapp.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.R

class MessageListAdapter(private val chatMessages: List<MessageData>, private val uid: String) :
    RecyclerView.Adapter<MessageListAdapter.ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatListViewHolder(inflater.inflate(R.layout.item_messages_list, parent, false))
    }

    override fun getItemCount(): Int = chatMessages.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chat = chatMessages[position]
        holder.bind(chat, uid)
    }

    inner class ChatListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val chatSent: TextView = itemView.findViewById<TextView>(R.id.ChatSent)
        private val chatReceived: TextView = itemView.findViewById<TextView>(R.id.ChatReceived)

        fun bind(chatData: MessageData, uid: String) {

            if (uid == chatData.senderId) {
                chatSent.text = chatData.messageText
                chatReceived.visibility = View.GONE
            } else {
                chatReceived.text = chatData.messageText
                chatSent.visibility = View.GONE
            }
        }
    }
}