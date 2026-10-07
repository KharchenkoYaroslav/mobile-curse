package ua.kpi.transformermonitoringsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Сутність (таблиця) для зберігання базових даних трансформатора
@Entity(tableName = "transformers")
data class Transformer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Унікальний ідентифікатор (Primary Key)
    val name: String,     // Назва або марка трансформатора
    val location: String  // Фізична локація або координати
)
