package ua.kpi.transformermonitoringsystem.core.domain

import jakarta.persistence.*
import ua.kpi.transformermonitoringsystem.shared.core.ITransformerStatus

@Entity
@Table(
    name = "transformer_statuses",
    indexes = [Index(name = "idx_status_transformer_id", columnList = "transformer_id")]
)
class TransformerStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @Column(name = "transformer_id", nullable = false)
    override var transformerId: Int,

    @Column(nullable = false)
    override var voltage: Double,

    @Column(nullable = false)
    override var temperature: Double,

    @Column(name = "load_value", nullable = false)
    override var load: Double
) : ITransformerStatus
