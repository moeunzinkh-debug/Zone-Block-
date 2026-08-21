package com.example.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Space
import android.widget.Switch
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import com.example.data.model.EdgeProtectionConfig
import kotlin.math.hypot

/**
 * Floating bubble + quick settings panel owned by the accessibility service.
 *
 * - The bubble is draggable anywhere on screen.
 * - Tapping it (without dragging) opens a quick settings panel, so the user can
 *   configure everything (off/on, zone size big/small, edges, haptics) without
 *   opening the app.
 * - After a few seconds without being touched, the bubble slides halfway into
 *   the nearest screen edge. Touching it again pulls it back out.
 */
class FloatingControls(
    private val service: EdgeTouchBlockAccessibilityService,
    private val onConfigChange: (EdgeProtectionConfig) -> Unit
) {

    companion object {
        private const val PREFS_NAME = "game_space_edge_prefs"
        private const val KEY_BUBBLE_X = "bubble_x"
        private const val KEY_BUBBLE_Y = "bubble_y"
        private const val KEY_BUBBLE_RETRACTED = "bubble_retracted"
        private const val RETRACT_DELAY_MS = 3500L
    }

    private val wm: WindowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = service.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val colorText = Color.parseColor("#F1F5F9")
    private val colorMuted = Color.parseColor("#94A3B8")
    private val colorActive = Color.parseColor("#FF426B")
    private val colorInactive = Color.parseColor("#64748B")
    private val colorPanelBg = Color.parseColor("#F20E1522")
    private val colorPanelBorder = Color.parseColor("#33FFFFFF")

    private val bubbleSize = dp(52)

    private var bubble: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var bubbleIcon: ImageView? = null

    private var panel: View? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var masterSwitch: Switch? = null
    private var sizeLabel: TextView? = null
    private var sizeSeek: SeekBar? = null
    private var edgeTop: TextView? = null
    private var edgeBottom: TextView? = null
    private var edgeLeft: TextView? = null
    private var edgeRight: TextView? = null
    private var hapticSwitch: Switch? = null

    private var isRetracted = false
    private var retractAnimator: ValueAnimator? = null
    private var dragging = false
    private var dragMoved = false
    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0
    private var startY = 0
    private var touchSlop = 0

    private var lastConfig = EdgeProtectionConfig()

    private val retractRunnable = object : Runnable {
        override fun run() {
            if (!dragging && bubble != null && panel == null) {
                animateToRetracted()
            }
        }
    }

    // ---------------------------------------------------------------- config

    fun onConfigChanged(config: EdgeProtectionConfig) {
        lastConfig = config
        ensureBubble()
        if (panel != null) {
            syncPanelControls(config)
        }
        // When protection is OFF the bubble turns dim and tucks into the edge,
        // but stays tappable so it can be switched back ON without the app.
        if (!isActive(config) && !isRetracted) {
            snapRetracted()
            positionPanel()
        }
    }

    fun onScreenSizeChanged() {
        // Rotation / size change: keep everything inside the new bounds.
        clampBubbleToScreen()
        if (panel != null) {
            positionPanel()
        }
    }

    fun destroy() {
        mainHandler.removeCallbacks(retractRunnable)
        retractAnimator?.cancel()
        retractAnimator = null
        val b = bubble
        if (b != null && b.isAttachedToWindow) {
            try {
                wm.removeView(b)
            } catch (_: Exception) {
            }
        }
        bubble = null
        bubbleParams = null
        closePanel()
    }

    // ---------------------------------------------------------------- bubble

    private fun isActive(config: EdgeProtectionConfig): Boolean =
        config.isEnabled && config.zoneSizePercent > 0

    @SuppressLint("ClickableViewAccessibility")
    private fun ensureBubble() {
        if (bubble != null) {
            styleBubble()
            return
        }

        touchSlop = ViewConfiguration.get(service).scaledTouchSlop

        val icon = ImageView(service).apply {
            setImageResource(R.drawable.ic_float_shield)
            val size = dp(24)
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
        }
        bubbleIcon = icon

        val container = FrameLayout(service).apply {
            addView(icon)
        }

        val w = bubbleSize
        val params = WindowManager.LayoutParams(
            w,
            w,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            val pos = restoredPosition()
            x = pos.first
            y = pos.second
        }

        container.setOnTouchListener { _, event -> handleBubbleTouch(event) }

        try {
            wm.addView(container, params)
        } catch (_: Exception) {
            return
        }
        bubble = container
        bubbleParams = params
        styleBubble()

        if (prefs.getBoolean(KEY_BUBBLE_RETRACTED, false)) {
            snapRetracted()
        } else {
            scheduleRetract()
        }
    }

    private fun styleBubble() {
        val view = bubble ?: return
        val active = isActive(lastConfig)
        view.alpha = if (active) 1f else 0.72f
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(if (active) "#F2121826" else "#E60F172A"))
            setStroke(dp(2), if (active) colorActive else colorInactive)
        }
        bubbleIcon?.alpha = if (active) 1f else 0.55f
    }

    private fun restoredPosition(): Pair<Int, Int> {
        val dm = service.getRealScreenDimensions()
        val savedX = prefs.getInt(KEY_BUBBLE_X, -1)
        val savedY = prefs.getInt(KEY_BUBBLE_Y, -1)
        return if (savedX >= 0) {
            val x = savedX.coerceIn(-bubbleSize / 2, dm.widthPixels - bubbleSize / 2)
            val y = if (savedY >= 0) savedY.coerceIn(0, dm.heightPixels - bubbleSize) else dm.heightPixels / 3
            x to y
        } else {
            (dm.widthPixels - bubbleSize) to (dm.heightPixels / 3)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleBubbleTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mainHandler.removeCallbacks(retractRunnable)
                retractAnimator?.cancel()
                retractAnimator = null
                expandFully()
                val params = bubbleParams ?: return true
                startX = params.x
                startY = params.y
                downRawX = event.rawX
                downRawY = event.rawY
                dragging = true
                dragMoved = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return true
                val view = bubble ?: return true
                val params = bubbleParams ?: return true
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragMoved && hypot(dx, dy) > touchSlop) {
                    dragMoved = true
                }
                if (dragMoved) {
                    val dm = service.getRealScreenDimensions()
                    params.x = (startX + dx.toInt()).coerceIn(0, dm.widthPixels - bubbleSize)
                    params.y = (startY + dy.toInt()).coerceIn(0, dm.heightPixels - bubbleSize)
                    try {
                        wm.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    positionPanel()
                }
            }

            MotionEvent.ACTION_UP -> {
                dragging = false
                if (!dragMoved) {
                    togglePanel()
                }
                persistPosition()
                scheduleRetract()
            }

            MotionEvent.ACTION_CANCEL -> {
                dragging = false
                persistPosition()
                scheduleRetract()
            }
        }
        return true
    }

    /** Slides the bubble fully back into view on the nearest horizontal edge. */
    private fun expandFully() {
        if (!isRetracted) return
        val view = bubble ?: return
        val params = bubbleParams ?: return
        val dm = service.getRealScreenDimensions()
        params.x = if (params.x < dm.widthPixels / 2) 0 else dm.widthPixels - bubbleSize
        try {
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
        isRetracted = false
        persistPosition()
    }

    /** Slides the bubble halfway into the nearest screen edge. */
    private fun animateToRetracted() {
        val view = bubble ?: return
        val params = bubbleParams ?: return
        if (isRetracted || dragging || panel != null) return
        val dm = service.getRealScreenDimensions()
        val w = bubbleSize
        val targetX = if (params.x + w / 2 < dm.widthPixels / 2) -w / 2 else dm.widthPixels - w / 2
        isRetracted = true
        if (params.x == targetX) {
            persistPosition()
            return
        }
        val animator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 260
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val p = bubbleParams ?: return@addUpdateListener
                p.x = animation.animatedValue as Int
                try {
                    wm.updateViewLayout(view, p)
                } catch (_: Exception) {
                }
            }
        }
        retractAnimator = animator
        animator.start()
        persistPosition()
    }

    /** Instantly places the bubble in its retracted spot (used on restore). */
    private fun snapRetracted() {
        val view = bubble ?: return
        val params = bubbleParams ?: return
        val dm = service.getRealScreenDimensions()
        val w = bubbleSize
        params.x = if (params.x + w / 2 < dm.widthPixels / 2) -w / 2 else dm.widthPixels - w / 2
        try {
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
        isRetracted = true
        persistPosition()
    }

    private fun clampBubbleToScreen() {
        val view = bubble ?: return
        val params = bubbleParams ?: return
        val dm = service.getRealScreenDimensions()
        val w = bubbleSize
        val maxX = if (isRetracted) dm.widthPixels - w / 2 else dm.widthPixels - w
        val minX = if (isRetracted) -w / 2 else 0
        params.x = params.x.coerceIn(minX, maxX)
        params.y = params.y.coerceIn(0, dm.heightPixels - w)
        try {
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
        persistPosition()
    }

    private fun persistPosition() {
        val params = bubbleParams ?: return
        prefs.edit()
            .putInt(KEY_BUBBLE_X, params.x)
            .putInt(KEY_BUBBLE_Y, params.y)
            .putBoolean(KEY_BUBBLE_RETRACTED, isRetracted)
            .apply()
    }

    private fun scheduleRetract() {
        mainHandler.removeCallbacks(retractRunnable)
        mainHandler.postDelayed(retractRunnable, RETRACT_DELAY_MS)
    }

    // ---------------------------------------------------------------- panel

    private fun togglePanel() {
        if (panel != null) {
            closePanel()
        } else {
            openPanel()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun openPanel() {
        if (panel != null) return
        mainHandler.removeCallbacks(retractRunnable)
        retractAnimator?.cancel()
        retractAnimator = null
        expandFully()

        val root = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(colorPanelBg)
                setStroke(dp(1), colorPanelBorder)
            }
        }

        root.addView(buildHeader())
        root.addView(space(dp(12)))
        root.addView(buildSwitchRow("PROTECTION", true))
        root.addView(space(dp(12)))
        root.addView(buildZoneSizeBlock())
        root.addView(space(dp(12)))
        root.addView(buildEdgesRow())
        root.addView(space(dp(12)))
        root.addView(buildSwitchRow("HAPTIC FEEDBACK", false))
        root.addView(space(dp(14)))
        root.addView(buildOpenAppButton())

        val params = WindowManager.LayoutParams(
            dp(252),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        // Close the panel when the user taps anywhere outside of it.
        // Return false for touches inside the panel so the switch, seekbar,
        // chips and buttons keep working normally.
        root.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_OUTSIDE) {
                closePanel()
                true
            } else {
                false
            }
        }

        try {
            wm.addView(root, params)
        } catch (_: Exception) {
            return
        }
        panel = root
        panelParams = params
        positionPanel()
        syncPanelControls(lastConfig)
    }

    private fun closePanel() {
        val view = panel ?: return
        if (view.isAttachedToWindow) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {
            }
        }
        panel = null
        panelParams = null
        scheduleRetract()
    }

    private fun positionPanel() {
        val view = panel ?: return
        val params = panelParams ?: return
        val bubblePosition = bubbleParams ?: return
        val dm = service.getRealScreenDimensions()

        val widthSpec = View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.AT_MOST)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.AT_MOST)
        view.measure(widthSpec, heightSpec)
        val panelW = view.measuredWidth
        val panelH = view.measuredHeight

        val margin = dp(8)
        val bubbleCenterX = bubblePosition.x + bubbleSize / 2
        val bubbleCenterY = bubblePosition.y + bubbleSize / 2

        // Keep the panel next to the bubble (never covering it) so the bubble
        // always stays tappable to close the panel again.
        params.x = if (bubbleCenterX < dm.widthPixels / 2) {
            // Bubble on the left half -> panel opens to its right
            (bubblePosition.x + bubbleSize + margin)
                .coerceAtMost(dm.widthPixels - panelW - margin)
        } else {
            // Bubble on the right half -> panel opens to its left
            (bubblePosition.x - panelW - margin)
                .coerceAtLeast(margin)
        }
        params.y = (bubbleCenterY - panelH / 2).coerceIn(margin, (dm.heightPixels - panelH - margin).coerceAtLeast(margin))
        try {
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
    }

    private fun syncPanelControls(config: EdgeProtectionConfig) {
        masterSwitch?.let { switch ->
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = config.isEnabled
            switch.setOnCheckedChangeListener { _, checked ->
                onConfigChange(lastConfig.copy(isEnabled = checked))
            }
        }

        sizeSeek?.progress = config.zoneSizePercent
        sizeLabel?.text = "${config.zoneSizePercent}%"

        styleEdgeChip(edgeTop, config.topEdgeEnabled)
        styleEdgeChip(edgeBottom, config.bottomEdgeEnabled)
        styleEdgeChip(edgeLeft, config.leftEdgeEnabled)
        styleEdgeChip(edgeRight, config.rightEdgeEnabled)

        hapticSwitch?.let { switch ->
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = config.hapticFeedbackEnabled
            switch.setOnCheckedChangeListener { _, checked ->
                onConfigChange(lastConfig.copy(hapticFeedbackEnabled = checked))
            }
        }
    }

    // ------------------------------------------------------ panel sub-views

    private fun buildHeader(): View {
        val row = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(
            label("ZONE BLOCK", 13f, colorText),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        val close = TextView(service).apply {
            text = "\u2715"
            setTextColor(colorActive)
            textSize = 16f
            setPadding(dp(12), 0, 0, dp(2))
            setOnClickListener { closePanel() }
        }
        row.addView(close, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        return row
    }

    private fun buildSwitchRow(title: String, isMaster: Boolean): View {
        val row = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(
            label(title, 11f, colorText),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        val switch = Switch(service).apply {
            setTextColor(colorMuted)
            thumbTintList = ColorStateList.valueOf(Color.WHITE)
            trackTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(colorActive, colorInactive)
            )
        }
        if (isMaster) {
            masterSwitch = switch
        } else {
            hapticSwitch = switch
        }
        row.addView(switch, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        return row
    }

    private fun buildZoneSizeBlock(): View {
        val column = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
        }

        val row = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(
            label("ZONE SIZE (BIG / SMALL)", 10f, colorMuted),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        val percentText = TextView(service).apply {
            text = "${lastConfig.zoneSizePercent}%"
            setTextColor(colorActive)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        sizeLabel = percentText
        row.addView(percentText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        column.addView(row, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val seek = SeekBar(service).apply {
            max = 24
            progress = lastConfig.zoneSizePercent
            progressTintList = ColorStateList.valueOf(colorActive)
            thumbTintList = ColorStateList.valueOf(colorActive)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        sizeLabel?.text = "$progress%"
                        onConfigChange(lastConfig.copy(zoneSizePercent = progress))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
        sizeSeek = seek
        column.addView(seek, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        column.addView(label("0% = OFF", 9f, colorMuted))
        return column
    }

    private fun buildEdgesRow(): View {
        val row = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val topChip = edgeChip("TOP") { onConfigChange(lastConfig.copy(topEdgeEnabled = !lastConfig.topEdgeEnabled)) }
        edgeTop = topChip
        row.addView(topChip, chipParams())

        val bottomChip = edgeChip("BOTTOM") { onConfigChange(lastConfig.copy(bottomEdgeEnabled = !lastConfig.bottomEdgeEnabled)) }
        edgeBottom = bottomChip
        row.addView(bottomChip, chipParams())

        val leftChip = edgeChip("LEFT") { onConfigChange(lastConfig.copy(leftEdgeEnabled = !lastConfig.leftEdgeEnabled)) }
        edgeLeft = leftChip
        row.addView(leftChip, chipParams())

        val rightChip = edgeChip("RIGHT") { onConfigChange(lastConfig.copy(rightEdgeEnabled = !lastConfig.rightEdgeEnabled)) }
        edgeRight = rightChip
        row.addView(rightChip, chipParams(isLast = true))

        return row
    }

    private fun chipParams(isLast: Boolean = false): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            if (!isLast) {
                marginEnd = dp(6)
            }
        }
    }

    private fun edgeChip(text: String, onClick: () -> Unit): TextView {
        return TextView(service).apply {
            this.text = text
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
            setOnClickListener { onClick() }
        }
    }

    private fun styleEdgeChip(chip: TextView?, enabled: Boolean) {
        val view = chip ?: return
        view.setTextColor(if (enabled) colorActive else colorMuted)
        view.background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat()
            setColor(if (enabled) Color.argb(30, 255, 66, 107) else Color.argb(20, 255, 255, 255))
            setStroke(dp(1), if (enabled) Color.argb(110, 255, 66, 107) else colorPanelBorder)
        }
    }

    private fun buildOpenAppButton(): View {
        return TextView(service).apply {
            text = "OPEN APP"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(11), 0, dp(11))
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(colorActive)
            }
            setOnClickListener {
                closePanel()
                try {
                    val intent = Intent(service, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    service.startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
    }

    // ------------------------------------------------------------ utilities

    private fun label(text: String, sizeSp: Float, color: Int): TextView {
        return TextView(service).apply {
            this.text = text
            setTextColor(color)
            textSize = sizeSp
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun space(height: Int): View {
        val space = Space(service)
        space.layoutParams = LinearLayout.LayoutParams(1, height)
        return space
    }

    private fun overlayType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            service.resources.displayMetrics
        ).toInt()
    }
}
