package com.gamevault.di;

import com.gamevault.data.local.GameVaultDatabase;
import com.gamevault.data.local.VaultItemDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class AppModule_ProvideVaultItemDaoFactory implements Factory<VaultItemDao> {
  private final Provider<GameVaultDatabase> databaseProvider;

  public AppModule_ProvideVaultItemDaoFactory(Provider<GameVaultDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public VaultItemDao get() {
    return provideVaultItemDao(databaseProvider.get());
  }

  public static AppModule_ProvideVaultItemDaoFactory create(
      Provider<GameVaultDatabase> databaseProvider) {
    return new AppModule_ProvideVaultItemDaoFactory(databaseProvider);
  }

  public static VaultItemDao provideVaultItemDao(GameVaultDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideVaultItemDao(database));
  }
}
