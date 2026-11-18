@file:Suppress("unused")

package ua.kpi.transformermonitoringsystem.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.kpi.transformermonitoringsystem.database.AuthDataStore
import javax.inject.Singleton

// Провайдер загальних системних компонентів
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): AuthDataStore {
        return AuthDataStore(context)
    }
}
