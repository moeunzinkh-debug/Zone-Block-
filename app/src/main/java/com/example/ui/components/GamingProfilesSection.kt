package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProfile
import com.example.ui.theme.CyberCyan
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GamingProfilesSection(
    currentProfileName: String,
    currentPercent: Int,
    onSelectProfile: (GameProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp)) // rounded-[2.5rem]
            .background(PanelDark)
            .border(1.dp, PanelBorder, RoundedCornerShape(36.dp))
            .padding(24.dp)
    ) {
        // Geometric Balance Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVE PROFILE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gaming Presets",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Active Profile Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(NeonRedDim)
                    .border(1.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = currentProfileName.uppercase(),
                    color = NeonRedLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2-Column Profile Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameProfile.values().forEach { profile ->
                val isSelected = currentProfileName.equals(profile.title, ignoreCase = true) ||
                        (profile != GameProfile.CUSTOM && currentPercent == profile.percent)

                val cardBg = if (isSelected) Slate800.copy(alpha = 0.9f) else Slate900.copy(alpha = 0.6f)
                val cardBorder = if (isSelected) NeonRed else PanelBorder
                val borderWidth = if (isSelected) 2.dp else 1.dp

                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .clip(RoundedCornerShape(18.dp))
                        .shadow(
                            elevation = if (isSelected) 8.dp else 0.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = NeonRed.copy(alpha = 0.25f)
                        )
                        .background(cardBg)
                        .border(borderWidth, cardBorder, RoundedCornerShape(18.dp))
                        .clickable { onSelectProfile(profile) }
                        .padding(14.dp)
                        .testTag("profile_card_${profile.name.lowercase()}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.iconName,
                                color = if (isSelected) NeonRedLight else CyberCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.percent}%",
                                color = if (isSelected) NeonRedLight else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = profile.title,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = profile.subtitle,
                            color = TextMuted,
                            fontSize = 9.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

