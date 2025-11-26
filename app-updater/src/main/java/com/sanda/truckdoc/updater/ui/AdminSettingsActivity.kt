package com.sanda.truckdoc.updater.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.config.CustomServerConfig
import com.sanda.truckdoc.updater.config.GitHubConfig
import com.sanda.truckdoc.updater.databinding.ActivityAdminSettingsBinding
import com.sanda.truckdoc.updater.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@AndroidEntryPoint
class AdminSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAdminSettingsBinding
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private var isAuthenticated = false
    private val manifestTestClient by lazy { OkHttpClient() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Settings"
        
        // Show password dialog first
        showPasswordDialog()
    }
    
    private fun showPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.passwordInput)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Admin Authentication")
            .setMessage("Enter admin password to access settings")
            .setView(dialogView)
            .setPositiveButton("Authenticate") { _, _ ->
                val password = passwordInput.text.toString()
                if (preferencesManager.verifyAdminPassword(password)) {
                    isAuthenticated = true
                    setupSettings()
                } else {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun setupSettings() {
        // Load current settings
        val (currentOwner, currentRepo) = preferencesManager.getGitHubRepoConfig()
        
        binding.repoOwnerInput.setText(currentOwner)
        binding.repoNameInput.setText(currentRepo)

        binding.customServerSwitch.isChecked = preferencesManager.isCustomServerEnabled
        binding.customServerManifestInput.setText(preferencesManager.customServerManifestUrl)
        toggleCustomServerFields(binding.customServerSwitch.isChecked)
        updateRepoStatus()

        binding.customServerSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleCustomServerFields(isChecked)
        }
        
        // Save button
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
        
        // Reset to default button
        binding.resetButton.setOnClickListener {
            resetToDefault()
        }
        
        // Change password button
        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        
        // Test connection button
        binding.testGitHubButton.setOnClickListener {
            testGitHubConnection()
        }

        binding.testJenkinsButton.setOnClickListener {
            testJenkinsManifest()
        }
    }
    
    private fun saveSettings() {
        val owner = binding.repoOwnerInput.text.toString().trim()
        val repo = binding.repoNameInput.text.toString().trim()
        val useCustomServer = binding.customServerSwitch.isChecked
        val manifestUrl = binding.customServerManifestInput.text.toString().trim()

        if (!useCustomServer) {
            if (owner.isEmpty() || repo.isEmpty()) {
                Toast.makeText(this, "Owner and repository name are required", Toast.LENGTH_SHORT).show()
                return
            }

            if (!owner.matches(Regex("[a-zA-Z0-9-_]+"))) {
                Toast.makeText(this, "Invalid owner format", Toast.LENGTH_SHORT).show()
                return
            }

            if (!repo.matches(Regex("[a-zA-Z0-9-_.]+"))) {
                Toast.makeText(this, "Invalid repository name format", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (useCustomServer) {
            if (manifestUrl.isEmpty()) {
                Toast.makeText(this, "Manifest URL is required when using Jenkins server", Toast.LENGTH_SHORT).show()
                return
            }

            if (!manifestUrl.startsWith("http")) {
                Toast.makeText(this, "Manifest URL must start with http or https", Toast.LENGTH_SHORT).show()
                return
            }

            preferencesManager.customServerManifestUrl = manifestUrl
        }

        preferencesManager.isCustomServerEnabled = useCustomServer

        if (owner.isNotEmpty() && repo.isNotEmpty()) {
            preferencesManager.customRepoOwner = owner
            preferencesManager.customRepoName = repo
        }

        updateRepoStatus()

        val modeLabel = if (useCustomServer) "Jenkins server" else "GitHub repository"
        Toast.makeText(this, "Settings saved. Restart app to use $modeLabel.", Toast.LENGTH_LONG).show()
    }
    
    private fun resetToDefault() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset to Default")
            .setMessage("Reset to default GitHub repository?\n\n${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}")
            .setPositiveButton("Reset") { _, _ ->
                preferencesManager.clearCustomRepo()
                binding.repoOwnerInput.setText(GitHubConfig.REPO_OWNER)
                binding.repoNameInput.setText(GitHubConfig.REPO_NAME)
                preferencesManager.isCustomServerEnabled = false
                preferencesManager.customServerManifestUrl = CustomServerConfig.defaultManifestUrl()
                binding.customServerSwitch.isChecked = false
                binding.customServerManifestInput.setText(preferencesManager.customServerManifestUrl)
                toggleCustomServerFields(false)
                updateRepoStatus()
                Toast.makeText(this, "Reset to default. Restart app to apply.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Change Admin Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                
                if (!preferencesManager.verifyAdminPassword(currentPassword)) {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                preferencesManager.setAdminPassword(newPassword)
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun testGitHubConnection() {
        val owner = binding.repoOwnerInput.text.toString().trim()
        val repo = binding.repoNameInput.text.toString().trim()
        
        if (owner.isEmpty() || repo.isEmpty()) {
            Toast.makeText(this, "Enter owner and repository name first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val defaultText = binding.testGitHubButton.text
        binding.testGitHubButton.isEnabled = false
        binding.testGitHubButton.text = "Testing..."
        
        // Test connection in background
        // For now, just show success message
        // TODO: Implement actual API test
        binding.testGitHubButton.postDelayed({
            binding.testGitHubButton.isEnabled = true
            binding.testGitHubButton.text = defaultText
            val testUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            Toast.makeText(
                this, 
                "Test URL: $testUrl\n\nImplement actual test in production", 
                Toast.LENGTH_LONG
            ).show()
        }, 1000)
    }

    private fun testJenkinsManifest() {
        val manifestUrl = binding.customServerManifestInput.text.toString().trim()
        if (manifestUrl.isEmpty()) {
            Toast.makeText(this, "Enter manifest URL first", Toast.LENGTH_SHORT).show()
            return
        }

        val defaultText = binding.testJenkinsButton.text
        binding.testJenkinsButton.isEnabled = false
        binding.testJenkinsButton.text = "Testing..."

        lifecycleScope.launch {
            var errorMessage: String? = null
            val success = withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url(manifestUrl)
                        .get()
                        .build()
                    manifestTestClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            errorMessage = "HTTP ${response.code}"
                            return@withContext false
                        }
                        return@withContext true
                    }
                } catch (e: Exception) {
                    errorMessage = e.localizedMessage
                    false
                }
            }

            binding.testJenkinsButton.isEnabled = true
            binding.testJenkinsButton.text = defaultText

            if (success) {
                Toast.makeText(
                    this@AdminSettingsActivity,
                    "Manifest reachable. Jenkins publishing is configured.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@AdminSettingsActivity,
                    "Manifest test failed: ${errorMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun toggleCustomServerFields(enabled: Boolean) {
        binding.customServerConfigGroup.isVisible = enabled
    }

    private fun updateRepoStatus() {
        binding.customRepoStatus.text = when {
            preferencesManager.isCustomServerEnabled -> {
                val url = preferencesManager.customServerManifestUrl
                "Using Jenkins server: $url"
            }
            preferencesManager.hasCustomRepo() -> {
                "Using custom repository: ${preferencesManager.customRepoOwner}/${preferencesManager.customRepoName}"
            }
            else -> "Using default repository: ${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}"
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

