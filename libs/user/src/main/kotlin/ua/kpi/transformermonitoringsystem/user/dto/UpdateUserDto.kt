package ua.kpi.transformermonitoringsystem.user.dto

import jakarta.validation.constraints.Size
import ua.kpi.transformermonitoringsystem.shared.user.UpdateUser

data class UpdateUserDto(
    override val currentPassword: String? = null,
    override val login: String? = null,
    @field:Size(min = 6, message = "Пароль повинен містити щонайменше 6 символів")
    override val password: String? = null
) : UpdateUser
