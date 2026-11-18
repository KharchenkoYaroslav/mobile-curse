package ua.kpi.transformermonitoringsystem.server.controller

import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.auth.dto.AuthCredentialsDto
import ua.kpi.transformermonitoringsystem.auth.dto.AuthResponseDto
import ua.kpi.transformermonitoringsystem.auth.dto.ChangeRoleToAdminRequestDto
import ua.kpi.transformermonitoringsystem.auth.dto.LogoutRequestDto
import ua.kpi.transformermonitoringsystem.auth.dto.RefreshRequestDto
import ua.kpi.transformermonitoringsystem.auth.security.RefreshTokenGuard
import ua.kpi.transformermonitoringsystem.auth.service.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val refreshTokenGuard: RefreshTokenGuard
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody credentialsDto: AuthCredentialsDto): AuthResponseDto {
        return authService.register(credentialsDto.login, credentialsDto.password)
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody credentialsDto: AuthCredentialsDto): AuthResponseDto {
        return authService.login(credentialsDto.login, credentialsDto.password)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @AuthenticationPrincipal principal: Any?,
        @RequestBody(required = false) body: LogoutRequestDto?
    ) {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")

        val refreshToken = body?.refreshToken?.takeIf { it.isNotBlank() }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Не передано refreshToken для виходу")

        authService.logout(userId, refreshToken)
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refreshTokens(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authHeader: String?,
        @RequestBody(required = false) body: RefreshRequestDto?
    ): AuthResponseDto {
        val authUser = refreshTokenGuard.validateAndExtract(authHeader, body?.refreshToken)
        return authService.refreshTokens(authUser.sub, authUser.refreshToken!!)
    }

    @PostMapping("/role/admin")
    @ResponseStatus(HttpStatus.OK)
    fun changeRoleToAdmin(
        @AuthenticationPrincipal principal: Any?,
        @Valid @RequestBody body: ChangeRoleToAdminRequestDto
    ) {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")

        authService.changeRoleToAdmin(userId, body.key)
    }

    @PostMapping("/role/user")
    @ResponseStatus(HttpStatus.OK)
    fun changeRoleToUser(
        @AuthenticationPrincipal principal: Any?
    ) {
        val userId = principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Немає доступу. Необхідна авторизація")

        authService.changeRoleToUser(userId)
    }
}
