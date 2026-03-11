package com.focuswechat

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.focuswechat.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadSettings()
    }
    
    private fun setupUI() {
        // Daily limit slider
        binding.seekBarDailyLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = (progress + 1) * 5 // 5-180 minutes
                binding.tvDailyLimitValue.text = "${minutes} 分钟"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Session limit slider
        binding.seekBarSessionLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = (progress + 1) * 5 // 5-30 minutes
                binding.tvSessionLimitValue.text = "${minutes} 分钟"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Save button
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
        
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun loadSettings() {
        val prefs = FocusWeChatApp.prefs
        
        // Daily limit
        val dailyLimit = prefs.dailyLimitMinutes
        binding.seekBarDailyLimit.progress = (dailyLimit / 5) - 1
        binding.tvDailyLimitValue.text = "${dailyLimit} 分钟"
        
        // Session limit
        val sessionLimit = prefs.sessionLimitMinutes
        binding.seekBarSessionLimit.progress = (sessionLimit / 5) - 1
        binding.tvSessionLimitValue.text = "${sessionLimit} 分钟"
        
        // Whitelist settings
        binding.switchLunch.isChecked = prefs.whitelistLunchEnabled
        binding.switchEvening.isChecked = prefs.whitelistEveningEnabled
        
        // Time pickers
        binding.tvLunchStart.text = prefs.lunchStart
        binding.tvLunchEnd.text = prefs.lunchEnd
        binding.tvEveningStart.text = prefs.eveningStart
        
        // Feature toggles
        binding.switchNotification.isChecked = prefs.notificationsEnabled
        binding.switchReminder.isChecked = prefs.reminderEnabled
    }
    
    private fun saveSettings() {
        val prefs = FocusWeChatApp.prefs
        
        // Save limits
        prefs.dailyLimitMinutes = (binding.seekBarDailyLimit.progress + 1) * 5
        prefs.sessionLimitMinutes = (binding.seekBarSessionLimit.progress + 1) * 5
        
        // Save whitelist settings
        prefs.whitelistLunchEnabled = binding.switchLunch.isChecked
        prefs.whitelistEveningEnabled = binding.switchEvening.isChecked
        
        // Save times
        prefs.lunchStart = binding.tvLunchStart.text.toString()
        prefs.lunchEnd = binding.tvLunchEnd.text.toString()
        prefs.eveningStart = binding.tvEveningStart.text.toString()
        
        // Save feature toggles
        prefs.notificationsEnabled = binding.switchNotification.isChecked
        prefs.reminderEnabled = binding.switchReminder.isChecked
        
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
}