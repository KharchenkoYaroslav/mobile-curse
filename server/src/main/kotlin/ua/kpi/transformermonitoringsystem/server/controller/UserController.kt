package ua.kpi.transformermonitoringsystem.server.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.user.dto.UpdateUserDto
import ua.kpi.transformermonitoringsystem.user.dto.UserProfileDto
import ua.kpi.transformermonitoringsystem.user.service.UserService

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    fun getCurrentUserProfile(@AuthenticationPrincipal principal: Any?): UserProfileDto {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")
        return userService.getProfileById(userId)
    }

    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    fun updateProfile(
        @AuthenticationPrincipal principal: Any?,
        @Valid @RequestBody dto: UpdateUserDto
    ): UserProfileDto {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")
        return userService.updateProfile(userId, dto)
    }

    @DeleteMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    fun deleteProfile(@AuthenticationPrincipal principal: Any?): UserProfileDto {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")
        return userService.delete(userId)
    }
}
