package com.gamevault.di;

import com.gamevault.data.local.GameStateDao;
import com.gamevault.data.local.GameVaultDatabase;
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
public final class AppModule_ProvideGameStateDaoFactory implements Factory<GameStateDao> {
  private final Provider<GameVaultDatabase> databaseProvider;

  public AppModule_ProvideGameStateDaoFactory(Provider<GameVaultDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public GameStateDao get() {
    return provideGameStateDao(databaseProvider.get());
  }

  public static AppModule_ProvideGameStateDaoFactory create(
      Provider<GameVaultDatabase> databaseProvider) {
    return new AppModule_ProvideGameStateDaoFactory(databaseProvider);
  }

  public static GameStateDao provideGameStateDao(GameVaultDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGameStateDao(database));
  }
}
