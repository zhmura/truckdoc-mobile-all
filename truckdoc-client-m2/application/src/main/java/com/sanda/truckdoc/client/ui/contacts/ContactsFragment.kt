package com.sanda.truckdoc.client.ui.contacts

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
import com.sanda.truckdoc.client.databinding.FragmentContactsBinding
import com.sanda.truckdoc.client.data.model.DbContactRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ContactsViewModel by viewModels()
    private val contactsAdapter = ContactsAdapter(
        onContactClick = { contact -> handleContactClick(contact) },
        onContactLongClick = { contact -> handleContactLongClick(contact) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
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
            adapter = contactsAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshContacts()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe contacts
                launch {
                    viewModel.contacts.collectLatest { contacts ->
                        contactsAdapter.submitList(contacts)
                        updateEmptyState(contacts.isEmpty())
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

    private fun handleContactClick(contact: DbContactRecord) {
        // Show contact details
    }

    private fun handleContactLongClick(contact: DbContactRecord) {
        // Show contact options menu
    }

    private fun showAddContactDialog() {
        // Show add contact dialog
    }

    private fun refreshContacts() {
        // Refresh contacts from server
    }

    private fun showError(error: String) {
        // Show error message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 