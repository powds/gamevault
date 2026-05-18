package com.gamevault.domain.model

enum class VaultItemType {
    PHOTO,
    VIDEO,
    DOCUMENT,
    APP
}

data class VaultItem(
    val id: Long = 0,
    val name: String,
    val type: VaultItemType,
    val path: String,
    val size: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val isHidden: Boolean = true
)