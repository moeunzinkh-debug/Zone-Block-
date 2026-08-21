package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EdgeProtectionConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GameYellow
import com.example.ui.theme.GameYellowDim
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGradientEnd
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ControlsSection(
    config: EdgeProtectionConfig,
    isAccessibilityEnabled: Boolean,
    onToggleProtection: () -> Unit,
    onZoneSizeChange: (Int) -> Unit,
    onToggleEdge: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onResetDefault: () -> Unit,
    onEmergencyStop: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percent = config.zoneSizePercent
    val isProtectionActive = config.isEnabled && percent > 0
    val demoWidthPx = 190
    val estimatedPx = if (isProtectionActive) (demoWidthPx * percent / 100f).roundToInt() else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp)) // rounded-[2.5rem]
            .background(PanelDark)
            .border(1.dp, PanelBorder, RoundedCornerShape(36.dp))
            .padding(24.dp)
    ) {
        // SYSTEM OVERLAY / ACCESSIBILITY STATUS CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isAccessibilityEnabled) MatrixGreen.copy(alpha = 0.12f)
                    else NeonRed.copy(alpha = 0.12f)
                )
                .border(
                    1.dp,
                    if (isAccessibilityEnabled) MatrixGreen.copy(alpha = 0.4f)
                    else NeonRed.copy(alpha = 0.4f),
                    RoundedCornerShape(20.dp)
                )
                .clickable { onOpenAccessibilitySettings() }
                .padding(14.dp)
                .testTag("accessibility_status_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAccessibilityEnabled) MatrixGreen.copy(alpha = 0.2f)
                                else NeonRed.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAccessibilityEnabled) Icons.Default.Security else Icons.Default.AccessibilityNew,
                            contentDescription = "Status",
                            tint = if (isAccessibilityEnabled) MatrixGreen else NeonRedLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isAccessibilityEnabled) "REAL SCREEN BARRIER ACTIVE" else "ACCESSIBILITY PERMISSION NEEDED",
                            color = if (isAccessibilityEnabled) MatrixGreen else NeonRedLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAccessibilityEnabled) "100% physically blocking edges over games" else "Required to block edges across all games & apps",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isAccessibilityEnabled) MatrixGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.25f))
                        .border(
                            1.dp,
                            if (isAccessibilityEnabled) MatrixGreen.copy(alpha = 0.5f) else NeonRed.copy(alpha = 0.5f),
                            RoundedCornerShape(99.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isAccessibilityEnabled) "Settings" else "Enable",
                            color = if (isAccessibilityEnabled) MatrixGreen else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) MatrixGreen else Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: MASTER ACTIVE PROTECTION TOGGLE (Geometric Balance Header)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Active Protection",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isProtectionActive) "Currently blocking $percent% of screen edges" else "Touch protection is currently disabled",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Geometric Glow Switch
            GeometricToggleSwitch(
                checked = config.isEnabled,
                onCheckedChange = { onToggleProtection() },
                modifier = Modifier.testTag("protection_master_switch")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PanelBorder)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: ZONE THICKNESS (Geometric Balance Layout)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "ZONE THICKNESS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )
                Text(
                    text = "$percent%",
                    color = NeonRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("zone_value_display")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Slider(
                value = percent.toFloat(),
                onValueChange = { onZoneSizeChange(it.roundToInt()) },
                valueRange = 0f..24f,
                steps = 23,
                colors = SliderDefaults.colors(
                    thumbColor = NeonRed,
                    activeTrackColor = NeonRed,
                    inactiveTrackColor = Slate800
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_size_slider")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0% (OFF)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "24% (MAX)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Measurements cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Left / right thickness",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$estimatedPx px",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("horizontal_px_readout")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Top / bottom thickness",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$estimatedPx px · equal",
                            color = CyberCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("vertical_px_readout")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PanelBorder)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: ACTIVE EDGES SELECTOR
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Active edge zones",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customize which screen edges reject accidental palm swipes",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EdgeChip(name = "Top Edge", enabled = config.topEdgeEnabled, onClick = { onToggleEdge("TOP") })
                EdgeChip(name = "Bottom Edge", enabled = config.bottomEdgeEnabled, onClick = { onToggleEdge("BOTTOM") })
                EdgeChip(name = "Left Edge", enabled = config.leftEdgeEnabled, onClick = { onToggleEdge("LEFT") })
                EdgeChip(name = "Right Edge", enabled = config.rightEdgeEnabled, onClick = { onToggleEdge("RIGHT") })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PanelBorder)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: HAPTICS & ACTION BUTTONS (Geometric Balance 2-Column Grid)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptics",
                        tint = if (config.hapticFeedbackEnabled) CyberCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Haptic rejection pulse",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vibrate softly on blocked edge touch",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = config.hapticFeedbackEnabled,
                    onCheckedChange = { onToggleHaptics() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberCyan,
                        checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Slate800
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2-Column Action Buttons matching Geometric Balance HTML Spec
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reset Button: py-3.5 bg-slate-800 rounded-2xl border border-white/5
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .clickable { onResetDefault() }
                        .padding(vertical = 14.dp)
                        .testTag("reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Reset Defaults",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Save Button: bg-gradient-to-r from-[#FF426B] to-[#D73778] rounded-2xl shadow-lg shadow-pink-500/20
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = NeonRed.copy(alpha = 0.3f))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonRed, NeonRedGradientEnd)
                            )
                        )
                        .clickable { onSaveSettings() }
                        .padding(vertical = 14.dp)
                        .testTag("save_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "SAVE CONFIG",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Emergency Stop Option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonRed.copy(alpha = 0.08f))
                    .border(1.dp, NeonRed.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .clickable { onEmergencyStop() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = NeonRedLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "EMERGENCY SHUTOFF",
                        color = NeonRedLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: ANDROID NOTICE NOTE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GameYellowDim)
                .border(1.dp, GameYellow.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Note",
                    tint = GameYellow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Android APK notice: the interactive touch guard operates within the preview arena and simulator. System-wide edge overlays utilize Android AccessibilityService in native device environments.",
                    color = Color(0xFFE6D0A3),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun EdgeChip(
    name: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (enabled) NeonRed.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f)
    val borderColor = if (enabled) NeonRed.copy(alpha = 0.6f) else PanelBorder
    val textColor = if (enabled) NeonRedLight else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(99.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (enabled) NeonRed else TextMuted, CircleShape)
            )
            Text(
                text = name,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GeometricToggleSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackBg by animateColorAsState(
        targetValue = if (checked) MatrixGreen.copy(alpha = 0.2f) else Slate800,
        label = "switch_track"
    )
    val thumbBg by animateColorAsState(
        targetValue = if (checked) MatrixGreen else TextMuted,
        label = "switch_thumb"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        label = "switch_offset"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(trackBg)
            .border(1.dp, if (checked) MatrixGreen.copy(alpha = 0.3f) else PanelBorder, RoundedCornerShape(99.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(16.dp)
                .shadow(elevation = if (checked) 8.dp else 0.dp, shape = CircleShape, spotColor = MatrixGreen)
                .background(thumbBg, CircleShape)
        )
    }
}

