package ua.kpi.transformermonitoringsystem.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ua.kpi.transformermonitoringsystem.database.entity.Transformer
import ua.kpi.transformermonitoringsystem.database.entity.TransformerStatus

// Data Access Object (DAO) для виконання запитів до БД
@Dao
interface TransformerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransformer(transformer: Transformer): Long

    @Query("DELETE FROM transformers")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transformerEntities: List<Transformer>)

    @Transaction
    suspend fun clearAndInsertAll(transformerEntities: List<Transformer>) {
        clearAll()
        insertAll(transformerEntities)
    }

    @Query("SELECT * FROM transformers ORDER BY name ASC")
    suspend fun getAllTransformers(): List<Transformer>

    @Update
    suspend fun updateTransformer(transformer: Transformer)

    @Delete
    suspend fun deleteTransformer(transformer: Transformer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: TransformerStatus)

    @Delete
    suspend fun deleteStatus(status: TransformerStatus)

    @Query("SELECT * FROM transformer_status WHERE transformerId = :transformerId ORDER BY id DESC")
    suspend fun getStatusesForTransformer(transformerId: Int): List<TransformerStatus>

    @Query("SELECT AVG(temperature) FROM transformer_status WHERE transformerId = :transformerId")
    suspend fun getAverageTemperature(transformerId: Int): Double?

    @Query("SELECT AVG(load) FROM transformer_status WHERE transformerId = :transformerId")
    suspend fun getAverageLoad(transformerId: Int): Double?

    @Transaction
    suspend fun insertTransformerWithStatuses(
        transformer: Transformer,
        statuses: List<TransformerStatus>
    ) {
        val id = insertTransformer(transformer.copy(id = 0)).toInt()
        statuses.forEach { insertStatus(it.copy(id = 0, transformerId = id)) }
    }
}