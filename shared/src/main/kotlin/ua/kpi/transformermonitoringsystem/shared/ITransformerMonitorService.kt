package ua.kpi.transformermonitoringsystem.shared

/**
 * Загальний контракт для моніторингу трансформаторів.
 * Модуль :shared містить виключно абстракції/інтерфейси без прив'язки до серверних DTO.
 */
interface ITransformerMonitorService {
    fun getHealthStatus(): String
}
