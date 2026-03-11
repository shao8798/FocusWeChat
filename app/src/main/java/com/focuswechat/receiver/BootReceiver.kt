package com.focuswechat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.focuswechat.service.MonitorForegroundService
import com.focuswechat.service.VideoMonitorService

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, checking if service should start")
            
            // Check if accessibility service was enabled
            if (isAccessibilityServiceEnabled(context)) {
                Log.d(TAG, "Starting monitoring service")
                val serviceIntent = Intent(context, MonitorForegroundService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
    
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val serviceName = "${context.packageName}/${VideoMonitorService::class.java.canonicalName}"
        return enabledServices.contains(serviceName)
    }
}