package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_events")
data class BlockedEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val edgeZone: String, // "TOP", "BOTTOM", "LEFT", "RIGHT"
    val timestamp: Long = System.currentTimeMillis(),
    val profileName: String,
    val blockedDurationMs: Long = 0
)
