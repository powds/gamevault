package com.gamevault.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class GameEngine_Factory implements Factory<GameEngine> {
  @Override
  public GameEngine get() {
    return newInstance();
  }

  public static GameEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GameEngine newInstance() {
    return new GameEngine();
  }

  private static final class InstanceHolder {
    private static final GameEngine_Factory INSTANCE = new GameEngine_Factory();
  }
}
