package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.core.ITransformerStatus

/**
 * Статус/телеметрія трансформатора з сервера.
 */
data class TransformerStatus(
    override val id: Int = 0,
    override val transformerId: Int = 0,
    override val voltage: Double = 0.0,
    override val temperature: Double = 0.0,
    override val load: Double = 0.0
) : ITransformerStatus
