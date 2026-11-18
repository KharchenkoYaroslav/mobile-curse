package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.auth.RefreshRequest as SharedRefreshRequest

/**
 * Запит оновлення токена.
 */
data class RefreshRequest(
    override val refreshToken: String? = null
) : SharedRefreshRequest
