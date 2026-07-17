package com.babyvoice.bridge.integration.assistant

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

fun Context.isAccessibilityServiceEnabled(serviceClassName: String): Boolean {
    val enabled = Settings.Secure.getInt(
        contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0,
    ) == 1
    if (!enabled) return false

    val expected = ComponentName(packageName, serviceClassName).flattenToString()
    val enabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ).orEmpty()
    return enabledServices
        .split(':')
        .any { it == expected }
}
