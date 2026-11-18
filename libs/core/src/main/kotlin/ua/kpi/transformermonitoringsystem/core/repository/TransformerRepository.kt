package ua.kpi.transformermonitoringsystem.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ua.kpi.transformermonitoringsystem.core.domain.Transformer

@Repository
interface TransformerRepository : JpaRepository<Transformer, Int>
