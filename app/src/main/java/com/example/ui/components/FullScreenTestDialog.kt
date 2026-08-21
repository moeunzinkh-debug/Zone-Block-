package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EdgeProtectionConfig
import com.example.ui.theme.BgDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedDim
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PhoneScreenBg1
import com.example.ui.theme.PhoneScreenBg2
import com.example.ui.theme.PhoneScreenBg3
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TouchFeedback
import kotlinx.coroutines.delay
import kotlin.random.Random

data class TargetOrb(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val isBonus: Boolean = false
)

@Composable
fun FullScreenTestDialog(
    isOpen: Boolean,
    config: EdgeProtectionConfig,
    activePulseZone: String?,
    lastTouchFeedback: TouchFeedback?,
    onTouchScreen: (xRatio: Float, yRatio: Float, widthPx: Float, heightPx: Float) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var arenaSize by remember { mutableStateOf(IntSize.Zero) }
        var score by remember { mutableStateOf(0) }
        var targets by remember {
            mutableStateOf(
                listOf(
                    TargetOrb(1, 0.5f, 0.4f),
                    TargetOrb(2, 0.3f, 0.6f),
                    TargetOrb(3, 0.7f, 0.65f)
                )
            )
        }

        // Spawn targets periodically
        LaunchedEffect(Unit) {
            while (true) {
                delay(2200)
                targets = listOf(
                    TargetOrb(Random.nextInt(), Random.nextFloat().coerceIn(0.18f, 0.82f), Random.nextFloat().coerceIn(0.2f, 0.8f)),
                    TargetOrb(Random.nextInt(), Random.nextFloat().coerceIn(0.18f, 0.82f), Random.nextFloat().coerceIn(0.2f, 0.8f)),
                    TargetOrb(Random.nextInt(), Random.nextFloat().coerceIn(0.05f, 0.95f), Random.nextFloat().coerceIn(0.05f, 0.95f), isBonus = true)
                )
            }
        }

        val percent = config.zoneSizePercent
        val isProtectionActive = config.isEnabled && percent > 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PhoneScreenBg1, BgDark, PhoneScreenBg3)
                    )
                )
                .onSizeChanged { arenaSize = it }
                .pointerInput(isProtectionActive, percent, config) {
                    detectTapGestures { offset ->
                        if (arenaSize.width > 0 && arenaSize.height > 0) {
                            val xRatio = offset.x / arenaSize.width
                            val yRatio = offset.y / arenaSize.height
                            onTouchScreen(
                                xRatio,
                                yRatio,
                                arenaSize.width.toFloat(),
                                arenaSize.height.toFloat()
                            )

                            // Check target hits if not blocked
                            targets.forEach { orb ->
                                val targetX = orb.xRatio * arenaSize.width
                                val targetY = orb.yRatio * arenaSize.height
                                val distSq = (offset.x - targetX) * (offset.x - targetX) + (offset.y - targetY) * (offset.y - targetY)
                                if (distSq < 3600) { // Hit within ~60px
                                    score += if (orb.isBonus) 50 else 20
                                }
                            }
                        }
                    }
                }
        ) {
            // Target orbs inside arena
            if (arenaSize.width > 0 && arenaSize.height > 0) {
                targets.forEach { orb ->
                    val orbX = (orb.xRatio * arenaSize.width).toInt() - 25
                    val orbY = (orb.yRatio * arenaSize.height).toInt() - 25
                    val orbColor = if (orb.isBonus) NeonRed else CyberCyan

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(orbX, orbY) }
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(orbColor.copy(alpha = 0.25f))
                            .border(2.dp, orbColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (orb.isBonus) "★" else "◈",
                            color = orbColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Header HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(BgDark.copy(alpha = 0.85f))
                    .border(1.dp, PanelBorder)
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonRed)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Arena",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "GAME TEST ARENA",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isProtectionActive) "Edge Protection: $percent% ACTIVE" else "Edge Protection: OFF",
                            color = if (isProtectionActive) MatrixGreen else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "SCORE: $score",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Arena",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Interactive Protected Edge Zones
            if (isProtectionActive && arenaSize.width > 0) {
                val edgePx = arenaSize.width * (percent / 100f)
                val edgeHeightPercent = if (arenaSize.height > 0) (edgePx / arenaSize.height) else (percent / 100f)

                val zoneColor = NeonRed.copy(alpha = 0.35f)
                val borderColor = NeonRedLight.copy(alpha = 0.8f)

                // Top Zone
                if (config.topEdgeEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(edgeHeightPercent)
                            .align(Alignment.TopCenter)
                            .background(if (activePulseZone == "top") NeonRed.copy(alpha = 0.75f) else zoneColor)
                            .border(1.dp, borderColor)
                    ) {
                        Text(
                            text = "TOP EDGE BLOCKED ($percent%)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Bottom Zone
                if (config.bottomEdgeEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(edgeHeightPercent)
                            .align(Alignment.BottomCenter)
                            .background(if (activePulseZone == "bottom") NeonRed.copy(alpha = 0.75f) else zoneColor)
                            .border(1.dp, borderColor)
                    ) {
                        Text(
                            text = "BOTTOM EDGE BLOCKED ($percent%)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Left Zone
                if (config.leftEdgeEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxSize()
                            .align(Alignment.CenterStart)
                            .background(if (activePulseZone == "left") NeonRed.copy(alpha = 0.75f) else zoneColor)
                            .border(1.dp, borderColor)
                    ) {
                        Text(
                            text = "LEFT BLOCKED",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Right Zone
                if (config.rightEdgeEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxSize()
                            .align(Alignment.CenterEnd)
                            .background(if (activePulseZone == "right") NeonRed.copy(alpha = 0.75f) else zoneColor)
                            .border(1.dp, borderColor)
                    ) {
                        Text(
                            text = "RIGHT BLOCKED",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Touch ripple feedback
            if (lastTouchFeedback != null && arenaSize.width > 0 && arenaSize.height > 0) {
                TouchRippleIndicator(
                    feedback = lastTouchFeedback,
                    containerWidth = arenaSize.width.toFloat(),
                    containerHeight = arenaSize.height.toFloat()
                )
            }

            // Bottom info tip
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(BgDark.copy(alpha = 0.9f))
                    .border(1.dp, PanelBorder, RoundedCornerShape(99.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tap edge zones to test palm/swipe rejection · Tap centers to hit targets",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
