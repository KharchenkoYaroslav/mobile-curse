package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.auth.AuthResponse as SharedAuthResponse

/**
 * Відповідь сервера авторизації.
 */
data class AuthResponse(
    override val accessToken: String,
    override val refreshToken: String
) : SharedAuthResponse
