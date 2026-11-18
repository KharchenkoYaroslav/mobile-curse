package ua.kpi.transformermonitoringsystem.auth.dto

import ua.kpi.transformermonitoringsystem.shared.auth.LogoutRequest

data class LogoutRequestDto(
    override val refreshToken: String? = null
) : LogoutRequest
