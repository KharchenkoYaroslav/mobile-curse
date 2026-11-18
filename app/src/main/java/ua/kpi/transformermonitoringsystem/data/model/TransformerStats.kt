package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.core.ITransformerStats

/**
 * Агрегована статистика мережі.
 */
data class TransformerStats(
    override val totalPower: Double = 0.0,
    override val criticalIssues: Int = 0,
    override val averageLoad: Double = 0.0,
    override val averageTemperature: Double = 0.0,
    override val totalCount: Int = 0
) : ITransformerStats
