package com.gamevault.data.repository;

import android.content.Context;
import com.gamevault.data.local.GameStateDao;
import com.gamevault.data.local.HiddenAppDao;
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
public final class VaultRepository_Factory implements Factory<VaultRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<VaultItemDao> vaultItemDaoProvider;

  private final Provider<HiddenAppDao> hiddenAppDaoProvider;

  private final Provider<GameStateDao> gameStateDaoProvider;

  public VaultRepository_Factory(Provider<Context> contextProvider,
      Provider<VaultItemDao> vaultItemDaoProvider, Provider<HiddenAppDao> hiddenAppDaoProvider,
      Provider<GameStateDao> gameStateDaoProvider) {
    this.contextProvider = contextProvider;
    this.vaultItemDaoProvider = vaultItemDaoProvider;
    this.hiddenAppDaoProvider = hiddenAppDaoProvider;
    this.gameStateDaoProvider = gameStateDaoProvider;
  }

  @Override
  public VaultRepository get() {
    return newInstance(contextProvider.get(), vaultItemDaoProvider.get(), hiddenAppDaoProvider.get(), gameStateDaoProvider.get());
  }

  public static VaultRepository_Factory create(Provider<Context> contextProvider,
      Provider<VaultItemDao> vaultItemDaoProvider, Provider<HiddenAppDao> hiddenAppDaoProvider,
      Provider<GameStateDao> gameStateDaoProvider) {
    return new VaultRepository_Factory(contextProvider, vaultItemDaoProvider, hiddenAppDaoProvider, gameStateDaoProvider);
  }

  public static VaultRepository newInstance(Context context, VaultItemDao vaultItemDao,
      HiddenAppDao hiddenAppDao, GameStateDao gameStateDao) {
    return new VaultRepository(context, vaultItemDao, hiddenAppDao, gameStateDao);
  }
}
