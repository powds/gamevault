package com.gamevault.data.repository;

import android.content.Context;
import com.gamevault.data.local.GameStateDao;
import com.gamevault.data.local.HiddenAppDao;
import com.gamevault.data.local.VaultFolderDao;
import com.gamevault.data.local.VaultItemDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class BackupRepository_Factory implements Factory<BackupRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<VaultFolderDao> vaultFolderDaoProvider;

  private final Provider<VaultItemDao> vaultItemDaoProvider;

  private final Provider<HiddenAppDao> hiddenAppDaoProvider;

  private final Provider<GameStateDao> gameStateDaoProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  public BackupRepository_Factory(Provider<Context> contextProvider,
      Provider<VaultFolderDao> vaultFolderDaoProvider, Provider<VaultItemDao> vaultItemDaoProvider,
      Provider<HiddenAppDao> hiddenAppDaoProvider, Provider<GameStateDao> gameStateDaoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    this.contextProvider = contextProvider;
    this.vaultFolderDaoProvider = vaultFolderDaoProvider;
    this.vaultItemDaoProvider = vaultItemDaoProvider;
    this.hiddenAppDaoProvider = hiddenAppDaoProvider;
    this.gameStateDaoProvider = gameStateDaoProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public BackupRepository get() {
    return newInstance(contextProvider.get(), vaultFolderDaoProvider.get(), vaultItemDaoProvider.get(), hiddenAppDaoProvider.get(), gameStateDaoProvider.get(), cryptoManagerProvider.get());
  }

  public static BackupRepository_Factory create(Provider<Context> contextProvider,
      Provider<VaultFolderDao> vaultFolderDaoProvider, Provider<VaultItemDao> vaultItemDaoProvider,
      Provider<HiddenAppDao> hiddenAppDaoProvider, Provider<GameStateDao> gameStateDaoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    return new BackupRepository_Factory(contextProvider, vaultFolderDaoProvider, vaultItemDaoProvider, hiddenAppDaoProvider, gameStateDaoProvider, cryptoManagerProvider);
  }

  public static BackupRepository newInstance(Context context, VaultFolderDao vaultFolderDao,
      VaultItemDao vaultItemDao, HiddenAppDao hiddenAppDao, GameStateDao gameStateDao,
      CryptoManager cryptoManager) {
    return new BackupRepository(context, vaultFolderDao, vaultItemDao, hiddenAppDao, gameStateDao, cryptoManager);
  }
}
