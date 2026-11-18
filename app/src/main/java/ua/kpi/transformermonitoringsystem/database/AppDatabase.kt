package ua.kpi.transformermonitoringsystem.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ua.kpi.transformermonitoringsystem.database.entity.Transformer
import ua.kpi.transformermonitoringsystem.database.entity.TransformerStatus

// Головний клас бази даних Room, який об'єднує сутності
@Database(
    entities = [Transformer::class, TransformerStatus::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transformerDao(): TransformerDao
}
