package com.sanda.truckdoc.updater.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.util.UpdateScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
    
    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {
        
        @Inject
        lateinit var updateScheduler: UpdateScheduler
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            setupAutoCheckPreference()
            setupCheckIntervalPreference()
        }
        
        private fun setupAutoCheckPreference() {
            val autoCheckPreference = findPreference<SwitchPreferenceCompat>("auto_check_enabled")
            autoCheckPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    val intervalPreference = findPreference<ListPreference>("check_interval")
                    val intervalHours = when (intervalPreference?.value) {
                        "1" -> 1L
                        "6" -> 6L
                        "12" -> 12L
                        "24" -> 24L
                        else -> 6L
                    }
                    updateScheduler.schedulePeriodicUpdateCheck(intervalHours)
                } else {
                    updateScheduler.cancelPeriodicUpdateCheck()
                }
                true
            }
        }
        
        private fun setupCheckIntervalPreference() {
            val intervalPreference = findPreference<ListPreference>("check_interval")
            intervalPreference?.setOnPreferenceChangeListener { _, newValue ->
                val autoCheckPreference = findPreference<SwitchPreferenceCompat>("auto_check_enabled")
                if (autoCheckPreference?.isChecked == true) {
                    val intervalHours = when (newValue) {
                        "1" -> 1L
                        "6" -> 6L
                        "12" -> 12L
                        "24" -> 24L
                        else -> 6L
                    }
                    updateScheduler.schedulePeriodicUpdateCheck(intervalHours)
                }
                true
            }
        }
    }
} 