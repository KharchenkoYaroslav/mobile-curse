package ua.kpi.transformermonitoringsystem.core.dto

import jakarta.validation.constraints.Positive
import ua.kpi.transformermonitoringsystem.core.domain.TransformerStatus
import ua.kpi.transformermonitoringsystem.shared.core.ITransformerStatus

data class TransformerStatusDto(
    override val id: Int = 0,
    override val transformerId: Int,

    @field:Positive(message = "Напруга повинна бути більшою за 0")
    override val voltage: Double,

    override val temperature: Double,
    override val load: Double
) : ITransformerStatus {
    companion object {
        fun from(entity: TransformerStatus): TransformerStatusDto {
            return TransformerStatusDto(
                id = entity.id,
                transformerId = entity.transformerId,
                voltage = entity.voltage,
                temperature = entity.temperature,
                load = entity.load
            )
        }
    }

    fun toEntity(): TransformerStatus {
        return TransformerStatus(
            id = id,
            transformerId = transformerId,
            voltage = voltage,
            temperature = temperature,
            load = load
        )
    }
}
