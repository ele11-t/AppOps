package com.ele.appops.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable?,
    val isSystemApp: Boolean,
    var isSelected: Boolean = false,
    val opStatus: String = "Unknown" // "Allowed", "Ignored", "Mixed", or "Unknown"
)
