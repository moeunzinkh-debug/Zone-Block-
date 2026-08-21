package com.example.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.data.db.AppDatabase
import com.example.data.db.BlockedEvent
import com.example.data.model.EdgeProtectionConfig
import com.example.data.repository.EdgeProtectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class EdgeTouchBlockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var repository: EdgeProtectionRepository? = null
    private var floatingControls: FloatingControls? = null

    // Overlay views (invisible barriers that only consume touches)
    private var leftOverlay: View? = null
    private var rightOverlay: View? = null
    private var topOverlay: View? = null
    private var bottomOverlay: View? = null

    private var currentConfig: EdgeProtectionConfig = EdgeProtectionConfig()

    companion object {
        @Volatile
        private var instance: EdgeTouchBlockAccessibilityService? = null

        fun isRunning(): Boolean = instance != null

        /**
         * Called by the app whenever the user changes a setting.
         * The app's ViewModel holds its own repository instance, so the service
         * must re-read SharedPreferences to pick up the latest saved config.
         */
        fun requestUpdate() {
            instance?.onExternalUpdateRequested()
        }
    }

    private fun onExternalUpdateRequested() {
        repository?.reloadFromPrefs()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val db = AppDatabase.getDatabase(applicationContext)
        repository = EdgeProtectionRepository(applicationContext, db.blockedEventDao())

        floatingControls = FloatingControls(
            service = this,
            onConfigChange = { newConfig ->
                repository?.updateConfig(newConfig)
            }
        )

        serviceScope.launch {
            repository?.config?.collectLatest { config ->
                currentConfig = config
                withContext(Dispatchers.Main) {
                    updateOverlays()
                    floatingControls?.onConfigChanged(config)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events monitoring if needed
    }

    override fun onInterrupt() {
        removeOverlays()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOverlays()
        floatingControls?.onScreenSizeChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        removeOverlays()
        floatingControls?.destroy()
        floatingControls = null
        serviceScope.cancel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateOverlays() {
        val wm = windowManager ?: return

        // If protection is disabled or zone size is 0%, remove overlays completely
        if (!currentConfig.isEnabled || currentConfig.zoneSizePercent <= 0) {
            removeOverlays()
            return
        }

        val displayMetrics = getRealScreenDimensions()
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val percent = currentConfig.zoneSizePercent
        val edgeThicknessPx = ((screenWidth * percent) / 100f).roundToInt().coerceAtLeast(1)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }

        val overlayFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        // Update LEFT Edge Overlay
        if (currentConfig.leftEdgeEnabled) {
            leftOverlay = ensureOverlayView(
                existingView = leftOverlay,
                edgeName = "LEFT",
                gravity = Gravity.START or Gravity.TOP,
                width = edgeThicknessPx,
                height = WindowManager.LayoutParams.MATCH_PARENT,
                layoutType = layoutType,
                flags = overlayFlags,
                wm = wm
            )
        } else {
            removeSingleOverlay(leftOverlay, wm)
            leftOverlay = null
        }

        // Update RIGHT Edge Overlay
        if (currentConfig.rightEdgeEnabled) {
            rightOverlay = ensureOverlayView(
                existingView = rightOverlay,
                edgeName = "RIGHT",
                gravity = Gravity.END or Gravity.TOP,
                width = edgeThicknessPx,
                height = WindowManager.LayoutParams.MATCH_PARENT,
                layoutType = layoutType,
                flags = overlayFlags,
                wm = wm
            )
        } else {
            removeSingleOverlay(rightOverlay, wm)
            rightOverlay = null
        }

        // Update TOP Edge Overlay
        if (currentConfig.topEdgeEnabled) {
            topOverlay = ensureOverlayView(
                existingView = topOverlay,
                edgeName = "TOP",
                gravity = Gravity.TOP or Gravity.START,
                width = WindowManager.LayoutParams.MATCH_PARENT,
                height = edgeThicknessPx,
                layoutType = layoutType,
                flags = overlayFlags,
                wm = wm
            )
        } else {
            removeSingleOverlay(topOverlay, wm)
            topOverlay = null
        }

        // Update BOTTOM Edge Overlay
        if (currentConfig.bottomEdgeEnabled) {
            bottomOverlay = ensureOverlayView(
                existingView = bottomOverlay,
                edgeName = "BOTTOM",
                gravity = Gravity.BOTTOM or Gravity.START,
                width = WindowManager.LayoutParams.MATCH_PARENT,
                height = edgeThicknessPx,
                layoutType = layoutType,
                flags = overlayFlags,
                wm = wm
            )
        } else {
            removeSingleOverlay(bottomOverlay, wm)
            bottomOverlay = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ensureOverlayView(
        existingView: View?,
        edgeName: String,
        gravity: Int,
        width: Int,
        height: Int,
        layoutType: Int,
        flags: Int,
        wm: WindowManager
    ): View {
        val params = WindowManager.LayoutParams(
            width,
            height,
            layoutType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = 0
            y = 0
        }

        if (existingView != null && existingView.isAttachedToWindow) {
            try {
                wm.updateViewLayout(existingView, params)
                return existingView
            } catch (_: Exception) {
                removeSingleOverlay(existingView, wm)
            }
        }

        // Create a fully transparent barrier view.
        // Outside the app the blocking zone is invisible (the screen looks
        // completely normal) but it still consumes every touch so games never
        // receive accidental edge/palm input. Colors are only shown inside the
        // in-app previews.
        val view = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)

            // Consumes touch event 100% (returning true blocks it from underlying games/apps)
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        handleTouchBlocked(edgeName)
                    }
                }
                // Return true to consume and block the touch completely!
                true
            }
        }

        try {
            wm.addView(view, params)
        } catch (_: Exception) {
            // In case of window manager state exception
        }

        return view
    }

    private fun handleTouchBlocked(edgeName: String) {
        // Trigger Haptic Feedback (if enabled)
        triggerHaptic()

        // Record Blocked Event to Room DB
        serviceScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.blockedEventDao().insertEvent(
                    BlockedEvent(
                        edgeZone = edgeName,
                        profileName = currentConfig.profileName,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
                // DB write failure safety
            }
        }
    }

    private fun triggerHaptic() {
        if (!currentConfig.hapticFeedbackEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45L)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration error
        }
    }

    internal fun getRealScreenDimensions(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        val wm = windowManager ?: return resources.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowBounds = wm.currentWindowMetrics.bounds
            displayMetrics.widthPixels = windowBounds.width()
            displayMetrics.heightPixels = windowBounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(displayMetrics)
        }
        return displayMetrics
    }

    private fun removeOverlays() {
        val wm = windowManager ?: return
        removeSingleOverlay(leftOverlay, wm)
        leftOverlay = null

        removeSingleOverlay(rightOverlay, wm)
        rightOverlay = null

        removeSingleOverlay(topOverlay, wm)
        topOverlay = null

        removeSingleOverlay(bottomOverlay, wm)
        bottomOverlay = null
    }

    private fun removeSingleOverlay(view: View?, wm: WindowManager) {
        if (view != null && view.isAttachedToWindow) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {
                // Already removed or detached
            }
        }
    }
}
