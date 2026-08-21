package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EdgeProtectionConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PhoneCameraPill
import com.example.ui.theme.PhoneFrameBorder
import com.example.ui.theme.PhoneScreenBg1
import com.example.ui.theme.PhoneScreenBg2
import com.example.ui.theme.PhoneScreenBg3
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TouchFeedback
import kotlin.math.roundToInt

@Composable
fun PhoneSimulator(
    config: EdgeProtectionConfig,
    activePulseZone: String?,
    lastTouchFeedback: TouchFeedback?,
    onTouchScreen: (xRatio: Float, yRatio: Float, widthPx: Float, heightPx: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val percent = config.zoneSizePercent
    val isProtectionActive = config.isEnabled && percent > 0

    // Ambient glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "game_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(PanelDark)
            .border(1.dp, PanelBorder, RoundedCornerShape(32.dp))
            .padding(22.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LIVE VIEW",
                    color = NeonRedLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Protected Screen",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status chip with active glowing indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, PanelBorder, RoundedCornerShape(99.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("preview_chip_indicator")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .shadow(
                                elevation = if (isProtectionActive) 6.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = MatrixGreen
                            )
                            .background(
                                if (isProtectionActive) MatrixGreen else TextMuted,
                                CircleShape
                            )
                    )
                    Text(
                        text = if (isProtectionActive) "$percent% ACTIVE" else "DISABLED",
                        color = if (isProtectionActive) TextPrimary else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Phone Simulator Container with side floating measurement badges (Geometric Balance)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF070914).copy(alpha = 0.6f))
                .padding(vertical = 24.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Realistic Phone Mockup Box
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(350.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF050711))
                    .border(6.dp, PhoneFrameBorder, RoundedCornerShape(32.dp))
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.7f))
                    .padding(6.dp)
                    .testTag("phone_screen_preview")
            ) {
                var screenSize by remember { mutableStateOf(IntSize.Zero) }

                // Inner Phone Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PhoneScreenBg1, PhoneScreenBg2, PhoneScreenBg3)
                            )
                        )
                        .onSizeChanged { screenSize = it }
                        .pointerInput(isProtectionActive, percent, config) {
                            detectTapGestures { offset ->
                                if (screenSize.width > 0 && screenSize.height > 0) {
                                    val xRatio = offset.x / screenSize.width
                                    val yRatio = offset.y / screenSize.height
                                    onTouchScreen(
                                        xRatio,
                                        yRatio,
                                        screenSize.width.toFloat(),
                                        screenSize.height.toFloat()
                                    )
                                }
                            }
                        }
                ) {
                    // Radial glow background in screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(glowScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    radius = 280f
                                )
                            )
                    )

                    // LIVE VIEW text overlay
                    Text(
                        text = "LIVE VIEW",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.4.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 32.dp)
                    )

                    // Center Geometric Diamond
                    Text(
                        text = "◈",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Protection Zones matching Geometric Balance design (border-b / border-t / border-r / border-l)
                    if (isProtectionActive && screenSize.width > 0) {
                        val edgePx = screenSize.width * (percent / 100f)
                        val edgeHeightPercent = if (screenSize.height > 0) (edgePx / screenSize.height) else (percent / 100f)

                        val zoneBgColor = NeonRed.copy(alpha = 0.3f)
                        val zoneBorderColor = NeonRed

                        // Top Zone
                        if (config.topEdgeEnabled) {
                            val isPulsing = activePulseZone == "top"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(edgeHeightPercent)
                                    .align(Alignment.TopCenter)
                                    .background(if (isPulsing) NeonRed.copy(alpha = 0.75f) else zoneBgColor)
                                    .border(1.dp, zoneBorderColor)
                            )
                        }

                        // Bottom Zone
                        if (config.bottomEdgeEnabled) {
                            val isPulsing = activePulseZone == "bottom"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(edgeHeightPercent)
                                    .align(Alignment.BottomCenter)
                                    .background(if (isPulsing) NeonRed.copy(alpha = 0.75f) else zoneBgColor)
                                    .border(1.dp, zoneBorderColor)
                            )
                        }

                        // Left Zone
                        if (config.leftEdgeEnabled) {
                            val isPulsing = activePulseZone == "left"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percent / 100f)
                                    .fillMaxSize()
                                    .align(Alignment.CenterStart)
                                    .background(if (isPulsing) NeonRed.copy(alpha = 0.75f) else zoneBgColor)
                                    .border(1.dp, zoneBorderColor)
                            )
                        }

                        // Right Zone
                        if (config.rightEdgeEnabled) {
                            val isPulsing = activePulseZone == "right"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percent / 100f)
                                    .fillMaxSize()
                                    .align(Alignment.CenterEnd)
                                    .background(if (isPulsing) NeonRed.copy(alpha = 0.75f) else zoneBgColor)
                                    .border(1.dp, zoneBorderColor)
                            )
                        }
                    }

                    // Interactive touch ripple indicator
                    if (lastTouchFeedback != null && screenSize.width > 0 && screenSize.height > 0) {
                        TouchRippleIndicator(
                            feedback = lastTouchFeedback,
                            containerWidth = screenSize.width.toFloat(),
                            containerHeight = screenSize.height.toFloat()
                        )
                    }
                }

                // Phone Top Camera Pill Notch
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 4.dp)
                        .width(64.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(PhoneCameraPill)
                )
            }

            // Floating Geometric Measurement Cards on Right
            val horizPx = if (isProtectionActive) (percent * 1.8f).roundToInt() else 0
            val vertPx = if (isProtectionActive) (percent * 1.8f).roundToInt() else 0

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-4).dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vertical Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PanelDark)
                        .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "VERTICAL",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${vertPx}px",
                            color = NeonRedLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Horizontal Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PanelDark)
                        .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "HORIZONTAL",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${horizPx}px",
                            color = NeonRedLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preview Status Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isProtectionActive) MatrixGreen else TextMuted,
                            CircleShape
                        )
                        .border(
                            2.dp,
                            if (isProtectionActive) MatrixGreenDim else Color.Transparent,
                            CircleShape
                        )
                )
                Text(
                    text = if (isProtectionActive) "Active Protection" else "Protection Disabled",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (isProtectionActive) "Blocking $percent% of screen edges" else "No edge area blocked",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun TouchRippleIndicator(
    feedback: TouchFeedback,
    containerWidth: Float,
    containerHeight: Float
) {
    val animProgress = remember(feedback.id) { Animatable(0f) }

    LaunchedEffect(feedback.id) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    val touchX = feedback.xRatio * containerWidth
    val touchY = feedback.yRatio * containerHeight
    val color = if (feedback.isBlocked) NeonRed else CyberCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val radius = 10f + animProgress.value * 35f
                val alpha = (1f - animProgress.value).coerceIn(0f, 1f)

                // Outer wave
                drawCircle(
                    color = color.copy(alpha = alpha * 0.7f),
                    radius = radius,
                    center = Offset(touchX, touchY)
                )
                // Center dot
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = 5f,
                    center = Offset(touchX, touchY)
                )
            }
    )
}

