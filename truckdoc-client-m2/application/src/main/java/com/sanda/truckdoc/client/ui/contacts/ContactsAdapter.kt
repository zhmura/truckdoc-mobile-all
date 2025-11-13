package com.sanda.truckdoc.client.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.model.DbContactRecord
import com.sanda.truckdoc.client.databinding.ItemContactBinding

class ContactsAdapter(
    private val onContactClick: (DbContactRecord) -> Unit,
    private val onContactLongClick: (DbContactRecord) -> Unit
) : ListAdapter<DbContactRecord, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onContactClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onContactLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(contact: DbContactRecord) {
            binding.apply {
                tvName.text = contact.label
                tvPhone.text = contact.phone
                tvRole.text = contact.role
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<DbContactRecord>() {
        override fun areItemsTheSame(oldItem: DbContactRecord, newItem: DbContactRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbContactRecord, newItem: DbContactRecord): Boolean {
            return oldItem == newItem
        }
    }
} 