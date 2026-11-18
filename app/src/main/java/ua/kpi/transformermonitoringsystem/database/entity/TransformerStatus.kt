package ua.kpi.transformermonitoringsystem.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Сутність для збереження історії статусів
@Entity(
    tableName = "transformer_status",
    foreignKeys = [ForeignKey(
        entity = Transformer::class,
        parentColumns = ["id"],
        childColumns = ["transformerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("transformerId")]
)
data class TransformerStatus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transformerId: Int,
    val voltage: Double,
    val temperature: Double,
    val load: Double
)
