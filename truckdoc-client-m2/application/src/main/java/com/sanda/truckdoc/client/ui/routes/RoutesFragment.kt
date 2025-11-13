package com.sanda.truckdoc.client.ui.routes

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
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import com.sanda.truckdoc.client.databinding.FragmentRoutesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoutesFragment : Fragment() {
    private var _binding: FragmentRoutesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RoutesViewModel by viewModels()
    private val routesAdapter = RoutesAdapter(
        onRouteClick = { route: DbRouteAssignment -> handleRouteClick(route) },
        onRouteLongClick = { route: DbRouteAssignment -> handleRouteLongClick(route) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routesAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddRoute.setOnClickListener {
            showAddRouteDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshRoutes()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe routes
                launch {
                    viewModel.routes.collectLatest { routes ->
                        routesAdapter.submitList(routes)
                        updateEmptyState(routes.isEmpty())
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.swipeRefresh.isRefreshing = isLoading
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

    private fun handleRouteClick(route: DbRouteAssignment) {
        // Handle route click
    }

    private fun handleRouteLongClick(route: DbRouteAssignment) {
        // Show route options menu
    }

    private fun showAddRouteDialog() {
        // Show add route dialog
    }

    private fun refreshRoutes() {
        // Refresh routes from server
    }

    private fun showError(error: String) {
        // Show error message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 