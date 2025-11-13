package com.sanda.truckdoc.client.ui.locations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanda.truckdoc.client.data.model.DbLocation
import com.sanda.truckdoc.client.databinding.ItemLocationBinding
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class LocationsAdapter(
    private val onLocationClick: (DbLocation) -> Unit,
    private val onLocationLongClick: (DbLocation) -> Unit
) : ListAdapter<DbLocation, LocationsAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLocationClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLocationLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(location: DbLocation) {
            binding.apply {
                tvLatitude.text = "Lat: ${location.latitude}"
                tvLongitude.text = "Lng: ${location.longitude}"
                tvTime.text = DateTime(location.time).toString(DateTimeFormat.forPattern("MMM dd, yyyy HH:mm"))
                tvAccuracy.text = "Accuracy: ${location.accuracy}m"
            }
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<DbLocation>() {
        override fun areItemsTheSame(oldItem: DbLocation, newItem: DbLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DbLocation, newItem: DbLocation): Boolean {
            return oldItem == newItem
        }
    }
} 