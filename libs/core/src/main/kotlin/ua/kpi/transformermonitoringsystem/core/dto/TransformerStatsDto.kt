package ua.kpi.transformermonitoringsystem.core.dto

import ua.kpi.transformermonitoringsystem.shared.core.ITransformerStats

data class TransformerStatsDto(
    override val totalPower: Double,
    override val criticalIssues: Int,
    override val averageLoad: Double,
    override val averageTemperature: Double,
    override val totalCount: Int
) : ITransformerStats
