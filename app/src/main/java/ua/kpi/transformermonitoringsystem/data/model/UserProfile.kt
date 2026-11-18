package ua.kpi.transformermonitoringsystem.data.model

import java.time.Instant
import ua.kpi.transformermonitoringsystem.shared.auth.Role
import ua.kpi.transformermonitoringsystem.shared.user.UserProfile as SharedUserProfile

/**
 * Профіль користувача з сервера.
 */
data class UserProfile(
    override val id: String,
    override val login: String,
    override val role: Role,
    override val createdAt: Instant
) : SharedUserProfile
