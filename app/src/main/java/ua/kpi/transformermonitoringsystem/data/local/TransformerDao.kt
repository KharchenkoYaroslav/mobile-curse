package ua.kpi.transformermonitoringsystem.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

// Data Access Object (DAO) для виконання запитів до БД
@Dao
interface TransformerDao {
    // Додає трансформатор у БД. Повертає ID нового запису.
    @Insert
    suspend fun insertTransformer(transformer: Transformer): Long

    // Повертає весь список трансформаторів, посортований за назвою
    @Query("SELECT * FROM transformers ORDER BY name ASC")
    suspend fun getAllTransformers(): List<Transformer>

    // Оновлює дані трансформатора
    @Update
    suspend fun updateTransformer(transformer: Transformer)

    // Видаляє запис з БД
    @Delete
    suspend fun deleteTransformer(transformer: Transformer)

    // Вставка одного статусу
    @Insert
    suspend fun insertStatus(status: TransformerStatus)

    // Видалення статусу
    @Delete
    suspend fun deleteStatus(status: TransformerStatus)

    // Отримання історії статусів для конкретного трансформатора (від нових до старих)
    @Query("SELECT * FROM transformer_status WHERE transformerId = :transformerId ORDER BY id DESC")
    suspend fun getStatusesForTransformer(transformerId: Int): List<TransformerStatus>

    // SQL-запит з агрегатною функцією AVG для обчислення середньої температури
    @Query("SELECT AVG(temperature) FROM transformer_status WHERE transformerId = :transformerId")
    suspend fun getAverageTemperature(transformerId: Int): Double?

    // SQL-запит з агрегатною функцією AVG для обчислення середнього навантаження
    @Query("SELECT AVG(load) FROM transformer_status WHERE transformerId = :transformerId")
    suspend fun getAverageLoad(transformerId: Int): Double?

    // Виконання комплексної транзакції: вставка трансформатора і всіх його початкових статусів
    @Transaction
    suspend fun insertTransformerWithStatuses(
        transformer: Transformer,
        statuses: List<TransformerStatus>
    ) {
        val id = insertTransformer(transformer).toInt()
        statuses.forEach { insertStatus(it.copy(transformerId = id)) }
    }
}
