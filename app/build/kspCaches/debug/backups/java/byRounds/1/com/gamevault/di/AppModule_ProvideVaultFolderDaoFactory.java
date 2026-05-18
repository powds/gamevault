package com.gamevault.di;

import com.gamevault.data.local.GameVaultDatabase;
import com.gamevault.data.local.VaultFolderDao;
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
public final class AppModule_ProvideVaultFolderDaoFactory implements Factory<VaultFolderDao> {
  private final Provider<GameVaultDatabase> databaseProvider;

  public AppModule_ProvideVaultFolderDaoFactory(Provider<GameVaultDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public VaultFolderDao get() {
    return provideVaultFolderDao(databaseProvider.get());
  }

  public static AppModule_ProvideVaultFolderDaoFactory create(
      Provider<GameVaultDatabase> databaseProvider) {
    return new AppModule_ProvideVaultFolderDaoFactory(databaseProvider);
  }

  public static VaultFolderDao provideVaultFolderDao(GameVaultDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideVaultFolderDao(database));
  }
}
