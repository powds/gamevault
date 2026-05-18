package com.gamevault.presentation.common.theme

import androidx.compose.ui.graphics.Color

// 2048 Game Colors
val GameBackground = Color(0xFFFAF8EF)
val GameBoard = Color(0xFFBBADA0)
val TileEmpty = Color(0xFFCDC1B4)

val Tile2 = Color(0xFFEEE4DA)
val Tile4 = Color(0xFFEDE0C8)
val Tile8 = Color(0xFFF2B179)
val Tile16 = Color(0xFFF59563)
val Tile32 = Color(0xFFF67C5F)
val Tile64 = Color(0xFFF65E3B)
val Tile128 = Color(0xFFEDCF72)
val Tile256 = Color(0xFFEDCC61)
val Tile512 = Color(0xFFEDC850)
val Tile1024 = Color(0xFFEDC53F)
val Tile2048 = Color(0xFFEDC22E)
val TileSuper = Color(0xFF3C3A32)

val TextDark = Color(0xFF776E65)
val TextLight = Color(0xFFF9F6F2)

// Vault Colors
val VaultBackground = Color(0xFF1A1A2E)
val VaultSurface = Color(0xFF16213E)
val VaultPrimary = Color(0xFFE94560)
val VaultSecondary = Color(0xFF0F3460)
val VaultText = Color(0xFFE8E8E8)

fun getTileColor(value: Int): Color = when {
    value <= 0 -> TileEmpty
    value == 2 -> Tile2
    value == 4 -> Tile4
    value == 8 -> Tile8
    value == 16 -> Tile16
    value == 32 -> Tile32
    value == 64 -> Tile64
    value == 128 -> Tile128
    value == 256 -> Tile256
    value == 512 -> Tile512
    value == 1024 -> Tile1024
    value == 2048 -> Tile2048
    else -> TileSuper
}

fun getTextColor(value: Int): Color = if (value <= 4) TextDark else TextLight