package com.example.data.model

data class EdgeProtectionConfig(
    val isEnabled: Boolean = true,
    val zoneSizePercent: Int = 13,
    val topEdgeEnabled: Boolean = true,
    val bottomEdgeEnabled: Boolean = true,
    val leftEdgeEnabled: Boolean = true,
    val rightEdgeEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val showTouchRipple: Boolean = true,
    val visualOverlayOpacity: Float = 0.42f,
    val profileName: String = "Balanced"
)

enum class GameProfile(
    val title: String,
    val subtitle: String,
    val percent: Int,
    val iconName: String
) {
    BALANCED("Balanced", "Default all-round protection", 13, "◈"),
    FPS_SHOOTER("FPS Pro", "Heavy palm rejection for shooters", 18, "⌖"),
    MOBA_ARENA("MOBA Arena", "Slim edges for skill pad & minimap", 8, "⚔"),
    RHYTHM_TOUCH("Rhythm Tap", "Ultra-narrow edge for lane taps", 4, "♪"),
    CUSTOM("Custom", "Fine-tuned zone settings", 13, "⚙")
}
