@file:Suppress("unused")

package ua.kpi.transformermonitoringsystem.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.kpi.transformermonitoringsystem.database.AppDatabase
import ua.kpi.transformermonitoringsystem.database.TransformerDao
import javax.inject.Singleton

// Провайдер БД та її DAO
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "transformer-db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideTransformerDao(database: AppDatabase): TransformerDao {
        return database.transformerDao()
    }
}
