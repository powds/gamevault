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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultFolderDao: VaultFolderDao,
    private val vaultItemDao: VaultItemDao,
    private val hiddenAppDao: HiddenAppDao,
    private val gameStateDao: GameStateDao,
    private val cryptoManager: CryptoManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gamevault_prefs", Context.MODE_PRIVATE)
    private val vaultDir: File = File(context.filesDir, "vault_files")
    private val encryptedVaultDir: File = File(context.filesDir, "encrypted_vault")
    private val backupDir: File = File(context.filesDir, "backups")

    init {
        if (!vaultDir.exists()) vaultDir.mkdirs()
        if (!encryptedVaultDir.exists()) encryptedVaultDir.mkdirs()
        if (!backupDir.exists()) backupDir.mkdirs()
    }

    // ==================== Encrypted File Storage ====================

    suspend fun encryptAndStoreFile(sourcePath: String, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return@withContext null

            val encryptedFileName = "${System.currentTimeMillis()}_$fileName.enc"
            val encryptedFile = File(encryptedVaultDir, encryptedFileName)

            val success = cryptoManager.encryptFile(sourceFile, encryptedFile)
            if (success) {
                // Store original size as metadata
                File("$encryptedFile.meta").writeText(sourceFile.length().toString())
                encryptedFile.absolutePath
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun decryptFileForViewing(encryptedFilePath: String, destPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encryptedFile = File(encryptedFilePath)
            if (!encryptedFile.exists()) return@withContext false

            val destFile = File(destPath)
            cryptoManager.decryptFile(encryptedFile, destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteEncryptedFile(encryptedFilePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(encryptedFilePath)
            if (file.exists()) {
                File("$encryptedFilePath.meta").delete()
                file.delete()
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getEncryptedStorageUsed(): Long {
        return encryptedVaultDir.walkTopDown().filter { it.isFile && it.extension == "enc" }
            .sumOf { it.length() }
    }

    fun getOriginalFileSize(encryptedFilePath: String): Long {
        val metaFile = File("$encryptedFilePath.meta")
        return if (metaFile.exists()) {
            metaFile.readText().toLongOrNull() ?: 0L
        } else 0L
    }

    fun getEncryptedFiles(): List<File> {
        return encryptedVaultDir.listFiles()?.filter { it.extension == "enc" } ?: emptyList()
    }

    fun isEncryptionEnabled(): Boolean = prefs.getBoolean("encryption_enabled", false)
    fun setEncryptionEnabled(enabled: Boolean) = prefs.edit().putBoolean("encryption_enabled", enabled).apply()

    // ==================== Backup ====================

    data class BackupMetadata(
        val backupId: String,
        val timestamp: Long,
        val itemCount: Int,
        val totalSize: Long,
        val encrypted: Boolean
    )

    suspend fun createLocalBackup(): BackupMetadata? = withContext(Dispatchers.IO) {
        try {
            val backupId = "backup_${System.currentTimeMillis()}"
            val backupFolder = File(backupDir, backupId)
            backupFolder.mkdirs()

            // Export database to JSON
            val metadataJson = exportDatabaseToJson()
            val metadataFile = File(backupFolder, "metadata.json")
            metadataFile.writeText(metadataJson)

            // Copy encrypted files
            var totalSize = metadataFile.length()
            encryptedVaultDir.listFiles()?.filter { it.extension == "enc" }?.forEach { file ->
                val destFile = File(backupFolder, file.name)
                file.copyTo(destFile, overwrite = true)
                totalSize += destFile.length()

                // Copy metadata
                val metaSrc = File("${file.absolutePath}.meta")
                if (metaSrc.exists()) {
                    metaSrc.copyTo(File(backupFolder, "${file.name}.meta"), overwrite = true)
                }
            }

            val manifest = JSONObject().apply {
                put("backupId", backupId)
                put("timestamp", System.currentTimeMillis())
                put("itemCount", encryptedVaultDir.listFiles()?.count { it.extension == "enc" } ?: 0)
                put("totalSize", totalSize)
                put("encrypted", isEncryptionEnabled())
                put("appVersion", "1.0")
            }

            val manifestFile = File(backupFolder, "manifest.json")
            manifestFile.writeText(manifest.toString())

            BackupMetadata(
                backupId = backupId,
                timestamp = System.currentTimeMillis(),
                itemCount = encryptedVaultDir.listFiles()?.count { it.extension == "enc" } ?: 0,
                totalSize = totalSize,
                encrypted = isEncryptionEnabled()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreLocalBackup(backupId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFolder = File(backupDir, backupId)
            if (!backupFolder.exists()) return@withContext false

            val manifestFile = File(backupFolder, "manifest.json")
            if (!manifestFile.exists()) return@withContext false

            // Restore encrypted files
            backupFolder.listFiles()?.filter { it.extension == "enc" }?.forEach { file ->
                val destFile = File(encryptedVaultDir, file.name)
                file.copyTo(destFile, overwrite = true)

                // Restore metadata
                val metaSrc = File("${file.absolutePath}.meta")
                if (metaSrc.exists()) {
                    metaSrc.copyTo(File("${destFile.absolutePath}.meta"), overwrite = true)
                }
            }

            // Import database from JSON
            val metadataFile = File(backupFolder, "metadata.json")
            if (metadataFile.exists()) {
                importDatabaseFromJson(metadataFile.readText())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getLocalBackups(): List<BackupMetadata> {
        return backupDir.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { folder ->
                val manifestFile = File(folder, "manifest.json")
                if (manifestFile.exists()) {
                    try {
                        val obj = JSONObject(manifestFile.readText())
                        BackupMetadata(
                            backupId = obj.getString("backupId"),
                            timestamp = obj.getLong("timestamp"),
                            itemCount = obj.getInt("itemCount"),
                            totalSize = obj.getLong("totalSize"),
                            encrypted = obj.getBoolean("encrypted")
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    fun deleteLocalBackup(backupId: String): Boolean {
        val backupFolder = File(backupDir, backupId)
        return backupFolder.deleteRecursively()
    }

    fun formatBackupSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val exportData = JSONObject()

        // Export folders
        val foldersArray = JSONArray()
        val folders = vaultFolderDao.getAllFolders().first()
        folders.forEach { folder ->
            val folderObj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("createdAt", folder.createdAt)
            }
            foldersArray.put(folderObj)
        }
        exportData.put("folders", foldersArray)

        // Export items metadata
        val itemsArray = JSONArray()
        val items = vaultItemDao.getAllItems().first()
        items.forEach { item ->
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("type", item.type)
                put("path", item.path)
                put("size", item.size)
                put("dateAdded", item.dateAdded)
                put("thumbnailPath", item.thumbnailPath ?: "")
                put("folderId", item.folderId ?: 0)
            }
            itemsArray.put(itemObj)
        }
        exportData.put("items", itemsArray)

        // Export hidden apps
        val appsArray = JSONArray()
        val apps = hiddenAppDao.getAllHiddenApps().first()
        apps.forEach { app ->
            val appObj = JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
                put("iconPath", app.iconPath ?: "")
                put("dateHidden", app.dateHidden)
            }
            appsArray.put(appObj)
        }
        exportData.put("hiddenApps", appsArray)

        // Export settings
        val settings = JSONObject().apply {
            put("encryption_enabled", isEncryptionEnabled())
            put("auto_lock_enabled", prefs.getBoolean("auto_lock_enabled", true))
            put("auto_lock_timeout", prefs.getInt("auto_lock_timeout", 60))
            put("biometric_enabled", prefs.getBoolean("biometric_enabled", false))
            put("intruder_capture", prefs.getBoolean("intruder_capture", true))
            put("decoy_pin_set", prefs.getBoolean("decoy_pin_set", false))
        }
        exportData.put("settings", settings)

        exportData.toString()
    }

    private suspend fun importDatabaseFromJson(json: String) = withContext(Dispatchers.IO) {
        try {
            val data = JSONObject(json)

            // Import folders
            if (data.has("folders")) {
                val folders = data.getJSONArray("folders")
                for (i in 0 until folders.length()) {
                    val folder = folders.getJSONObject(i)
                    vaultFolderDao.insertFolder(
                        VaultFolderEntity(
                            id = folder.getLong("id"),
                            name = folder.getString("name"),
                            createdAt = folder.getLong("createdAt")
                        )
                    )
                }
            }

            // Import items
            if (data.has("items")) {
                val items = data.getJSONArray("items")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    vaultItemDao.insertItem(
                        VaultItemEntity(
                            id = item.getLong("id"),
                            name = item.getString("name"),
                            type = item.getString("type"),
                            path = item.getString("path"),
                            size = item.getLong("size"),
                            dateAdded = item.getLong("dateAdded"),
                            thumbnailPath = if (item.isNull("thumbnailPath")) null else item.getString("thumbnailPath"),
                            folderId = if (item.getInt("folderId") == 0) null else item.getLong("folderId")
                        )
                    )
                }
            }

            // Import hidden apps
            if (data.has("hiddenApps")) {
                val apps = data.getJSONArray("hiddenApps")
                for (i in 0 until apps.length()) {
                    val app = apps.getJSONObject(i)
                    hiddenAppDao.hideApp(
                        HiddenAppEntity(
                            packageName = app.getString("packageName"),
                            appName = app.getString("appName"),
                            iconPath = if (app.isNull("iconPath")) null else app.getString("iconPath"),
                            dateHidden = app.getLong("dateHidden")
                        )
                    )
                }
            }

            // Import settings
            if (data.has("settings")) {
                val settings = data.getJSONObject("settings")
                if (settings.has("encryption_enabled")) {
                    setEncryptionEnabled(settings.getBoolean("encryption_enabled"))
                }
                if (settings.has("auto_lock_enabled")) {
                    prefs.edit().putBoolean("auto_lock_enabled", settings.getBoolean("auto_lock_enabled")).apply()
                }
                if (settings.has("auto_lock_timeout")) {
                    prefs.edit().putInt("auto_lock_timeout", settings.getInt("auto_lock_timeout")).apply()
                }
                if (settings.has("biometric_enabled")) {
                    prefs.edit().putBoolean("biometric_enabled", settings.getBoolean("biometric_enabled")).apply()
                }
                if (settings.has("intruder_capture")) {
                    prefs.edit().putBoolean("intruder_capture", settings.getBoolean("intruder_capture")).apply()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== File Export/Import for External Sharing ====================

    suspend fun exportBackupToDirectory(destPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = createLocalBackup() ?: return@withContext false
            val sourceFolder = File(backupDir, backup.backupId)
            val destFolder = File(destPath, "GameVault_Backup_${System.currentTimeMillis()}")

            if (destFolder.exists()) destFolder.deleteRecursively()
            destFolder.mkdirs()

            sourceFolder.copyRecursively(destFolder, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackupFromDirectory(sourcePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFolder = File(sourcePath)
            if (!sourceFolder.exists()) return@withContext false

            val manifestFile = File(sourceFolder, "manifest.json")
            if (!manifestFile.exists()) return@withContext false

            val obj = JSONObject(manifestFile.readText())
            val backupId = obj.getString("backupId")

            // Copy to backup directory
            val destFolder = File(backupDir, backupId)
            if (destFolder.exists()) destFolder.deleteRecursively()
            sourceFolder.copyRecursively(destFolder, overwrite = true)

            restoreLocalBackup(backupId)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==================== Google Drive Placeholder ====================
    // Note: Full Google Drive API integration requires:
    // 1. Google Cloud Console project setup
    // 2. OAuth 2.0 consent screen configuration
    // 3. Drive API enabled in the project
    // 4. API credentials (client_secrets.json)
    //
    // To implement Google Drive backup:
    // 1. Go to https://console.cloud.google.com
    // 2. Create a project or select existing
    // 3. Enable "Drive API" from Library
    // 4. Configure OAuth consent screen
    // 5. Create API credentials (OAuth 2.0 Client ID)
    // 6. Download client_secret.json
    //
    // For now, the app exports backups to local files that can be
    // manually synced to Google Drive or any cloud storage.

    fun getGoogleDriveSetupRequired(): Boolean = true

    data class GoogleDriveBackupStatus(
        val configured: Boolean = false,
        val lastSyncTime: Long = 0,
        val autoSyncEnabled: Boolean = false
    )

    fun getGoogleDriveStatus(): GoogleDriveBackupStatus {
        return GoogleDriveBackupStatus(
            configured = prefs.getBoolean("gdrive_configured", false),
            lastSyncTime = prefs.getLong("gdrive_last_sync", 0),
            autoSyncEnabled = prefs.getBoolean("gdrive_auto_sync", false)
        )
    }

    fun setGoogleDriveConfigured(configured: Boolean) {
        prefs.edit().putBoolean("gdrive_configured", configured).apply()
    }

    fun updateLastSyncTime() {
        prefs.edit().putLong("gdrive_last_sync", System.currentTimeMillis()).apply()
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("gdrive_auto_sync", enabled).apply()
    }
}