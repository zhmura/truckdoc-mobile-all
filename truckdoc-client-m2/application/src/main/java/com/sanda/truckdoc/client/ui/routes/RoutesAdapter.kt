package com.sanda.truckdoc.client.ui.routes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import com.sanda.truckdoc.client.databinding.ItemRouteBinding

class RoutesAdapter(
    private val onRouteClick: (DbRouteAssignment) -> Unit,
    private val onRouteLongClick: (DbRouteAssignment) -> Unit
) : ListAdapter<DbRouteAssignment, RoutesAdapter.RouteViewHolder>(RouteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RouteViewHolder(
        private val binding: ItemRouteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRouteClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRouteLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(route: DbRouteAssignment) {
            binding.apply {
                tvName.text = route.name
                tvDescription.text = route.description
                tvServerId.text = "Server ID: ${route.serverAssignmentId}"
            }
        }
    }

    private class RouteDiffCallback : DiffUtil.ItemCallback<DbRouteAssignment>() {
        override fun areItemsTheSame(oldItem: DbRouteAssignment, newItem: DbRouteAssignment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbRouteAssignment, newItem: DbRouteAssignment): Boolean {
            return oldItem == newItem
        }
    }
} 