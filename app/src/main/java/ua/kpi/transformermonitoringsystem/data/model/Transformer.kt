package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.core.ITransformer

/**
 * Трансформатор з сервера.
 */
data class Transformer(
    override val id: Int = 0,
    override val name: String,
    override val location: String,
    override val currentLoad: Double = 0.0,
    override val temperature: Double = 0.0,
    override val maxLoad: Double = 100.0
) : ITransformer
