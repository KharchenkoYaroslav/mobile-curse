package ua.kpi.transformermonitoringsystem.core.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.core.dto.TransformerDto
import ua.kpi.transformermonitoringsystem.core.dto.TransformerStatsDto
import ua.kpi.transformermonitoringsystem.core.dto.TransformerStatusDto
import ua.kpi.transformermonitoringsystem.core.repository.TransformerRepository
import ua.kpi.transformermonitoringsystem.core.repository.TransformerStatusRepository

@Service
class TransformerService(
    private val transformerRepository: TransformerRepository,
    private val statusRepository: TransformerStatusRepository
) {

    fun getAllTransformers(): List<TransformerDto> {
        return transformerRepository.findAll().map { TransformerDto.from(it) }
    }

    fun getTransformerById(id: Int): TransformerDto {
        val entity = transformerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Трансформатор з id=$id не знайдено") }
        return TransformerDto.from(entity)
    }

    @Transactional
    fun addTransformer(dto: TransformerDto): TransformerDto {
        val entity = dto.toEntity().apply { id = 0 }
        val saved = transformerRepository.save(entity)
        return TransformerDto.from(saved)
    }

    @Transactional
    fun updateTransformer(id: Int, dto: TransformerDto): TransformerDto {
        val existing = transformerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Трансформатор з id=$id не знайдено") }

        existing.name = dto.name
        existing.location = dto.location
        existing.currentLoad = dto.currentLoad
        existing.temperature = dto.temperature
        existing.maxLoad = dto.maxLoad

        val updated = transformerRepository.save(existing)
        return TransformerDto.from(updated)
    }

    @Transactional
    fun deleteTransformer(id: Int) {
        if (!transformerRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Трансформатор з id=$id не знайдено")
        }
        // Каскадно видаляємо історію статусів
        statusRepository.deleteByTransformerId(id)
        transformerRepository.deleteById(id)
    }

    // Об'єднана статистика
    fun getNetworkStats(): TransformerStatsDto {
        val all = transformerRepository.findAll()
        if (all.isEmpty()) {
            return TransformerStatsDto(
                totalPower = 0.0,
                criticalIssues = 0,
                averageLoad = 0.0,
                averageTemperature = 0.0,
                totalCount = 0
            )
        }

        val totalPower = all.sumOf { it.currentLoad }
        val criticalIssues = all.count { (it.temperature > 85.0) || (it.currentLoad > (it.maxLoad * 0.9)) }
        val avgLoad = all.map { it.currentLoad }.average()
        val avgTemp = all.map { it.temperature }.average()

        return TransformerStatsDto(
            totalPower = totalPower,
            criticalIssues = criticalIssues,
            averageLoad = avgLoad,
            averageTemperature = avgTemp,
            totalCount = all.size
        )
    }

    // Робота з історією статусів
    fun getStatusesByTransformerId(transformerId: Int): List<TransformerStatusDto> {
        if (!transformerRepository.existsById(transformerId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Трансформатор з id=$transformerId не знайдено")
        }
        return statusRepository.findByTransformerIdOrderByIdAsc(transformerId).map { TransformerStatusDto.from(it) }
    }

    @Transactional
    fun addStatus(transformerId: Int, dto: TransformerStatusDto): TransformerStatusDto {
        val transformer = transformerRepository.findById(transformerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Трансформатор з id=$transformerId не знайдено") }

        val statusEntity = dto.toEntity().apply {
            this.id = 0
            this.transformerId = transformerId
        }
        val saved = statusRepository.save(statusEntity)

        // Оновлюємо актуальний стан трансформатора останніми показниками зі статусу
        transformer.currentLoad = dto.load
        transformer.temperature = dto.temperature
        transformerRepository.save(transformer)

        return TransformerStatusDto.from(saved)
    }

    @Transactional
    fun deleteStatus(statusId: Int) {
        if (!statusRepository.existsById(statusId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Статус з id=$statusId не знайдено")
        }
        statusRepository.deleteById(statusId)
    }
}
