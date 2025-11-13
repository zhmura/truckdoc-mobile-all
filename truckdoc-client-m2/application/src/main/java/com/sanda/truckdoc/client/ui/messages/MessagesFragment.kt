package com.sanda.truckdoc.client.ui.messages

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
import com.sanda.truckdoc.client.R
import com.sanda.truckdoc.client.data.model.ServerMessage
import com.sanda.truckdoc.client.databinding.FragmentMessagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessagesFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MessagesViewModel by viewModels()
    private val messagesAdapter = MessagesAdapter(
        onMessageClick = { message -> handleMessageClick(message) },
        onMessageLongClick = { message -> handleMessageLongClick(message) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
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
            adapter = messagesAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe messages
                launch {
                    viewModel.messages.collectLatest { messages ->
                        messagesAdapter.submitList(messages)
                        updateEmptyState(messages.isEmpty())
                    }
                }

                // Observe pending messages
                launch {
                    viewModel.pendingOutMessages.collectLatest { pendingMessages ->
                        updatePendingMessagesCount(pendingMessages.size)
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

    private fun updatePendingMessagesCount(count: Int) {
        binding.pendingMessagesCount.text = getString(R.string.pending_messages_count, count)
        binding.pendingMessagesCount.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun handleMessageClick(message: ServerMessage) {
        // Handle message click
    }

    private fun handleMessageLongClick(message: ServerMessage) {
        // Show message options menu
    }

    private fun showError(error: String) {
        // Show error message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 