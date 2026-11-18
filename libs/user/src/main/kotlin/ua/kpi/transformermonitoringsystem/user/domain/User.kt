package ua.kpi.transformermonitoringsystem.user.domain

import jakarta.persistence.*
import ua.kpi.transformermonitoringsystem.shared.auth.Role
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var login: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_refresh_tokens",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "token_hash", nullable = false)
    var refreshTokens: MutableList<String> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
