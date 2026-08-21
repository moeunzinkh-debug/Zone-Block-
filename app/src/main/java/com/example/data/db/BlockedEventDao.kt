package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedEventDao {
    @Query("SELECT * FROM blocked_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentBlockedEvents(): Flow<List<BlockedEvent>>

    @Query("SELECT COUNT(*) FROM blocked_events")
    fun getTotalBlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: BlockedEvent)

    @Query("DELETE FROM blocked_events")
    suspend fun clearAllEvents()
}
