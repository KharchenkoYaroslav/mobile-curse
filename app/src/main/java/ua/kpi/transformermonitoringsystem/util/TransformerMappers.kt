package ua.kpi.transformermonitoringsystem.util

import ua.kpi.transformermonitoringsystem.database.entity.Transformer as TransformerEntity
import ua.kpi.transformermonitoringsystem.database.entity.TransformerStatus as TransformerStatusEntity
import ua.kpi.transformermonitoringsystem.data.model.Transformer as TransformerDto
import ua.kpi.transformermonitoringsystem.data.model.TransformerStatus as TransformerStatusDto

// ------------------------------------------------------------------
// Функції перетворення (Mappers) між DTO сервера та Room Entity
// ------------------------------------------------------------------
fun TransformerDto.toEntity() = TransformerEntity(
    id = id,
    name = name,
    location = location,
    currentLoad = currentLoad,
    temperature = temperature,
    maxLoad = maxLoad
)

fun TransformerEntity.toDto() = TransformerDto(
    id = id,
    name = name,
    location = location,
    currentLoad = currentLoad,
    temperature = temperature,
    maxLoad = maxLoad
)

fun TransformerStatusDto.toEntity() = TransformerStatusEntity(
    id = id,
    transformerId = transformerId,
    voltage = voltage,
    temperature = temperature,
    load = load
)

fun TransformerStatusEntity.toDto() = TransformerStatusDto(
    id = id,
    transformerId = transformerId,
    voltage = voltage,
    temperature = temperature,
    load = load
)
