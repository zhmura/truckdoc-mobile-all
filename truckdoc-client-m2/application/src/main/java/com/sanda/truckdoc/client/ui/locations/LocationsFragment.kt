package com.sanda.truckdoc.client.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanda.truckdoc.client.databinding.FragmentLocationsBinding
import com.sanda.truckdoc.client.data.model.DbLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsFragment : Fragment() {
    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LocationsViewModel by viewModels()
    private val locationsAdapter = LocationsAdapter(
        onLocationClick = { location -> handleLocationClick(location) },
        onLocationLongClick = { location -> handleLocationLongClick(location) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationsAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe locations
                launch {
                    viewModel.locations.collectLatest { locations ->
                        locationsAdapter.submitList(locations)
                        updateEmptyState(locations.isEmpty())
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                // Observe error state
                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let { showError(it) }
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun handleLocationClick(location: DbLocation) {
        // Handle location click
    }

    private fun handleLocationLongClick(location: DbLocation) {
        // Show location options menu
    }

    private fun showError(error: String) {
        // Show error message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 