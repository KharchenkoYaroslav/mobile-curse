package ua.kpi.transformermonitoringsystem.auth.dto

import jakarta.validation.constraints.NotBlank
import ua.kpi.transformermonitoringsystem.shared.auth.ChangeRoleToAdminRequest

data class ChangeRoleToAdminRequestDto(
    @field:NotBlank(message = "Ключ адміністратора не може бути порожнім")
    override val key: String = ""
) : ChangeRoleToAdminRequest
