package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.auth.LogoutRequest as SharedLogoutRequest

/**
 * Запит виходу з системи.
 */
data class LogoutRequest(
    override val refreshToken: String? = null
) : SharedLogoutRequest
