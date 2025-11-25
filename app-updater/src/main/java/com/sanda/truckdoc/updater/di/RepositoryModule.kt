package com.sanda.truckdoc.updater.di

import com.sanda.truckdoc.updater.data.repository.GitHubUpdateRepository
import com.sanda.truckdoc.updater.data.repository.UpdateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUpdateProvider(
        gitHubUpdateRepository: GitHubUpdateRepository
    ): UpdateProvider
}

