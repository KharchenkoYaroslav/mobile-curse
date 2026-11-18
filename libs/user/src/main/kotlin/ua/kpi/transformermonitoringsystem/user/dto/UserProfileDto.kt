package ua.kpi.transformermonitoringsystem.user.dto

import java.time.Instant
import ua.kpi.transformermonitoringsystem.shared.auth.Role
import ua.kpi.transformermonitoringsystem.shared.user.UserProfile
import ua.kpi.transformermonitoringsystem.user.domain.User

data class UserProfileDto(
    override val id: String,
    override val login: String,
    override val role: Role,
    override val createdAt: Instant
) : UserProfile {
    companion object {
        fun from(user: User): UserProfileDto {
            return UserProfileDto(
                id = user.id.toString(),
                login = user.login,
                role = user.role,
                createdAt = user.createdAt
            )
        }
    }
}
