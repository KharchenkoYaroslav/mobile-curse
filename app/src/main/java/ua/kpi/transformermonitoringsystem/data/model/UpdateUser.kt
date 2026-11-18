package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.user.UpdateUser as SharedUpdateUser

/**
 * Оновлення даних профілю користувача.
 */
data class UpdateUser(
    override val currentPassword: String? = null,
    override val login: String? = null,
    override val password: String? = null
) : SharedUpdateUser
