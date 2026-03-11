package com.focuswechat.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey
    val date: String,  // Format: yyyy-MM-dd
    
    @ColumnInfo(name = "total_seconds")
    val totalSeconds: Int = 0,
    
    @ColumnInfo(name = "session_count")
    val sessionCount: Int = 0,
    
    @ColumnInfo(name = "last_session_start")
    val lastSessionStart: Long? = null,
    
    @ColumnInfo(name = "is_limited")
    val isLimited: Boolean = false,
    
    @ColumnInfo(name = "block_count")
    val blockCount: Int = 0
)

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: String,  // Format: yyyy-MM-dd
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Timestamp
    
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,  // Null if still active
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int = 0,
    
    @ColumnInfo(name = "was_blocked")
    val wasBlocked: Boolean = false
)

@Dao
interface UsageRecordDao {
    @Query("SELECT * FROM usage_records WHERE date = :date")
    suspend fun getRecord(date: String): UsageRecord?
    
    @Query("SELECT * FROM usage_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetween(startDate: String, endDate: String): Flow<List<UsageRecord>>
    
    @Query("SELECT * FROM usage_records ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<UsageRecord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: UsageRecord)
    
    @Query("UPDATE usage_records SET total_seconds = totalSeconds + :seconds WHERE date = :date")
    suspend fun addTime(date: String, seconds: Int)
    
    @Query("UPDATE usage_records SET block_count = blockCount + 1 WHERE date = :date")
    suspend fun incrementBlockCount(date: String)
}

@Dao
interface SessionLogDao {
    @Insert
    suspend fun insert(log: SessionLog): Long
    
    @Query("UPDATE session_logs SET end_time = :endTime, duration_seconds = :duration WHERE id = :id")
    suspend fun endSession(id: Long, endTime: Long, duration: Int)
    
    @Query("SELECT * FROM session_logs WHERE date = :date ORDER BY start_time DESC")
    fun getSessionsForDate(date: String): Flow<List<SessionLog>>
}

@Database(entities = [UsageRecord::class, SessionLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun sessionLogDao(): SessionLogDao
}