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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BlockedEvent
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedDim
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatsSection(
    validTouches: Int,
    sessionBlockedCount: Int,
    totalDatabaseBlockedCount: Int,
    recentEvents: List<BlockedEvent>,
    onClearStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTouches = validTouches + sessionBlockedCount
    val preventionRate = if (totalTouches > 0) {
        ((sessionBlockedCount.toFloat() / totalTouches) * 100).roundToInt()
    } else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp)) // rounded-[2.5rem]
            .background(PanelDark)
            .border(1.dp, PanelBorder, RoundedCornerShape(36.dp))
            .padding(24.dp)
    ) {
        // Geometric Balance Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "METRICS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Live Touch Telemetry",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (sessionBlockedCount > 0 || validTouches > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Slate800)
                        .border(1.dp, PanelBorder, RoundedCornerShape(99.dp))
                        .clickable { onClearStats() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("reset_stats_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Reset",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Stat Grid (Geometric Balance 2-Card & Rate Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Blocked Palms Card: bg-[#FF426B]/10 border border-[#FF426B]/20 p-4 rounded-3xl
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NeonRedDim)
                    .border(1.dp, NeonRed.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .padding(14.dp)
                    .testTag("stat_blocked_card")
            ) {
                Column {
                    Text(
                        text = "BLOCKED PALMS",
                        color = NeonRedLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$sessionBlockedCount",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "All-time: $totalDatabaseBlockedCount",
                        color = NeonRedLight.copy(alpha = 0.7f),
                        fontSize = 8.sp
                    )
                }
            }

            // Valid Touches Card: bg-slate-800/50 border border-white/5 p-4 rounded-3xl
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Slate800.copy(alpha = 0.6f))
                    .border(1.dp, PanelBorder, RoundedCornerShape(24.dp))
                    .padding(14.dp)
                    .testTag("stat_valid_card")
            ) {
                Column {
                    Text(
                        text = "VALID TOUCHES",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$validTouches",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rate: $preventionRate%",
                        color = if (preventionRate > 0) MatrixGreen else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Recent Blocked Events Log Console
        if (recentEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "EVENT STREAM",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recentEvents.take(4).forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate900.copy(alpha = 0.7f))
                            .border(1.dp, PanelBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NeonRed, CircleShape)
                            )
                            Text(
                                text = "${event.edgeZone} EDGE REJECTED",
                                color = NeonRedLight,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "· ${event.profileName}",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Text(
                            text = timeFormat.format(Date(event.timestamp)),
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

