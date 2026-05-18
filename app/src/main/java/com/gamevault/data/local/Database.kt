package com.gamevault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vault_folders")
data class VaultFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val thumbnailPath: String?,
    val folderId: Long? = null
)

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val iconPath: String?,
    val dateHidden: Long
)

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val score: Int,
    val bestScore: Int,
    val gridJson: String
)

@Dao
interface VaultFolderDao {
    @Query("SELECT * FROM vault_folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<VaultFolderEntity>>

    @Query("SELECT * FROM vault_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): VaultFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VaultFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: VaultFolderEntity)

    @Delete
    suspend fun deleteFolder(folder: VaultFolderEntity)

    @Query("DELETE FROM vault_folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("SELECT COUNT(*) FROM vault_items WHERE folderId = :folderId")
    suspend fun getItemCountInFolder(folderId: Long): Int
}

@Dao
interface VaultItemDao {
    @Query("SELECT * FROM vault_items ORDER BY dateAdded DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE folderId IS NULL ORDER BY dateAdded DESC")
    fun getRootItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE folderId = :folderId ORDER BY dateAdded DESC")
    fun getItemsInFolder(folderId: Long): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    @Update
    suspend fun updateItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM vault_items WHERE name LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun searchItems(query: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE type = :type ORDER BY dateAdded DESC")
    fun getItemsByType(type: String): Flow<List<VaultItemEntity>>
}

@Dao
interface HiddenAppDao {
    @Query("SELECT * FROM hidden_apps")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Query("SELECT * FROM hidden_apps WHERE packageName = :packageName")
    suspend fun getHiddenApp(packageName: String): HiddenAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideApp(app: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun unhideApp(packageName: String)
}

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameState(): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)
}

@Database(
    entities = [
        VaultFolderEntity::class,
        VaultItemEntity::class,
        HiddenAppEntity::class,
        GameStateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract fun vaultFolderDao(): VaultFolderDao
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun gameStateDao(): GameStateDao
}