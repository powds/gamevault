package com.gamevault.presentation.vault;

import android.content.Context;
import com.gamevault.data.repository.VaultRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class VaultViewModel_Factory implements Factory<VaultViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<VaultRepository> repositoryProvider;

  public VaultViewModel_Factory(Provider<Context> contextProvider,
      Provider<VaultRepository> repositoryProvider) {
    this.contextProvider = contextProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public VaultViewModel get() {
    return newInstance(contextProvider.get(), repositoryProvider.get());
  }

  public static VaultViewModel_Factory create(Provider<Context> contextProvider,
      Provider<VaultRepository> repositoryProvider) {
    return new VaultViewModel_Factory(contextProvider, repositoryProvider);
  }

  public static VaultViewModel newInstance(Context context, VaultRepository repository) {
    return new VaultViewModel(context, repository);
  }
}
