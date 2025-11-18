package com.sanda.truckdoc.updater.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            updateUi(state)
        }
        
        viewModel.systemUpdateInfo.observe(this) { systemUpdate ->
            updateVersionInfo(systemUpdate)
        }
        
        viewModel.downloadProgress.observe(this) { progress ->
            updateDownloadProgress(progress)
        }
    }
    
    private fun setupClickListeners() {
        binding.checkButton.setOnClickListener {
            viewModel.checkForUpdates()
        }
        
        binding.installButton.setOnClickListener {
            // Handle install button click
        }
        
        binding.settingsFab.setOnClickListener {
            // Open settings
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun updateUi(state: UiState) {
        when (state) {
            is UiState.Loading -> {
                binding.statusText.setText(R.string.checking_for_updates)
                binding.progressCard.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
            }
            
            is UiState.NoUpdateAvailable -> {
                binding.statusText.setText(R.string.no_updates_available)
                binding.progressCard.visibility = View.GONE
                binding.installButton.visibility = View.GONE
            }
            
            is UiState.UpdateAvailable -> {
                binding.statusText.setText(R.string.update_available)
                binding.progressCard.visibility = View.GONE
                binding.installButton.visibility = View.VISIBLE
            }
            
            is UiState.Downloading -> {
                binding.statusText.text = "Downloading ${state.target.displayName}..."
                binding.progressCard.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = false
            }
            
            is UiState.DownloadComplete -> {
                binding.statusText.text = "${state.target.displayName} download complete"
                binding.progressCard.visibility = View.GONE
                binding.installButton.visibility = View.VISIBLE
                
                // Auto-install the APK
                installApk(state.file)
            }
            
            is UiState.Installing -> {
                binding.statusText.text = "Installing update..."
                binding.progressCard.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
            }
            
            is UiState.Error -> {
                binding.statusText.text = state.message
                binding.progressCard.visibility = View.GONE
                binding.installButton.visibility = View.GONE
                
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateVersionInfo(systemUpdate: com.sanda.truckdoc.updater.data.model.SystemUpdateInfo) {
        // Client App Info
        val clientUpdate = systemUpdate.clientAppUpdate
        binding.currentVersionText.text = buildString {
            append("Client: ${clientUpdate.currentVersion.versionName}")
            if (clientUpdate.updateAvailable) {
                append(" → ${clientUpdate.latestVersion?.versionName ?: "?"}")
            }
        }
        
        // Updater App Info (show separately if needed)
        val updaterUpdate = systemUpdate.updaterAppUpdate
        if (updaterUpdate.updateAvailable) {
            binding.latestVersionText.text = buildString {
                append("Updater: ${updaterUpdate.currentVersion.versionName}")
                append(" → ${updaterUpdate.latestVersion?.versionName ?: "?"}")
            }
        } else {
            clientUpdate.latestVersion?.let { latest ->
                binding.latestVersionText.text = "Latest: ${latest.versionName}"
            }
        }
        
        binding.lastCheckText.text = getString(
            R.string.last_check, 
            viewModel.getFormattedLastCheckTime(systemUpdate.lastCheckTime)
        )
    }
    
    private fun updateDownloadProgress(progress: DownloadProgress) {
        binding.progressText.text = progress.message
        binding.progressBar.progress = progress.progress
    }
    
    private fun installApk(file: java.io.File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(file)
            }
            
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            startActivity(intent)
            
        } catch (e: Exception) {
            Snackbar.make(
                binding.root, 
                "Failed to install update: ${e.message}", 
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
} 