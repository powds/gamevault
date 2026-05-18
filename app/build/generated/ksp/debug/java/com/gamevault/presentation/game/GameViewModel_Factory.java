package com.gamevault.presentation.game;

import com.gamevault.data.repository.VaultRepository;
import com.gamevault.domain.usecase.GameEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GameViewModel_Factory implements Factory<GameViewModel> {
  private final Provider<GameEngine> gameEngineProvider;

  private final Provider<VaultRepository> vaultRepositoryProvider;

  public GameViewModel_Factory(Provider<GameEngine> gameEngineProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    this.gameEngineProvider = gameEngineProvider;
    this.vaultRepositoryProvider = vaultRepositoryProvider;
  }

  @Override
  public GameViewModel get() {
    return newInstance(gameEngineProvider.get(), vaultRepositoryProvider.get());
  }

  public static GameViewModel_Factory create(Provider<GameEngine> gameEngineProvider,
      Provider<VaultRepository> vaultRepositoryProvider) {
    return new GameViewModel_Factory(gameEngineProvider, vaultRepositoryProvider);
  }

  public static GameViewModel newInstance(GameEngine gameEngine, VaultRepository vaultRepository) {
    return new GameViewModel(gameEngine, vaultRepository);
  }
}
