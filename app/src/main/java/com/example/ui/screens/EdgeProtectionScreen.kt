package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RealScreenEdgeOverlay
import com.example.ui.components.AccessibilityPromptDialog
import com.example.ui.components.ControlsSection
import com.example.ui.components.FullScreenTestDialog
import com.example.ui.components.GamingProfilesSection
import com.example.ui.components.PhoneSimulator
import com.example.ui.components.StatsSection
import com.example.ui.components.ToastBanner
import com.example.ui.components.TopBrandBar
import com.example.ui.theme.BgDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EdgeProtectionViewModel

@Composable
fun EdgeProtectionScreen(
    viewModel: EdgeProtectionViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val showAccessibilityDialog by viewModel.showAccessibilityDialog.collectAsStateWithLifecycle()
    val activePulseZone by viewModel.activePulseZone.collectAsStateWithLifecycle()
    val lastTouchFeedback by viewModel.lastTouchFeedback.collectAsStateWithLifecycle()
    val validTouchCount by viewModel.validTouchCount.collectAsStateWithLifecycle()
    val sessionBlockedCount by viewModel.sessionBlockedCount.collectAsStateWithLifecycle()
    val totalDatabaseBlockedCount by viewModel.totalDatabaseBlockedCount.collectAsStateWithLifecycle()
    val recentBlockedEvents by viewModel.recentBlockedEvents.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isFullScreenSandboxOpen by viewModel.isFullScreenSandboxOpen.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
                // Atmospheric ambient radial background gradients
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonRed.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(size.width * 0.85f, 0f),
                            radius = size.width * 0.75f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(CyberCyan.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(0f, size.height),
                            radius = size.width * 0.6f
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Top Fixed Brand Navigation Bar
                TopBrandBar(
                    onOpenFullScreenTest = { viewModel.openFullScreenSandbox(true) }
                )

                // Scrollable Content
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    val isTabletOrWide = maxWidth >= 840.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .widthIn(max = 1120.dp)
                            .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Eyebrow
                        Text(
                            text = "TOUCH GUARD",
                            color = NeonRedLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.6.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Headline
                        Text(
                            text = "Edge protection",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.8).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle
                        Text(
                            text = "Real-time edge touch rejection barrier. Block top, left, right, and bottom screen zones across all games and apps.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.widthIn(max = 680.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Main Content (Side-by-Side on wide screens, stacked on compact)
                        if (isTabletOrWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                PhoneSimulator(
                                    config = config,
                                    activePulseZone = activePulseZone,
                                    lastTouchFeedback = lastTouchFeedback,
                                    onTouchScreen = { xRatio, yRatio, widthPx, heightPx ->
                                        viewModel.handleScreenTouch(xRatio, yRatio, widthPx, heightPx)
                                    },
                                    modifier = Modifier.weight(0.85f)
                                )

                                Column(
                                    modifier = Modifier.weight(1.15f),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    ControlsSection(
                                        config = config,
                                        isAccessibilityEnabled = isAccessibilityEnabled,
                                        onToggleProtection = { viewModel.toggleProtection() },
                                        onZoneSizeChange = { viewModel.setZoneSizePercent(it) },
                                        onToggleEdge = { viewModel.toggleEdge(it) },
                                        onSaveSettings = { viewModel.saveSettings() },
                                        onResetDefault = { viewModel.resetToDefault() },
                                        onEmergencyStop = { viewModel.emergencyStop() },
                                        onToggleHaptics = { viewModel.toggleHaptics() },
                                        onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() }
                                    )

                                    GamingProfilesSection(
                                        currentProfileName = config.profileName,
                                        currentPercent = config.zoneSizePercent,
                                        onSelectProfile = { viewModel.selectProfile(it) }
                                    )

                                    StatsSection(
                                        validTouches = validTouchCount,
                                        sessionBlockedCount = sessionBlockedCount,
                                        totalDatabaseBlockedCount = totalDatabaseBlockedCount,
                                        recentEvents = recentBlockedEvents,
                                        onClearStats = { viewModel.clearStats() }
                                    )
                                }
                            }
                        } else {
                            // Stacked Vertical Layout for standard handheld devices
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                PhoneSimulator(
                                    config = config,
                                    activePulseZone = activePulseZone,
                                    lastTouchFeedback = lastTouchFeedback,
                                    onTouchScreen = { xRatio, yRatio, widthPx, heightPx ->
                                        viewModel.handleScreenTouch(xRatio, yRatio, widthPx, heightPx)
                                    }
                                )

                                ControlsSection(
                                    config = config,
                                    isAccessibilityEnabled = isAccessibilityEnabled,
                                    onToggleProtection = { viewModel.toggleProtection() },
                                    onZoneSizeChange = { viewModel.setZoneSizePercent(it) },
                                    onToggleEdge = { viewModel.toggleEdge(it) },
                                    onSaveSettings = { viewModel.saveSettings() },
                                    onResetDefault = { viewModel.resetToDefault() },
                                    onEmergencyStop = { viewModel.emergencyStop() },
                                    onToggleHaptics = { viewModel.toggleHaptics() },
                                    onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() }
                                )

                                GamingProfilesSection(
                                    currentProfileName = config.profileName,
                                    currentPercent = config.zoneSizePercent,
                                    onSelectProfile = { viewModel.selectProfile(it) }
                                )

                                StatsSection(
                                    validTouches = validTouchCount,
                                    sessionBlockedCount = sessionBlockedCount,
                                    totalDatabaseBlockedCount = totalDatabaseBlockedCount,
                                    recentEvents = recentBlockedEvents,
                                    onClearStats = { viewModel.clearStats() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Footer
                        Text(
                            text = "Real Screen Edge Touch Barrier runs via Android Accessibility Service to intercept and block accidental palm contacts across system games and apps.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 24.dp)
                        )

                        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                    }
                }
            }

            // Floating Toast notification
            ToastBanner(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(end = 20.dp, bottom = 24.dp)
            )

            // Real Screen Edge Barrier Overlay (Applies barrier directly across the entire screen)
            RealScreenEdgeOverlay(
                config = config,
                activePulseZone = activePulseZone,
                onTouchEdge = { xRatio, yRatio, widthPx, heightPx ->
                    viewModel.handleScreenTouch(xRatio, yRatio, widthPx, heightPx)
                }
            )

            // Accessibility Permission Dialog
            AccessibilityPromptDialog(
                isOpen = showAccessibilityDialog,
                onDismiss = { viewModel.dismissAccessibilityDialog() },
                onOpenSettings = { viewModel.openAccessibilitySettings() }
            )

            // Full Screen Gaming Test Arena
            FullScreenTestDialog(
                isOpen = isFullScreenSandboxOpen,
                config = config,
                activePulseZone = activePulseZone,
                lastTouchFeedback = lastTouchFeedback,
                onTouchScreen = { xRatio, yRatio, widthPx, heightPx ->
                    viewModel.handleScreenTouch(xRatio, yRatio, widthPx, heightPx)
                },
                onDismiss = { viewModel.openFullScreenSandbox(false) }
            )
        }
    }
}
