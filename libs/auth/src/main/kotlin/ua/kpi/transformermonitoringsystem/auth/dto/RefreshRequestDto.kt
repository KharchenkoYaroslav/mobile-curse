package ua.kpi.transformermonitoringsystem.auth.dto

import ua.kpi.transformermonitoringsystem.shared.auth.RefreshRequest

data class RefreshRequestDto(
    override val refreshToken: String? = null
) : RefreshRequest
