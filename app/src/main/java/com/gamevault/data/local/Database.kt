package com.gamevault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val thumbnailPath: String?
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
interface VaultItemDao {
    @Query("SELECT * FROM vault_items ORDER BY dateAdded DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: Long)
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
    entities = [VaultItemEntity::class, HiddenAppEntity::class, GameStateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun gameStateDao(): GameStateDao
}