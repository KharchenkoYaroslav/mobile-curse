package ua.kpi.transformermonitoringsystem.auth.dto

import ua.kpi.transformermonitoringsystem.shared.auth.AuthResponse

data class AuthResponseDto(
    override val accessToken: String,
    override val refreshToken: String
) : AuthResponse
