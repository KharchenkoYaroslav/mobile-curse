@file:Suppress("unused")

package ua.kpi.transformermonitoringsystem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ua.kpi.transformermonitoringsystem.data.AuthApiService
import ua.kpi.transformermonitoringsystem.data.UserApiService
import ua.kpi.transformermonitoringsystem.data.TransformerApiService
import ua.kpi.transformermonitoringsystem.database.TransformerDao
import ua.kpi.transformermonitoringsystem.database.AuthDataStore
import ua.kpi.transformermonitoringsystem.data.TransformerRepository
import ua.kpi.transformermonitoringsystem.data.TransformerRepositoryImpl
import javax.inject.Singleton

// Провайдер репозиторіїв
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransformerRepository(
        authApi: AuthApiService,
        userApi: UserApiService,
        transformerApi: TransformerApiService,
        dao: TransformerDao,
        authDataStore: AuthDataStore
    ): TransformerRepository {
        return TransformerRepositoryImpl(authApi, userApi, transformerApi, dao, authDataStore)
    }
}
