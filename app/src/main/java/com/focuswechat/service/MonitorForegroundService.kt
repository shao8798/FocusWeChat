package com.focuswechat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focuswechat.MainActivity
import com.focuswechat.R

class MonitorForegroundService : Service() {
    
    companion object {
        private const val CHANNEL_ID = "focuswechat_monitor"
        private const val CHANNEL_NAME = "视频号监测"
        private const val NOTIFICATION_ID = 1
        private const val BLOCK_NOTIFICATION_ID = 2
        private const val REMINDER_NOTIFICATION_ID = 3
        
        fun showMonitoringNotification(context: Context, message: String) {
            val service = MonitorForegroundService()
            service.createNotificationChannel(context)
            
            val notification = service.buildNotification(context, message)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
        
        fun showBlockNotification(context: Context) {
            val service = MonitorForegroundService()
            service.createNotificationChannel(context)
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("⏰ 时间到啦！")
                .setContentText("今日视频号使用时间已达上限，休息一下吧")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .build()
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(BLOCK_NOTIFICATION_ID, notification)
        }
        
        fun showReminderNotification(context: Context) {
            val service = MonitorForegroundService()
            service.createNotificationChannel(context)
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("⏳ 即将到达限制")
                .setContentText("视频号使用时间快用完了，注意把握时间哦")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(REMINDER_NOTIFICATION_ID, notification)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(this, "视频号监测服务运行中")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "监测视频号使用时长"
                setShowBadge(false)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(context: Context, message: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("📱 视频号助手")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}