package ua.kpi.transformermonitoringsystem.data.model

import ua.kpi.transformermonitoringsystem.shared.auth.ChangeRoleToAdminRequest as SharedChangeRoleToAdminRequest

/**
 * Запит отримання ролі адміністратора за секретним ключем.
 */
data class ChangeRoleToAdminRequest(
    override val key: String = ""
) : SharedChangeRoleToAdminRequest
