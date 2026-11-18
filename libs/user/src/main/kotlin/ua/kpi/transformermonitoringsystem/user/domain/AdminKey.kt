package ua.kpi.transformermonitoringsystem.user.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "admin_keys")
class AdminKey(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "secret_key", nullable = false, unique = true)
    var key: String,

    @Column(nullable = false)
    var isReserved: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
