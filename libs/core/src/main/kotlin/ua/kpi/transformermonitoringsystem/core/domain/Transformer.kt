package ua.kpi.transformermonitoringsystem.core.domain

import jakarta.persistence.*
import ua.kpi.transformermonitoringsystem.shared.core.ITransformer

@Entity
@Table(name = "transformers")
class Transformer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int = 0,

    @Column(nullable = false)
    override var name: String,

    @Column(nullable = false)
    override var location: String,

    @Column(nullable = false)
    override var currentLoad: Double = 0.0,

    @Column(nullable = false)
    override var temperature: Double = 0.0,

    @Column(nullable = false)
    override var maxLoad: Double = 100.0
) : ITransformer
