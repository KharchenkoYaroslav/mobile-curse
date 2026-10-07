package ua.kpi.transformermonitoringsystem.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Головний клас бази даних Room, який об'єднує сутності
@Database(
    entities = [Transformer::class, TransformerStatus::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Метод для отримання об'єкту доступу до даних (DAO)
    abstract fun transformerDao(): TransformerDao
}
