package com.gamevault.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.gamevault.data.local.GameStateDao
import com.gamevault.data.local.GameStateEntity
import com.gamevault.data.local.HiddenAppDao
import com.gamevault.data.local.HiddenAppEntity
import com.gamevault.data.local.VaultItemDao
import com.gamevault.data.local.VaultItemEntity
import com.gamevault.domain.model.HiddenApp
import com.gamevault.domain.model.VaultItem
import com.gamevault.domain.model.VaultItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultItemDao: VaultItemDao,
    private val hiddenAppDao: HiddenAppDao,
    private val gameStateDao: GameStateDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gamevault_prefs", Context.MODE_PRIVATE)

    // ==================== Pattern/PIN ====================
    
    fun isPatternSet(): Boolean = prefs.getBoolean("pattern_set", false)
    
    fun setPattern(pattern: String) {
        prefs.edit().putString("pattern", pattern).putBoolean("pattern_set", true).apply()
    }
    
    fun getPattern(): String = prefs.getString("pattern", "") ?: ""
    
    fun isPinSet(): Boolean = prefs.getBoolean("pin_set", false)
    
    fun setPin(pin: String) {
        prefs.edit().putString("pin", pin).putBoolean("pin_set", true).apply()
    }
    
    fun getPin(): String = prefs.getString("pin", "") ?: ""
    
    fun isDecoyPinSet(): Boolean = prefs.getBoolean("decoy_pin_set", false)
    
    fun setDecoyPin(pin: String) {
        prefs.edit().putString("decoy_pin", pin).putBoolean("decoy_pin_set", true).apply()
    }
    
    fun getDecoyPin(): String = prefs.getString("decoy_pin", "") ?: ""
    
    fun verifyPattern(inputPattern: String): Boolean {
        val saved = prefs.getString("pattern", "") ?: ""
        return saved == inputPattern
    }
    
    fun verifyPin(inputPin: String): Boolean {
        val saved = prefs.getString("pin", "") ?: ""
        return saved == inputPin
    }
    
    fun verifyDecoyPin(inputPin: String): Boolean {
        val saved = prefs.getString("decoy_pin", "") ?: ""
        return saved == inputPin
    }
    
    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", false)
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }
    
    fun clearSecurity() {
        prefs.edit()
            .remove("pattern")
            .remove("pattern_set")
            .remove("pin")
            .remove("pin_set")
            .remove("decoy_pin")
            .remove("decoy_pin_set")
            .apply()
    }

    // ==================== Intruder Capture ====================
    
    fun getIntruderPhotosPath(): String {
        val path = context.getDir("intruder_photos", Context.MODE_PRIVATE).absolutePath
        return path
    }
    
    fun isIntruderCaptureEnabled(): Boolean = prefs.getBoolean("intruder_capture", true)
    
    fun setIntruderCaptureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("intruder_capture", enabled).apply()
    }
    
    fun getFailedAttemptCount(): Int = prefs.getInt("failed_attempts", 0)
    
    fun incrementFailedAttempts(): Int {
        val count = getFailedAttemptCount() + 1
        prefs.edit().putInt("failed_attempts", count).apply()
        return count
    }
    
    fun resetFailedAttempts() {
        prefs.edit().putInt("failed_attempts", 0).apply()
    }

    // ==================== Game State ====================

    fun getBestScore(): Int = prefs.getInt("best_score", 0)
    
    fun saveBestScore(score: Int) {
        if (score > getBestScore()) {
            prefs.edit().putInt("best_score", score).apply()
        }
    }

    suspend fun getGameState(): GameStateEntity? = gameStateDao.getGameState()
    
    suspend fun saveGameState(score: Int, bestScore: Int, grid: List<List<Int>>) {
        val gridJson = JSONArray(grid.map { row ->
            JSONArray(row)
        }).toString()
        
        saveBestScore(bestScore)
        
        gameStateDao.saveGameState(
            GameStateEntity(
                id = 1,
                score = score,
                bestScore = bestScore,
                gridJson = gridJson
            )
        )
    }

    // ==================== Vault Items ====================

    fun getVaultItems(): Flow<List<VaultItem>> {
        return vaultItemDao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addVaultItem(item: VaultItem) {
        vaultItemDao.insertItem(item.toEntity())
    }

    suspend fun deleteVaultItem(id: Long) {
        vaultItemDao.deleteById(id)
    }

    suspend fun getVaultItem(id: Long): VaultItem? {
        return vaultItemDao.getItemById(id)?.toDomain()
    }

    suspend fun deleteAllVaultItems() {
        // Get all items and delete them one by one
        val items = vaultItemDao.getAllItems()
        items.collect { itemList ->
            itemList.forEach { item ->
                vaultItemDao.deleteItem(item)
            }
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

    // ==================== Decoy Content ====================
    
    fun getDecoyItemCount(): Int = prefs.getInt("decoy_item_count", 0)
    
    fun setDecoyItemCount(count: Int) {
        prefs.edit().putInt("decoy_item_count", count).apply()
    }

    // ==================== Mappers ====================

    private fun VaultItemEntity.toDomain(): VaultItem {
        return VaultItem(
            id = id,
            name = name,
            type = VaultItemType.valueOf(type),
            path = path,
            size = size,
            dateAdded = dateAdded,
            thumbnailPath = thumbnailPath
        )
    }

    private fun VaultItem.toEntity(): VaultItemEntity {
        return VaultItemEntity(
            id = id,
            name = name,
            type = type.name,
            path = path,
            size = size,
            dateAdded = dateAdded,
            thumbnailPath = thumbnailPath
        )
    }

    private fun HiddenAppEntity.toDomain(): HiddenApp {
        return HiddenApp(
            packageName = packageName,
            appName = appName,
            iconPath = iconPath,
            dateHidden = dateHidden
        )
    }

    private fun HiddenApp.toEntity(): HiddenAppEntity {
        return HiddenAppEntity(
            packageName = packageName,
            appName = appName,
            iconPath = iconPath,
            dateHidden = dateHidden
        )
    }
}