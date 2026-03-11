package com.focuswechat.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "focuswechat_prefs"
        
        // Keys
        private const val KEY_DAILY_LIMIT = "daily_limit_minutes"
        private const val KEY_SESSION_LIMIT = "session_limit_minutes"
        private const val KEY_WHITELIST_LUNCH = "whitelist_lunch"
        private const val KEY_WHITELIST_EVENING = "whitelist_evening"
        private const val KEY_LUNCH_START = "lunch_start"
        private const val KEY_LUNCH_END = "lunch_end"
        private const val KEY_EVENING_START = "evening_start"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }
    
    // Daily limit in minutes (default: 60)
    var dailyLimitMinutes: Int
        get() = prefs.getInt(KEY_DAILY_LIMIT, 60)
        set(value) = prefs.edit().putInt(KEY_DAILY_LIMIT, value).apply()
    
    // Session limit in minutes (default: 15)
    var sessionLimitMinutes: Int
        get() = prefs.getInt(KEY_SESSION_LIMIT, 15)
        set(value) = prefs.edit().putInt(KEY_SESSION_LIMIT, value).apply()
    
    // Whitelist settings
    var whitelistLunchEnabled: Boolean
        get() = prefs.getBoolean(KEY_WHITELIST_LUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_WHITELIST_LUNCH, value).apply()
    
    var whitelistEveningEnabled: Boolean
        get() = prefs.getBoolean(KEY_WHITELIST_EVENING, true)
        set(value) = prefs.edit().putBoolean(KEY_WHITELIST_EVENING, value).apply()
    
    // Time settings (format: HH:mm)
    var lunchStart: String
        get() = prefs.getString(KEY_LUNCH_START, "12:00") ?: "12:00"
        set(value) = prefs.edit().putString(KEY_LUNCH_START, value).apply()
    
    var lunchEnd: String
        get() = prefs.getString(KEY_LUNCH_END, "13:00") ?: "13:00"
        set(value) = prefs.edit().putString(KEY_LUNCH_END, value).apply()
    
    var eveningStart: String
        get() = prefs.getString(KEY_EVENING_START, "18:00") ?: "18:00"
        set(value) = prefs.edit().putString(KEY_EVENING_START, value).apply()
    
    // Feature toggles
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, value).apply()
    
    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()
    
    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()
}