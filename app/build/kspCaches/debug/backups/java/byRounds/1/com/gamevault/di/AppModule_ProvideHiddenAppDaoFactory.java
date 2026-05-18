package com.gamevault.di;

import com.gamevault.data.local.GameVaultDatabase;
import com.gamevault.data.local.HiddenAppDao;
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
public final class AppModule_ProvideHiddenAppDaoFactory implements Factory<HiddenAppDao> {
  private final Provider<GameVaultDatabase> databaseProvider;

  public AppModule_ProvideHiddenAppDaoFactory(Provider<GameVaultDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public HiddenAppDao get() {
    return provideHiddenAppDao(databaseProvider.get());
  }

  public static AppModule_ProvideHiddenAppDaoFactory create(
      Provider<GameVaultDatabase> databaseProvider) {
    return new AppModule_ProvideHiddenAppDaoFactory(databaseProvider);
  }

  public static HiddenAppDao provideHiddenAppDao(GameVaultDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideHiddenAppDao(database));
  }
}
