package ua.kpi.transformermonitoringsystem.data

import ua.kpi.transformermonitoringsystem.data.local.Transformer
import ua.kpi.transformermonitoringsystem.data.local.TransformerDao
import ua.kpi.transformermonitoringsystem.data.local.TransformerStatus

// Репозиторій: Патерн для єдиного джерела даних. Інкапсулює логіку взаємодії з БД.
class TransformerRepository(private val dao: TransformerDao) {

    // Локальний кеш для уникнення зайвих запитів до БД
    private var cachedTransformers: List<Transformer>? = null

    // Збереження трансформатора та його початкових статусів
    suspend fun insertTransformerWithData(
        transformer: Transformer,
        statuses: List<TransformerStatus>
    ) {
        try {
            dao.insertTransformerWithStatuses(transformer, statuses)
            cachedTransformers = null
        } catch (e: Exception) {
            throw Exception("Помилка збереження бази даних: ${e.message}")
        }
    }

    // Отримання списку з підтримкою кешування
    suspend fun getAllTransformers(forceRefresh: Boolean = false): List<Transformer> {
        return try {
            if (forceRefresh || cachedTransformers == null) {
                val data = dao.getAllTransformers()
                cachedTransformers = data
                data
            } else {
                cachedTransformers!!
            }
        } catch (e: Exception) {
            throw Exception("Не вдалося завантажити список трансформаторів: ${e.message}")
        }
    }

    // Оновлення запису
    suspend fun updateTransformer(transformer: Transformer) {
        try {
            dao.updateTransformer(transformer)
            cachedTransformers = null
        } catch (e: Exception) {
            throw Exception("Помилка оновлення трансформатора: ${e.message}")
        }
    }

    // Видалення запису
    suspend fun deleteTransformer(transformer: Transformer) {
        try {
            dao.deleteTransformer(transformer)
            cachedTransformers = null
        } catch (e: Exception) {
            throw Exception("Помилка видалення трансформатора: ${e.message}")
        }
    }

    // Отримання пов'язаних статусів (історія)
    suspend fun getStatuses(transformerId: Int): List<TransformerStatus> {
        return try {
            dao.getStatusesForTransformer(transformerId)
        } catch (e: Exception) {
            throw Exception("Помилка завантаження статусів: ${e.message}")
        }
    }

    // Додавання нового статусу (запису телеметрії)
    suspend fun addStatus(status: TransformerStatus) {
        dao.insertStatus(status)
    }

    // Видалення конкретного статусу
    suspend fun deleteStatus(status: TransformerStatus) {
        dao.deleteStatus(status)
    }

    // Отримання агрегованої середньої температури
    suspend fun getAverageTemperature(transformerId: Int): Double {
        return dao.getAverageTemperature(transformerId) ?: 0.0
    }

    // Отримання агрегованого середнього навантаження
    suspend fun getAverageLoad(transformerId: Int): Double {
        return dao.getAverageLoad(transformerId) ?: 0.0
    }
}
