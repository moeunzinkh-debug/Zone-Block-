package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EdgeProtectionConfig
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedLight

@Composable
fun RealScreenEdgeOverlay(
    config: EdgeProtectionConfig,
    activePulseZone: String?,
    onTouchEdge: (xRatio: Float, yRatio: Float, widthPx: Float, heightPx: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!config.isEnabled || config.zoneSizePercent <= 0) return

    val percent = config.zoneSizePercent

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("real_screen_edge_overlay")
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()
        val edgeWidthDp = (maxWidth * (percent / 100f))

        // Base opacity from config
        val baseAlpha = (config.visualOverlayOpacity * 0.45f).coerceIn(0.04f, 0.5f)

        // LEFT EDGE BARRIER
        if (config.leftEdgeEnabled) {
            val isPulsing = activePulseZone.equals("left", ignoreCase = true)
            val leftBg = if (isPulsing) {
                Brush.horizontalGradient(
                    listOf(NeonRed.copy(alpha = 0.75f), NeonRed.copy(alpha = 0.2f), Color.Transparent)
                )
            } else {
                Brush.horizontalGradient(
                    listOf(NeonRed.copy(alpha = baseAlpha), Color.Transparent)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(edgeWidthDp)
                    .align(Alignment.CenterStart)
                    .background(leftBg)
                    .pointerInput(totalWidth, totalHeight, percent) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            down.consume()
                            onTouchEdge(down.position.x / totalWidth, down.position.y / totalHeight, totalWidth, totalHeight)

                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                if (event.changes.all { !it.pressed }) break
                            }
                        }
                    }
            )
        }

        // RIGHT EDGE BARRIER
        if (config.rightEdgeEnabled) {
            val isPulsing = activePulseZone.equals("right", ignoreCase = true)
            val rightBg = if (isPulsing) {
                Brush.horizontalGradient(
                    listOf(Color.Transparent, NeonRed.copy(alpha = 0.2f), NeonRed.copy(alpha = 0.75f))
                )
            } else {
                Brush.horizontalGradient(
                    listOf(Color.Transparent, NeonRed.copy(alpha = baseAlpha))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(edgeWidthDp)
                    .align(Alignment.CenterEnd)
                    .background(rightBg)
                    .pointerInput(totalWidth, totalHeight, percent) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            down.consume()
                            onTouchEdge(down.position.x / totalWidth, down.position.y / totalHeight, totalWidth, totalHeight)

                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                if (event.changes.all { !it.pressed }) break
                            }
                        }
                    }
            )
        }

        // TOP EDGE BARRIER
        if (config.topEdgeEnabled) {
            val isPulsing = activePulseZone.equals("top", ignoreCase = true)
            val topBg = if (isPulsing) {
                Brush.verticalGradient(
                    listOf(NeonRed.copy(alpha = 0.75f), NeonRed.copy(alpha = 0.2f), Color.Transparent)
                )
            } else {
                Brush.verticalGradient(
                    listOf(NeonRed.copy(alpha = baseAlpha), Color.Transparent)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(edgeWidthDp)
                    .align(Alignment.TopCenter)
                    .background(topBg)
                    .pointerInput(totalWidth, totalHeight, percent) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            down.consume()
                            onTouchEdge(down.position.x / totalWidth, down.position.y / totalHeight, totalWidth, totalHeight)

                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                if (event.changes.all { !it.pressed }) break
                            }
                        }
                    }
            )
        }

        // BOTTOM EDGE BARRIER
        if (config.bottomEdgeEnabled) {
            val isPulsing = activePulseZone.equals("bottom", ignoreCase = true)
            val bottomBg = if (isPulsing) {
                Brush.verticalGradient(
                    listOf(Color.Transparent, NeonRed.copy(alpha = 0.2f), NeonRed.copy(alpha = 0.75f))
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color.Transparent, NeonRed.copy(alpha = baseAlpha))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(edgeWidthDp)
                    .align(Alignment.BottomCenter)
                    .background(bottomBg)
                    .pointerInput(totalWidth, totalHeight, percent) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            down.consume()
                            onTouchEdge(down.position.x / totalWidth, down.position.y / totalHeight, totalWidth, totalHeight)

                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                if (event.changes.all { !it.pressed }) break
                            }
                        }
                    }
            )
        }

        // Active Pulse Indicator Badge (Center-top HUD notification when an edge touch is blocked)
        AnimatedVisibility(
            visible = activePulseZone != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                    .border(1.dp, NeonRed, RoundedCornerShape(99.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "BLOCKED: ${activePulseZone?.uppercase()} EDGE ZONE",
                    color = NeonRedLight,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
