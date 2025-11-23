package com.sanda.truckdoc.updater.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_admin -> {
                startActivity(Intent(this, AdminSettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showAboutDialog() {
        val appVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "Unknown"
        }
        
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("TruckDoc Updater")
            .setMessage("Version: $appVersion\n\nManages updates for TruckDoc mobile applications.")
            .setPositiveButton("OK", null)
            .show()
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
        // Manual check for updates button
        binding.checkButton.setOnClickListener {
            viewModel.checkForUpdates()
        }
        
        // Download client app update
        binding.downloadClientButton.setOnClickListener {
            viewModel.downloadUpdate(DownloadTarget.CLIENT_APP)
        }
        
        // Download updater update
        binding.downloadUpdaterButton.setOnClickListener {
            viewModel.downloadUpdate(DownloadTarget.UPDATER_APP)
        }
        
        // Legacy install button (for backward compatibility)
        binding.installButton.setOnClickListener {
            // Auto-download client app by default
            viewModel.downloadUpdate(DownloadTarget.CLIENT_APP)
        }
        
        // Settings
        binding.settingsFab.setOnClickListener {
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
                binding.checkButton.isEnabled = false
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
                binding.installButton.visibility = View.GONE
            }
            
            is UiState.NoUpdateAvailable -> {
                binding.statusText.setText(R.string.no_updates_available)
                binding.progressCard.visibility = View.GONE
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
                binding.installButton.visibility = View.GONE
                binding.checkButton.isEnabled = true
            }
            
            is UiState.UpdateAvailable -> {
                val systemUpdate = state.systemUpdate
                
                // Show appropriate status message
                val updateCount = listOf(
                    systemUpdate.clientAppUpdate.updateAvailable,
                    systemUpdate.updaterAppUpdate.updateAvailable
                ).count { it }
                
                binding.statusText.text = when {
                    updateCount == 2 -> "Updates available for both apps"
                    systemUpdate.clientAppUpdate.updateAvailable -> "Client app update available"
                    systemUpdate.updaterAppUpdate.updateAvailable -> "Updater update available"
                    else -> "No updates available"
                }
                
                binding.progressCard.visibility = View.GONE
                
                // Show download buttons based on what needs updating
                binding.downloadClientButton.visibility = if (systemUpdate.clientAppUpdate.updateAvailable) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                binding.downloadUpdaterButton.visibility = if (systemUpdate.updaterAppUpdate.updateAvailable) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                binding.installButton.visibility = View.GONE
                binding.checkButton.isEnabled = true
            }
            
            is UiState.Downloading -> {
                binding.statusText.text = "Downloading ${state.target.displayName}..."
                binding.progressCard.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = false
                binding.checkButton.isEnabled = false
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
                binding.installButton.visibility = View.GONE
            }
            
            is UiState.DownloadComplete -> {
                binding.statusText.text = "${state.target.displayName} download complete - Installing..."
                binding.progressCard.visibility = View.GONE
                binding.checkButton.isEnabled = true
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
                binding.installButton.visibility = View.GONE
                
                // Auto-install the APK
                installApk(state.file)
            }
            
            is UiState.Installing -> {
                binding.statusText.text = "Installing update..."
                binding.progressCard.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
                binding.checkButton.isEnabled = false
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
                binding.installButton.visibility = View.GONE
            }
            
            is UiState.Error -> {
                binding.statusText.text = state.message
                binding.progressCard.visibility = View.GONE
                binding.checkButton.isEnabled = true
                binding.downloadClientButton.visibility = View.GONE
                binding.downloadUpdaterButton.visibility = View.GONE
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