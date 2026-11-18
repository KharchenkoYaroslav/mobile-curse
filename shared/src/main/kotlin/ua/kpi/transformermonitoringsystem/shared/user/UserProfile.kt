package ua.kpi.transformermonitoringsystem.shared.user

import java.time.Instant
import ua.kpi.transformermonitoringsystem.shared.auth.Role

interface UserProfile {
    val id: String
    val login: String
    val role: Role
    val createdAt: Instant
}
