package com.focuswechat

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.focuswechat.databinding.ActivityMainBinding
import com.focuswechat.service.VideoMonitorService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkServiceStatus()
        loadTodayStats()
    }
    
    override fun onResume() {
        super.onResume()
        checkServiceStatus()
        loadTodayStats()
    }
    
    private fun setupUI() {
        // Service toggle
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableService()
            } else {
                disableService()
            }
        }
        
        // Quick settings buttons
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        binding.btnStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        
        // Quick time limit buttons
        binding.btnLimit15.setOnClickListener { setDailyLimit(15) }
        binding.btnLimit30.setOnClickListener { setDailyLimit(30) }
        binding.btnLimit60.setOnClickListener { setDailyLimit(60) }
        binding.btnLimitCustom.setOnClickListener { showCustomLimitDialog() }
    }
    
    private fun checkServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        binding.switchService.isChecked = isEnabled
        
        if (isEnabled) {
            binding.tvServiceStatus.text = "✅ 监测服务已开启"
            binding.tvServiceStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvServiceStatus.text = "⚠️ 需要开启无障碍服务"
            binding.tvServiceStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
        }
    }
    
    private fun loadTodayStats() {
        lifecycleScope.launch {
            val today = dateFormat.format(Date())
            val record = FocusWeChatApp.database.usageRecordDao().getRecord(today)
            
            val usedMinutes = (record?.totalSeconds ?: 0) / 60
            val limitMinutes = FocusWeChatApp.prefs.dailyLimitMinutes
            val remainingMinutes = (limitMinutes - usedMinutes).coerceAtLeast(0)
            
            // Update progress circle
            val progress = if (limitMinutes > 0) {
                (usedMinutes.toFloat() / limitMinutes * 100).toInt()
            } else 0
            
            binding.progressCircle.progress = progress.coerceAtMost(100)
            binding.tvProgressText.text = "$usedMinutes/$limitMinutes"
            
            // Update stats
            binding.tvUsedTime.text = "今日已使用: ${formatDuration(usedMinutes * 60)}"
            binding.tvRemainingTime.text = "剩余可用: ${formatDuration(remainingMinutes * 60)}"
            binding.tvBlockCount.text = "已拦截次数: ${record?.blockCount ?: 0}"
            
            // Check if in whitelist time
            if (isInWhitelistTime()) {
                binding.tvWhitelistStatus.text = "🟢 当前为白名单时段，不限制"
                binding.tvWhitelistStatus.visibility = android.view.View.VISIBLE
            } else {
                binding.tvWhitelistStatus.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun enableService() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            binding.switchService.isChecked = false
        } else {
            FocusWeChatApp.prefs.serviceEnabled = true
            Toast.makeText(this, "监测服务已启动", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun disableService() {
        FocusWeChatApp.prefs.serviceEnabled = false
        Toast.makeText(this, "监测服务已停止", Toast.LENGTH_SHORT).show()
    }
    
    private fun setDailyLimit(minutes: Int) {
        FocusWeChatApp.prefs.dailyLimitMinutes = minutes
        loadTodayStats()
        Toast.makeText(this, "每日限制已设置为 ${minutes} 分钟", Toast.LENGTH_SHORT).show()
    }
    
    private fun showCustomLimitDialog() {
        // TODO: Show dialog for custom limit input
        Toast.makeText(this, "自定义设置请前往设置页面", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SettingsActivity::class.java))
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        return enabledServices.contains(packageName)
    }
    
    private fun isInWhitelistTime(): Boolean {
        val prefs = FocusWeChatApp.prefs
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = hour * 60 + minute
        
        // Check lunch time
        if (prefs.whitelistLunchEnabled) {
            val lunchStart = parseTime(prefs.lunchStart)
            val lunchEnd = parseTime(prefs.lunchEnd)
            if (currentTime in lunchStart..lunchEnd) return true
        }
        
        // Check evening
        if (prefs.whitelistEveningEnabled) {
            val eveningStart = parseTime(prefs.eveningStart)
            if (currentTime >= eveningStart) return true
        }
        
        return false
    }
    
    private fun parseTime(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
    
    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "${seconds}秒"
        }
    }
}