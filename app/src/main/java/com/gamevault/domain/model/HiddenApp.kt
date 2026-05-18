package com.gamevault.domain.model

data class HiddenApp(
    val packageName: String,
    val appName: String,
    val iconPath: String? = null,
    val dateHidden: Long = System.currentTimeMillis()
)