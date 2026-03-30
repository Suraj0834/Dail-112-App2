package com.dial112.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dial112.databinding.ItemChatMessageBotBinding
import com.dial112.databinding.ItemChatMessageUserBinding
import com.dial112.model.ChatMessage

class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemChatMessageUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                UserMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemChatMessageBotBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                BotMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class UserMessageViewHolder(
        private val binding: ItemChatMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.timestamp
        }
    }

    inner class BotMessageViewHolder(
        private val binding: ItemChatMessageBotBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text
            binding.tvTime.text = message.timestamp
        }
    }
}
