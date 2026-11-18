@file:Suppress("unused")

package ua.kpi.transformermonitoringsystem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ua.kpi.transformermonitoringsystem.data.AuthApiService
import ua.kpi.transformermonitoringsystem.data.TransformerApiService
import ua.kpi.transformermonitoringsystem.data.UserApiService
import javax.inject.Singleton

// Провайдер API сервісів додатку
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideTransformerApiService(retrofit: Retrofit): TransformerApiService {
        return retrofit.create(TransformerApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
}
