package ua.kpi.transformermonitoringsystem.auth.dto

import ua.kpi.transformermonitoringsystem.shared.auth.AuthUser
import ua.kpi.transformermonitoringsystem.shared.auth.RefreshUser

data class AuthUserDto(
    override val sub: String,
    override val refreshToken: String? = null
) : AuthUser, RefreshUser
