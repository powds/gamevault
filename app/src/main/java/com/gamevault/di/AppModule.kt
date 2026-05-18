package com.gamevault.di

import android.content.Context
import androidx.room.Room
import com.gamevault.data.local.*
import com.gamevault.domain.usecase.GameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine = GameEngine()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameVaultDatabase {
        return Room.databaseBuilder(
            context,
            GameVaultDatabase::class.java,
            "gamevault.db"
        ).build()
    }

    @Provides
    fun provideVaultFolderDao(database: GameVaultDatabase): VaultFolderDao = database.vaultFolderDao()

    @Provides
    fun provideVaultItemDao(database: GameVaultDatabase): VaultItemDao = database.vaultItemDao()

    @Provides
    fun provideHiddenAppDao(database: GameVaultDatabase): HiddenAppDao = database.hiddenAppDao()

    @Provides
    fun provideGameStateDao(database: GameVaultDatabase): GameStateDao = database.gameStateDao()
}