package com.gamevault.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.gamevault.data.local.*
import com.gamevault.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultFolderDao: VaultFolderDao,
    private val vaultItemDao: VaultItemDao,
    private val hiddenAppDao: HiddenAppDao,
    private val gameStateDao: GameStateDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gamevault_prefs", Context.MODE_PRIVATE)
    private val vaultDir: File = File(context.filesDir, "vault_files")

    init {
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }
    }

    // ==================== Folders ====================

    fun getFolders(): Flow<List<VaultFolder>> {
        return vaultFolderDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createFolder(name: String): Long {
        val folder = VaultFolderEntity(
            name = name,
            createdAt = System.currentTimeMillis()
        )
        return vaultFolderDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folderId: Long) {
        // Move all items in folder to root
        val items = vaultItemDao.getItemsInFolder(folderId).first()
        for (item in items) {
            vaultItemDao.updateItem(item.copy(folderId = null))
        }
        vaultFolderDao.deleteFolderById(folderId)
    }

    suspend fun renameFolder(folderId: Long, newName: String) {
        val folder = vaultFolderDao.getFolderById(folderId)
        folder?.let {
            vaultFolderDao.updateFolder(it.copy(name = newName))
        }
    }

    suspend fun getFolderItemCount(folderId: Long): Int {
        return vaultFolderDao.getItemCountInFolder(folderId)
    }

    // ==================== Vault Items ====================

    fun getVaultItems(): Flow<List<VaultItem>> {
        return vaultItemDao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRootItems(): Flow<List<VaultItem>> {
        return vaultItemDao.getRootItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getItemsInFolder(folderId: Long): Flow<List<VaultItem>> {
        return vaultItemDao.getItemsInFolder(folderId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addVaultItem(item: VaultItem): Long {
        val entity = item.toEntity()
        vaultItemDao.insertItem(entity)
        return entity.id
    }

    suspend fun updateVaultItem(item: VaultItem) {
        vaultItemDao.updateItem(item.toEntity())
    }

    suspend fun deleteVaultItem(id: Long) {
        vaultItemDao.deleteById(id)
    }

    suspend fun getVaultItem(id: Long): VaultItem? {
        return vaultItemDao.getItemById(id)?.toDomain()
    }

    suspend fun moveItemToFolder(itemId: Long, folderId: Long?) {
        val item = vaultItemDao.getItemById(itemId)
        item?.let {
            vaultItemDao.updateItem(it.copy(folderId = folderId))
        }
    }

    suspend fun deleteAllVaultItems() {
        vaultDir.listFiles()?.forEach { it.delete() }
        vaultItemDao.deleteAll()
    }

    // ==================== Search ====================

    fun searchVaultItems(query: String): Flow<List<VaultItem>> {
        return vaultItemDao.searchItems(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getItemsByType(type: VaultItemType): Flow<List<VaultItem>> {
        return vaultItemDao.getItemsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun sortItems(items: List<VaultItem>, config: VaultSortConfig): List<VaultItem> {
        val sorted = when (config.sortBy) {
            SortBy.DATE -> items.sortedBy { it.dateAdded }
            SortBy.NAME -> items.sortedBy { it.name.lowercase() }
            SortBy.SIZE -> items.sortedBy { it.size }
            SortBy.TYPE -> items.sortedBy { it.type.ordinal }
        }
        return if (config.sortOrder == SortOrder.DESCENDING) sorted.reversed() else sorted
    }

    // ==================== Thumbnails ====================

    suspend fun generateThumbnail(item: VaultItem): String? = withContext(Dispatchers.IO) {
        try {
            val thumbnailDir = File(context.filesDir, "thumbnails")
            if (!thumbnailDir.exists()) thumbnailDir.mkdirs()

            val thumbnailFile = File(thumbnailDir, "thumb_${item.id}.jpg")
            if (thumbnailFile.exists()) return@withContext thumbnailFile.absolutePath

            when (item.type) {
                VaultItemType.PHOTO -> generatePhotoThumbnail(item.path, thumbnailFile)
                VaultItemType.VIDEO -> generateVideoThumbnail(item.path, thumbnailFile)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadThumbnail(item: VaultItem): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val thumbnailPath = item.thumbnailPath ?: return@withContext null
            val file = File(thumbnailPath)
            if (file.exists()) {
                BitmapFactory.decodeFile(thumbnailPath)
            } else {
                // Generate if missing
                val path = generateThumbnail(item)
                path?.let { BitmapFactory.decodeFile(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun generatePhotoThumbnail(sourcePath: String, destFile: File): String? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
            }
            val bitmap = BitmapFactory.decodeFile(sourcePath, options)
            if (bitmap != null) {
                Bitmap.createScaledBitmap(bitmap, 200, 200, true).let { scaled ->
                    FileOutputStream(destFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    scaled.recycle()
                    bitmap.recycle()
                    destFile.absolutePath
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun generateVideoThumbnail(sourcePath: String, destFile: File): String? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(
                    android.net.Uri.fromFile(File(sourcePath)),
                    Size(200, 200),
                    null
                )
            } else {
                @Suppress("DEPRECATION")
                ThumbnailUtils.createVideoThumbnail(sourcePath, MediaStore.Images.Thumbnails.MINI_KIND)
            }
            if (bitmap != null) {
                FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                bitmap.recycle()
                destFile.absolutePath
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Vault Storage ====================

    fun getVaultStorageUsed(): Long {
        return vaultDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    // ==================== Hidden Apps ====================

    fun getHiddenApps(): Flow<List<HiddenApp>> {
        return hiddenAppDao.getAllHiddenApps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun hideApp(app: HiddenApp) {
        hiddenAppDao.hideApp(app.toEntity())
    }

    suspend fun unhideApp(packageName: String) {
        hiddenAppDao.unhideApp(packageName)
    }

    suspend fun isAppHidden(packageName: String): Boolean {
        return hiddenAppDao.getHiddenApp(packageName) != null
    }

    // ==================== Security ====================

    fun isPatternSet(): Boolean = prefs.getBoolean("pattern_set", false)
    fun setPattern(pattern: String) = prefs.edit().putString("pattern", pattern).putBoolean("pattern_set", true).apply()
    fun getPattern(): String = prefs.getString("pattern", "") ?: ""
    fun isPinSet(): Boolean = prefs.getBoolean("pin_set", false)
    fun setPin(pin: String) = prefs.edit().putString("pin", pin).putBoolean("pin_set", true).apply()
    fun getPin(): String = prefs.getString("pin", "") ?: ""
    fun verifyPattern(inputPattern: String): Boolean = getPattern() == inputPattern
    fun verifyPin(inputPin: String): Boolean = getPin() == inputPin
    fun clearSecurity() = prefs.edit().remove("pattern").remove("pattern_set").remove("pin").remove("pin_set").apply()

    fun isDecoyPinSet(): Boolean = prefs.getBoolean("decoy_pin_set", false)
    fun setDecoyPin(pin: String) = prefs.edit().putString("decoy_pin", pin).putBoolean("decoy_pin_set", true).apply()
    fun getDecoyPin(): String = prefs.getString("decoy_pin", "") ?: ""
    fun verifyDecoyPin(inputPin: String): Boolean = getDecoyPin() == inputPin

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", false)
    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean("biometric_enabled", enabled).apply()

    fun getAutoLockTimeout(): Int = prefs.getInt("auto_lock_timeout", 60)
    fun setAutoLockTimeout(seconds: Int) = prefs.edit().putInt("auto_lock_timeout", seconds).apply()
    fun getLastActiveTime(): Long = prefs.getLong("last_active_time", System.currentTimeMillis())
    fun updateLastActiveTime() = prefs.edit().putLong("last_active_time", System.currentTimeMillis()).apply()
    fun isAutoLockEnabled(): Boolean = prefs.getBoolean("auto_lock_enabled", true)
    fun setAutoLockEnabled(enabled: Boolean) = prefs.edit().putBoolean("auto_lock_enabled", enabled).apply()
    fun shouldAutoLock(): Boolean = if (!isAutoLockEnabled()) false else (System.currentTimeMillis() - getLastActiveTime()) / 1000 > getAutoLockTimeout()

    fun isIntruderCaptureEnabled(): Boolean = prefs.getBoolean("intruder_capture", true)
    fun setIntruderCaptureEnabled(enabled: Boolean) = prefs.edit().putBoolean("intruder_capture", enabled).apply()
    fun getFailedAttemptCount(): Int = prefs.getInt("failed_attempts", 0)
    fun incrementFailedAttempts(): Int {
        val count = getFailedAttemptCount() + 1
        prefs.edit().putInt("failed_attempts", count).apply()
        return count
    }
    fun resetFailedAttempts() = prefs.edit().putInt("failed_attempts", 0).apply()

    fun getBestScore(): Int = prefs.getInt("best_score", 0)
    fun saveBestScore(score: Int) {
        if (score > getBestScore()) prefs.edit().putInt("best_score", score).apply()
    }

    suspend fun getGameState(): GameStateEntity? = gameStateDao.getGameState()

    suspend fun saveGameState(score: Int, bestScore: Int, grid: List<List<Int>>) {
        val gridJson = JSONArray(grid.map { row -> JSONArray(row) }).toString()
        saveBestScore(bestScore)
        gameStateDao.saveGameState(GameStateEntity(id = 1, score = score, bestScore = bestScore, gridJson = gridJson))
    }

    fun getDecoyItemCount(): Int = prefs.getInt("decoy_item_count", 0)
    fun setDecoyItemCount(count: Int) = prefs.edit().putInt("decoy_item_count", count).apply()

    // ==================== Mappers ====================

    private fun VaultFolderEntity.toDomain(): VaultFolder = VaultFolder(id = id, name = name, createdAt = createdAt)

    private fun VaultItemEntity.toDomain(): VaultItem = VaultItem(
        id = id, name = name, type = VaultItemType.valueOf(type),
        path = path, size = size, dateAdded = dateAdded,
        thumbnailPath = thumbnailPath, folderId = folderId
    )

    private fun VaultItem.toEntity(): VaultItemEntity = VaultItemEntity(
        id = id, name = name, type = type.name, path = path,
        size = size, dateAdded = dateAdded, thumbnailPath = thumbnailPath, folderId = folderId
    )

    private fun HiddenAppEntity.toDomain(): HiddenApp = HiddenApp(
        packageName = packageName, appName = appName,
        iconPath = iconPath, dateHidden = dateHidden
    )

    private fun HiddenApp.toEntity(): HiddenAppEntity = HiddenAppEntity(
        packageName = packageName, appName = appName,
        iconPath = iconPath, dateHidden = dateHidden
    )
}