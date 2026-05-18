package com.gamevault.domain.model

enum class VaultItemType {
    PHOTO,
    VIDEO,
    DOCUMENT,
    APP,
    AUDIO,
    OTHER
}

enum class SortBy {
    DATE,
    NAME,
    SIZE,
    TYPE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

data class VaultFolder(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
)

data class VaultItem(
    val id: Long = 0,
    val name: String,
    val type: VaultItemType,
    val path: String,
    val size: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val isHidden: Boolean = true,
    val folderId: Long? = null // null means root level
)

data class VaultSortConfig(
    val sortBy: SortBy = SortBy.DATE,
    val sortOrder: SortOrder = SortOrder.DESCENDING
)