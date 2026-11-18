package ua.kpi.transformermonitoringsystem.core.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import ua.kpi.transformermonitoringsystem.core.domain.Transformer
import ua.kpi.transformermonitoringsystem.shared.core.ITransformer

data class TransformerDto(
    override val id: Int = 0,

    @field:NotBlank(message = "Назва не може бути порожньою")
    override val name: String,

    @field:NotBlank(message = "Локація не може бути порожньою")
    override val location: String,

    override val currentLoad: Double = 0.0,
    override val temperature: Double = 0.0,

    @field:Positive(message = "Максимальне навантаження має бути більше 0")
    override val maxLoad: Double = 100.0
) : ITransformer {
    companion object {
        fun from(entity: Transformer): TransformerDto {
            return TransformerDto(
                id = entity.id,
                name = entity.name,
                location = entity.location,
                currentLoad = entity.currentLoad,
                temperature = entity.temperature,
                maxLoad = entity.maxLoad
            )
        }
    }

    fun toEntity(): Transformer {
        return Transformer(
            id = id,
            name = name,
            location = location,
            currentLoad = currentLoad,
            temperature = temperature,
            maxLoad = maxLoad
        )
    }
}
