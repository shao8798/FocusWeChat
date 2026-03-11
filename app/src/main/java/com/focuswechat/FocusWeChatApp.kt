package com.focuswechat

import android.app.Application
import androidx.room.Room
import com.focuswechat.data.AppDatabase
import com.focuswechat.util.PreferenceManager

class FocusWeChatApp : Application() {
    
    companion object {
        lateinit var database: AppDatabase
            private set
        lateinit var prefs: PreferenceManager
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "focuswechat_db"
        ).build()
        
        // Initialize preferences
        prefs = PreferenceManager(applicationContext)
    }
}