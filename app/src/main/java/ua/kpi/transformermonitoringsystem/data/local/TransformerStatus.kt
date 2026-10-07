package ua.kpi.transformermonitoringsystem.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Сутність (таблиця) для збереження історії показників (статусів)
@Entity(
    tableName = "transformer_status",
    foreignKeys = [ForeignKey(
        entity = Transformer::class,           // Зв'язок із таблицею transformers
        parentColumns = ["id"],
        childColumns = ["transformerId"],
        onDelete = ForeignKey.CASCADE          // Каскадне видалення: видаляємо трансформатор -> видаляються статуси
    )],
    indices = [Index("transformerId")]         // Індекс для пришвидшення пошуку по ID
)
data class TransformerStatus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Унікальний ідентифікатор запису
    val transformerId: Int,  // Зовнішній ключ (Foreign Key)
    val voltage: Double,     // Показник напруги
    val temperature: Double, // Показник температури
    val load: Double         // Показник навантаження
)
