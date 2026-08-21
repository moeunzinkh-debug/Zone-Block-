package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGradientEnd
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AccessibilityPromptDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(32.dp))
                .background(PanelDark)
                .border(1.dp, PanelBorder, RoundedCornerShape(32.dp))
                .shadow(elevation = 32.dp, shape = RoundedCornerShape(32.dp), spotColor = NeonRed.copy(alpha = 0.4f))
                .padding(24.dp)
                .testTag("accessibility_prompt_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonRed.copy(alpha = 0.15f))
                                .border(1.dp, NeonRed.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = "Accessibility",
                                tint = NeonRedLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ACCESSIBILITY PERMISSION",
                                color = NeonRedLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.4.sp
                            )
                            Text(
                                text = "Real Screen Protection",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                            .clickable { onDismiss() }
                            .testTag("close_accessibility_dialog"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explanation
                Text(
                    text = "To block accidental palm and edge touches 100% across all games and apps on your device, Android requires the Accessibility Service permission.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Steps Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Slate900.copy(alpha = 0.8f))
                        .border(1.dp, PanelBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "HOW TO ENABLE (3 QUICK STEPS):",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    StepRow(
                        number = "1",
                        text = "Tap 'Open Accessibility Settings' below"
                    )

                    StepRow(
                        number = "2",
                        text = "Find 'Real Screen Edge Touch Barrier' (or Edge Protection)"
                    )

                    StepRow(
                        number = "3",
                        text = "Toggle the switch to ON & Allow screen overlay"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Features guaranteed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeaturePill(text = "100% Real Edge Block", color = MatrixGreen)
                    FeaturePill(text = "Works In All Games", color = CyberCyan)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dismiss
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate800)
                            .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 13.dp)
                            .testTag("dismiss_perm_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Later",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Settings Primary Button
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = NeonRed.copy(alpha = 0.3f))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(NeonRed, NeonRedGradientEnd)
                                )
                            )
                            .clickable { onOpenSettings() }
                            .padding(vertical = 13.dp)
                            .testTag("open_accessibility_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Settings",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Open Settings",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(NeonRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = NeonRedLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun FeaturePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = text,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
