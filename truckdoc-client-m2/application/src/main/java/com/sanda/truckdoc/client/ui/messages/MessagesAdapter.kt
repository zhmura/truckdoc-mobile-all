package com.sanda.truckdoc.client.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.model.ServerMessage
import com.sanda.truckdoc.client.databinding.ItemMessageBinding
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class MessagesAdapter(
    private val onMessageClick: (ServerMessage) -> Unit,
    private val onMessageLongClick: (ServerMessage) -> Unit
) : ListAdapter<ServerMessage, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMessageClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMessageLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(message: ServerMessage) {
            binding.apply {
                tvSubject.text = message.message.takeIf { it.isNotEmpty() } ?: "No Subject"
                tvSender.text = message.sender.takeIf { it.isNotEmpty() } ?: "Unknown Sender"
                tvTime.text = DateTime(message.savedDate).toString(DateTimeFormat.forPattern("MMM dd, yyyy HH:mm"))
                tvContent.text = message.message.takeIf { it.isNotEmpty() } ?: "No content"
                
                // Hide attachment indicator since hasAttachments property doesn't exist
                ivAttachment.visibility = android.view.View.GONE
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<ServerMessage>() {
        override fun areItemsTheSame(oldItem: ServerMessage, newItem: ServerMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ServerMessage, newItem: ServerMessage): Boolean {
            return oldItem == newItem
        }
    }
} 