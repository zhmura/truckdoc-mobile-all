package com.sanda.truckdoc.updater.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.config.GitHubConfig
import com.sanda.truckdoc.updater.databinding.ActivityAdminSettingsBinding
import com.sanda.truckdoc.updater.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAdminSettingsBinding
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private var isAuthenticated = false
    
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
        
        // Show if using custom repo
        if (preferencesManager.hasCustomRepo()) {
            binding.customRepoStatus.text = "Using custom repository"
        } else {
            binding.customRepoStatus.text = "Using default repository: ${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}"
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
        binding.testConnectionButton.setOnClickListener {
            testConnection()
        }
    }
    
    private fun saveSettings() {
        val owner = binding.repoOwnerInput.text.toString().trim()
        val repo = binding.repoNameInput.text.toString().trim()
        
        if (owner.isEmpty() || repo.isEmpty()) {
            Toast.makeText(this, "Owner and repository name are required", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate format (basic check)
        if (!owner.matches(Regex("[a-zA-Z0-9-_]+"))) {
            Toast.makeText(this, "Invalid owner format", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!repo.matches(Regex("[a-zA-Z0-9-_.]+"))) {
            Toast.makeText(this, "Invalid repository name format", Toast.LENGTH_SHORT).show()
            return
        }
        
        preferencesManager.customRepoOwner = owner
        preferencesManager.customRepoName = repo
        
        binding.customRepoStatus.text = "Using custom repository: $owner/$repo"
        
        Toast.makeText(this, "Settings saved. Restart app to apply changes.", Toast.LENGTH_LONG).show()
    }
    
    private fun resetToDefault() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset to Default")
            .setMessage("Reset to default GitHub repository?\n\n${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}")
            .setPositiveButton("Reset") { _, _ ->
                preferencesManager.clearCustomRepo()
                binding.repoOwnerInput.setText(GitHubConfig.REPO_OWNER)
                binding.repoNameInput.setText(GitHubConfig.REPO_NAME)
                binding.customRepoStatus.text = "Using default repository: ${GitHubConfig.REPO_OWNER}/${GitHubConfig.REPO_NAME}"
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
    
    private fun testConnection() {
        val owner = binding.repoOwnerInput.text.toString().trim()
        val repo = binding.repoNameInput.text.toString().trim()
        
        if (owner.isEmpty() || repo.isEmpty()) {
            Toast.makeText(this, "Enter owner and repository name first", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.testConnectionButton.isEnabled = false
        binding.testConnectionButton.text = "Testing..."
        
        // Test connection in background
        // For now, just show success message
        // TODO: Implement actual API test
        binding.testConnectionButton.postDelayed({
            binding.testConnectionButton.isEnabled = true
            binding.testConnectionButton.text = "Test Connection"
            
            val testUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            Toast.makeText(
                this, 
                "Test URL: $testUrl\n\nImplement actual test in production", 
                Toast.LENGTH_LONG
            ).show()
        }, 1000)
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

