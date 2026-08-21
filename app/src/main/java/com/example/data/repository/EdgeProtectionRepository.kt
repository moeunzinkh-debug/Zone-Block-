package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.BlockedEvent
import com.example.data.db.BlockedEventDao
import com.example.data.model.EdgeProtectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EdgeProtectionRepository(
    private val context: Context,
    private val blockedEventDao: BlockedEventDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_space_edge_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<EdgeProtectionConfig> = _config.asStateFlow()

    val recentEvents: Flow<List<BlockedEvent>> = blockedEventDao.getRecentBlockedEvents()
    val totalBlockedCount: Flow<Int> = blockedEventDao.getTotalBlockedCount()

    private fun loadConfig(): EdgeProtectionConfig {
        return EdgeProtectionConfig(
            isEnabled = prefs.getBoolean("is_enabled", true),
            zoneSizePercent = prefs.getInt("zone_size_percent", 13),
            topEdgeEnabled = prefs.getBoolean("top_edge_enabled", true),
            bottomEdgeEnabled = prefs.getBoolean("bottom_edge_enabled", true),
            leftEdgeEnabled = prefs.getBoolean("left_edge_enabled", true),
            rightEdgeEnabled = prefs.getBoolean("right_edge_enabled", true),
            hapticFeedbackEnabled = prefs.getBoolean("haptic_enabled", true),
            showTouchRipple = prefs.getBoolean("show_touch_ripple", true),
            visualOverlayOpacity = prefs.getFloat("visual_opacity", 0.42f),
            profileName = prefs.getString("profile_name", "Balanced") ?: "Balanced"
        )
    }

    fun updateConfig(newConfig: EdgeProtectionConfig) {
        _config.value = newConfig
        prefs.edit().apply {
            putBoolean("is_enabled", newConfig.isEnabled)
            putInt("zone_size_percent", newConfig.zoneSizePercent)
            putBoolean("top_edge_enabled", newConfig.topEdgeEnabled)
            putBoolean("bottom_edge_enabled", newConfig.bottomEdgeEnabled)
            putBoolean("left_edge_enabled", newConfig.leftEdgeEnabled)
            putBoolean("right_edge_enabled", newConfig.rightEdgeEnabled)
            putBoolean("haptic_enabled", newConfig.hapticFeedbackEnabled)
            putBoolean("show_touch_ripple", newConfig.showTouchRipple)
            putFloat("visual_opacity", newConfig.visualOverlayOpacity)
            putString("profile_name", newConfig.profileName)
            apply()
        }
    }

    suspend fun recordBlockedEvent(edgeZone: String, profileName: String) {
        val event = BlockedEvent(
            edgeZone = edgeZone,
            profileName = profileName
        )
        blockedEventDao.insertEvent(event)
    }

    suspend fun clearHistory() {
        blockedEventDao.clearAllEvents()
    }
}
