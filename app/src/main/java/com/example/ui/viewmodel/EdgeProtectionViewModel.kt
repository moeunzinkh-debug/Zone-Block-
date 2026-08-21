package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.EdgeProtectionConfig
import com.example.data.model.GameProfile
import com.example.data.repository.EdgeProtectionRepository
import com.example.service.EdgeTouchBlockAccessibilityService
import com.example.util.AccessibilityHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TouchFeedback(
    val id: Long = System.currentTimeMillis(),
    val isBlocked: Boolean,
    val zone: String? = null,
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val message: String
)

class EdgeProtectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EdgeProtectionRepository

    val config: StateFlow<EdgeProtectionConfig>

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _showAccessibilityDialog = MutableStateFlow(false)
    val showAccessibilityDialog: StateFlow<Boolean> = _showAccessibilityDialog.asStateFlow()

    private val _lastTouchFeedback = MutableStateFlow<TouchFeedback?>(null)
    val lastTouchFeedback: StateFlow<TouchFeedback?> = _lastTouchFeedback.asStateFlow()

    private val _validTouchCount = MutableStateFlow(0)
    val validTouchCount: StateFlow<Int> = _validTouchCount.asStateFlow()

    private val _sessionBlockedCount = MutableStateFlow(0)
    val sessionBlockedCount: StateFlow<Int> = _sessionBlockedCount.asStateFlow()

    private val _activePulseZone = MutableStateFlow<String?>(null)
    val activePulseZone: StateFlow<String?> = _activePulseZone.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isFullScreenSandboxOpen = MutableStateFlow(false)
    val isFullScreenSandboxOpen: StateFlow<Boolean> = _isFullScreenSandboxOpen.asStateFlow()

    private var toastJob: Job? = null
    private var pulseJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EdgeProtectionRepository(application, database.blockedEventDao())
        config = repository.config
        checkAccessibilityStatus()
    }

    fun checkAccessibilityStatus() {
        // Re-read config from prefs so changes made from the floating quick
        // panel (which runs in the accessibility service) are reflected here.
        repository.reloadFromPrefs()
        val enabled = AccessibilityHelper.isAccessibilityServiceEnabled(getApplication())
        _isAccessibilityEnabled.value = enabled
        if (enabled) {
            EdgeTouchBlockAccessibilityService.requestUpdate()
        }
    }

    fun openAccessibilitySettings() {
        AccessibilityHelper.openAccessibilitySettings(getApplication())
        _showAccessibilityDialog.value = false
    }

    fun dismissAccessibilityDialog() {
        _showAccessibilityDialog.value = false
    }

    val recentBlockedEvents = repository.recentEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalDatabaseBlockedCount = repository.totalBlockedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun toggleProtection() {
        val current = config.value
        val newState = !current.isEnabled
        
        // If enabling protection and Accessibility Service is not active, prompt user to enable it
        if (newState) {
            checkAccessibilityStatus()
            if (!_isAccessibilityEnabled.value) {
                _showAccessibilityDialog.value = true
            }
        }

        val updated = current.copy(isEnabled = newState)
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()

        if (newState) {
            if (_isAccessibilityEnabled.value) {
                showToast("Real screen edge protection activated!")
            } else {
                showToast("Protection enabled (Accessibility required for system overlay)")
            }
        } else {
            showToast("Edge protection disabled")
        }
    }

    fun setZoneSizePercent(percent: Int) {
        val current = config.value
        val clamped = percent.coerceIn(0, 24)
        val profile = when (clamped) {
            GameProfile.BALANCED.percent -> GameProfile.BALANCED.title
            GameProfile.FPS_SHOOTER.percent -> GameProfile.FPS_SHOOTER.title
            GameProfile.MOBA_ARENA.percent -> GameProfile.MOBA_ARENA.title
            GameProfile.RHYTHM_TOUCH.percent -> GameProfile.RHYTHM_TOUCH.title
            else -> "Custom"
        }
        val updated = current.copy(zoneSizePercent = clamped, profileName = profile)
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
    }

    fun selectProfile(profile: GameProfile) {
        checkAccessibilityStatus()
        if (!_isAccessibilityEnabled.value) {
            _showAccessibilityDialog.value = true
        }

        val current = config.value
        val updated = current.copy(
            isEnabled = true,
            zoneSizePercent = profile.percent,
            profileName = profile.title
        )
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
        showToast("Profile active: ${profile.title} (${profile.percent}%)")
    }

    fun toggleEdge(edge: String) {
        val current = config.value
        val updated = when (edge.uppercase()) {
            "TOP" -> current.copy(topEdgeEnabled = !current.topEdgeEnabled)
            "BOTTOM" -> current.copy(bottomEdgeEnabled = !current.bottomEdgeEnabled)
            "LEFT" -> current.copy(leftEdgeEnabled = !current.leftEdgeEnabled)
            "RIGHT" -> current.copy(rightEdgeEnabled = !current.rightEdgeEnabled)
            else -> current
        }
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
    }

    fun resetToDefault() {
        val updated = config.value.copy(
            isEnabled = true,
            zoneSizePercent = 13,
            topEdgeEnabled = true,
            bottomEdgeEnabled = true,
            leftEdgeEnabled = true,
            rightEdgeEnabled = true,
            profileName = GameProfile.BALANCED.title
        )
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
        showToast("Zone size reset to 13%")
    }

    fun emergencyStop() {
        val updated = config.value.copy(isEnabled = false)
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
        showToast("Emergency stop: all edge overlays disabled")
    }

    fun saveSettings() {
        repository.updateConfig(config.value)
        EdgeTouchBlockAccessibilityService.requestUpdate()
        showToast("Edge protection settings saved & applied")
    }

    fun toggleHaptics() {
        val current = config.value
        val updated = current.copy(hapticFeedbackEnabled = !current.hapticFeedbackEnabled)
        repository.updateConfig(updated)
        showToast(if (updated.hapticFeedbackEnabled) "Haptic feedback on" else "Haptic feedback off")
    }

    fun setOverlayOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.1f, 0.9f)
        val updated = config.value.copy(visualOverlayOpacity = clamped)
        repository.updateConfig(updated)
        EdgeTouchBlockAccessibilityService.requestUpdate()
    }

    fun openFullScreenSandbox(open: Boolean) {
        _isFullScreenSandboxOpen.value = open
    }

    fun handleScreenTouch(xRatio: Float, yRatio: Float, containerWidthPx: Float, containerHeightPx: Float) {
        val currentConfig = config.value
        val percent = currentConfig.zoneSizePercent
        val isProtected = currentConfig.isEnabled && percent > 0

        // Calculate edge threshold in ratios based on screen dimensions
        // Top and bottom use the same pixel thickness as left and right edges
        val edgePx = containerWidthPx * (percent / 100f)
        val horizontalEdgeRatio = percent / 100f
        val verticalEdgeRatio = if (containerHeightPx > 0) edgePx / containerHeightPx else horizontalEdgeRatio

        var blockedZone: String? = null

        if (isProtected) {
            if (currentConfig.topEdgeEnabled && yRatio <= verticalEdgeRatio) {
                blockedZone = "top"
            } else if (currentConfig.bottomEdgeEnabled && yRatio >= (1f - verticalEdgeRatio)) {
                blockedZone = "bottom"
            } else if (currentConfig.leftEdgeEnabled && xRatio <= horizontalEdgeRatio) {
                blockedZone = "left"
            } else if (currentConfig.rightEdgeEnabled && xRatio >= (1f - horizontalEdgeRatio)) {
                blockedZone = "right"
            }
        }

        if (blockedZone != null) {
            // Blocked accidental touch
            _sessionBlockedCount.value += 1
            _activePulseZone.value = blockedZone
            
            triggerHaptic(isBlocked = true)

            viewModelScope.launch {
                repository.recordBlockedEvent(
                    edgeZone = blockedZone.uppercase(),
                    profileName = currentConfig.profileName
                )
            }

            _lastTouchFeedback.value = TouchFeedback(
                isBlocked = true,
                zone = blockedZone,
                xRatio = xRatio,
                yRatio = yRatio,
                message = "Touch blocked in $blockedZone edge zone"
            )

            pulseJob?.cancel()
            pulseJob = viewModelScope.launch {
                delay(350)
                _activePulseZone.value = null
            }

            showToast("Touch blocked in $blockedZone edge zone")
        } else {
            // Valid game touch registered
            _validTouchCount.value += 1
            triggerHaptic(isBlocked = false)

            _lastTouchFeedback.value = TouchFeedback(
                isBlocked = false,
                xRatio = xRatio,
                yRatio = yRatio,
                message = "Valid game touch registered!"
            )
        }
    }

    private fun triggerHaptic(isBlocked: Boolean) {
        if (!config.value.hapticFeedbackEnabled) return
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (isBlocked) {
                        VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                    } else {
                        VibrationEffect.createOneShot(15, 80)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isBlocked) 45L else 15L)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration permission not available
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(2400)
            _toastMessage.value = null
        }
    }

    fun clearStats() {
        _validTouchCount.value = 0
        _sessionBlockedCount.value = 0
        viewModelScope.launch {
            repository.clearHistory()
        }
        showToast("Touch statistics cleared")
    }
}
