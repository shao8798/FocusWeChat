package com.focuswechat.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focuswechat.FocusWeChatApp
import com.focuswechat.data.SessionLog
import com.focuswechat.data.UsageRecord
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class VideoMonitorService : AccessibilityService() {
    
    companion object {
        private const val TAG = "VideoMonitorService"
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val CHECK_INTERVAL = 1000L // 1 second
        
        var isRunning = false
            private set
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private var isInVideoChannel = false
    private var sessionStartTime: Long = 0
    private var currentSessionId: Long = 0
    private var currentDate: String = ""
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkVideoChannelStatus()
            mainHandler.postDelayed(this, CHECK_INTERVAL)
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
        
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(WECHAT_PACKAGE)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        
        isRunning = true
        currentDate = dateFormat.format(Date())
        
        // Start foreground service
        startForegroundService()
        
        // Start periodic check
        mainHandler.post(checkRunnable)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != WECHAT_PACKAGE) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                checkVideoChannelStatus()
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isRunning = false
        mainHandler.removeCallbacks(checkRunnable)
        serviceScope.cancel()
        
        // End current session if active
        if (isInVideoChannel) {
            endSession()
        }
    }
    
    private fun startForegroundService() {
        val intent = Intent(this, MonitorForegroundService::class.java)
        startService(intent)
    }
    
    private fun checkVideoChannelStatus() {
        val rootNode = rootInActiveWindow ?: return
        val isVideoPage = isVideoChannelPage(rootNode)
        
        if (isVideoPage && !isInVideoChannel) {
            // Entered video channel
            onEnterVideoChannel()
        } else if (!isVideoPage && isInVideoChannel) {
            // Left video channel
            onLeaveVideoChannel()
        } else if (isVideoPage && isInVideoChannel) {
            // Still in video channel, check time limit
            checkTimeLimit()
        }
        
        rootNode.recycle()
    }
    
    private fun isVideoChannelPage(rootNode: AccessibilityNodeInfo): Boolean {
        // Check for video channel indicators
        // Method 1: Check window title
        val windowTitle = rootNode.findAccessibilityNodeInfosByText("视频号")
        if (windowTitle.isNotEmpty()) {
            return true
        }
        
        // Method 2: Check specific view IDs (may vary by WeChat version)
        val videoIndicators = listOf(
            "视频号",
            "Channel",
            "发现",
            "Discover"
        )
        
        for (indicator in videoIndicators) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(indicator)
            if (nodes.isNotEmpty()) {
                // Additional check: look for video feed indicators
                val videoFeed = rootNode.findAccessibilityNodeInfosByText("赞")
                val comment = rootNode.findAccessibilityNodeInfosByText("评论")
                if (videoFeed.isNotEmpty() || comment.isNotEmpty()) {
                    nodes.forEach { it.recycle() }
                    videoFeed.forEach { it.recycle() }
                    comment.forEach { it.recycle() }
                    return true
                }
                nodes.forEach { it.recycle() }
            }
        }
        
        return false
    }
    
    private fun onEnterVideoChannel() {
        Log.d(TAG, "Entered video channel")
        isInVideoChannel = true
        sessionStartTime = System.currentTimeMillis()
        
        // Check if in whitelist time
        if (isInWhitelistTime()) {
            Log.d(TAG, "In whitelist time, no limit")
            return
        }
        
        serviceScope.launch {
            val today = dateFormat.format(Date())
            
            // Create session log
            val sessionLog = SessionLog(
                date = today,
                startTime = sessionStartTime
            )
            currentSessionId = FocusWeChatApp.database.sessionLogDao().insert(sessionLog)
            
            // Get or create usage record
            var record = FocusWeChatApp.database.usageRecordDao().getRecord(today)
            if (record == null) {
                record = UsageRecord(date = today)
                FocusWeChatApp.database.usageRecordDao().insertOrUpdate(record)
            }
            
            // Show notification
            MonitorForegroundService.showMonitoringNotification(
                this@VideoMonitorService,
                "正在监测视频号使用时长"
            )
        }
    }
    
    private fun onLeaveVideoChannel() {
        Log.d(TAG, "Left video channel")
        endSession()
    }
    
    private fun endSession() {
        if (!isInVideoChannel) return
        
        isInVideoChannel = false
        val sessionEndTime = System.currentTimeMillis()
        val duration = ((sessionEndTime - sessionStartTime) / 1000).toInt()
        
        serviceScope.launch {
            // Update session log
            if (currentSessionId > 0) {
                FocusWeChatApp.database.sessionLogDao().endSession(
                    currentSessionId,
                    sessionEndTime,
                    duration
                )
            }
            
            // Update daily total
            val today = dateFormat.format(Date())
            FocusWeChatApp.database.usageRecordDao().addTime(today, duration)
            
            Log.d(TAG, "Session ended: ${duration}s")
        }
    }
    
    private fun checkTimeLimit() {
        if (isInWhitelistTime()) return
        
        val prefs = FocusWeChatApp.prefs
        val dailyLimit = prefs.dailyLimitMinutes * 60 // Convert to seconds
        val sessionLimit = prefs.sessionLimitMinutes * 60
        
        serviceScope.launch {
            val today = dateFormat.format(Date())
            val record = FocusWeChatApp.database.usageRecordDao().getRecord(today)
            val currentSession = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
            
            val totalUsed = record?.totalSeconds ?: 0
            val totalWithCurrent = totalUsed + currentSession
            
            // Check daily limit
            if (totalWithCurrent >= dailyLimit) {
                Log.d(TAG, "Daily limit reached: ${totalWithCurrent}s / ${dailyLimit}s")
                blockVideoChannel()
                return@launch
            }
            
            // Check session limit
            if (currentSession >= sessionLimit) {
                Log.d(TAG, "Session limit reached: ${currentSession}s / ${sessionLimit}s")
                blockVideoChannel()
                return@launch
            }
            
            // Check reminder (5 minutes before limit)
            val fiveMinutes = 5 * 60
            if (prefs.reminderEnabled && 
                (totalWithCurrent >= dailyLimit - fiveMinutes || 
                 currentSession >= sessionLimit - fiveMinutes)) {
                showReminderNotification()
            }
        }
    }
    
    private fun isInWhitelistTime(): Boolean {
        val prefs = FocusWeChatApp.prefs
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = hour * 60 + minute // Minutes since midnight
        
        // Check lunch time
        if (prefs.whitelistLunchEnabled) {
            val lunchStart = parseTime(prefs.lunchStart)
            val lunchEnd = parseTime(prefs.lunchEnd)
            if (currentTime in lunchStart..lunchEnd) {
                return true
            }
        }
        
        // Check evening time
        if (prefs.whitelistEveningEnabled) {
            val eveningStart = parseTime(prefs.eveningStart)
            if (currentTime >= eveningStart) {
                return true
            }
        }
        
        return false
    }
    
    private fun parseTime(timeStr: String): Int {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return hour * 60 + minute
    }
    
    private fun blockVideoChannel() {
        Log.d(TAG, "Blocking video channel")
        
        // Perform back action to exit video channel
        performGlobalAction(GLOBAL_ACTION_BACK)
        
        // Increment block count
        serviceScope.launch {
            val today = dateFormat.format(Date())
            FocusWeChatApp.database.usageRecordDao().incrementBlockCount(today)
        }
        
        // Show block notification
        MonitorForegroundService.showBlockNotification(this)
        
        // End current session
        endSession()
    }
    
    private fun showReminderNotification() {
        MonitorForegroundService.showReminderNotification(this)
    }
}