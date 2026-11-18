package ua.kpi.transformermonitoringsystem.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ua.kpi.transformermonitoringsystem.core.domain.TransformerStatus

@Repository
interface TransformerStatusRepository : JpaRepository<TransformerStatus, Int> {
    fun findByTransformerIdOrderByIdAsc(transformerId: Int): List<TransformerStatus>
    fun deleteByTransformerId(transformerId: Int)
}
