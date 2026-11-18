package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.auth.AuthCredentials as SharedAuthCredentials

/**
 * Дані авторизації.
 */
data class AuthCredentials(
    override val login: String = "",
    override val password: String = ""
) : SharedAuthCredentials
