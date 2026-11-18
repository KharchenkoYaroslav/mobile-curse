package ua.kpi.transformermonitoringsystem.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import ua.kpi.transformermonitoringsystem.shared.auth.AuthCredentials

data class AuthCredentialsDto(
    @field:NotBlank(message = "Логін не може бути порожнім")
    override val login: String = "",

    @field:NotBlank(message = "Пароль не може бути порожнім")
    @field:Size(min = 6, message = "Пароль повинен містити щонайменше 6 символів")
    override val password: String = ""
) : AuthCredentials
