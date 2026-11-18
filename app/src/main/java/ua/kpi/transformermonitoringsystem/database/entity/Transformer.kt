package ua.kpi.transformermonitoringsystem.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Сутність (таблиця) для зберігання базових даних трансформатора
@Entity(tableName = "transformers")
data class Transformer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String,
    val currentLoad: Double = 0.0,
    val temperature: Double = 0.0,
    val maxLoad: Double = 100.0
)