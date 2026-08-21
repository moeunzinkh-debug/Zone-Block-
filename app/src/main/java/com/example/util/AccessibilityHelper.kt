package com.example.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.service.EdgeTouchBlockAccessibilityService

object AccessibilityHelper {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        // Method 1: Check active service instance singleton
        if (EdgeTouchBlockAccessibilityService.isRunning()) {
            return true
        }

        // Method 2: Check enabled accessibility services string in settings
        val expectedComponentName = ComponentName(context, EdgeTouchBlockAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""

        if (enabledServices.contains(expectedComponentName) || enabledServices.contains(EdgeTouchBlockAccessibilityService::class.java.simpleName)) {
            return true
        }

        // Method 3: Query AccessibilityManager
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val runningServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in runningServices) {
            val resolveInfo = service.resolveInfo ?: continue
            if (resolveInfo.serviceInfo?.packageName == context.packageName &&
                resolveInfo.serviceInfo?.name == EdgeTouchBlockAccessibilityService::class.java.name) {
                return true
            }
        }
        return false
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Ignore fallback
            }
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                openAccessibilitySettings(context)
            }
        }
    }
}
