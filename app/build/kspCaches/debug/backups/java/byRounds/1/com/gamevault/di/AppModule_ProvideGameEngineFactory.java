package com.gamevault.di;

import com.gamevault.domain.usecase.GameEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideGameEngineFactory implements Factory<GameEngine> {
  @Override
  public GameEngine get() {
    return provideGameEngine();
  }

  public static AppModule_ProvideGameEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GameEngine provideGameEngine() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGameEngine());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideGameEngineFactory INSTANCE = new AppModule_ProvideGameEngineFactory();
  }
}
