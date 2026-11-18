package ua.kpi.transformermonitoringsystem.auth.security

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.auth.dto.AuthUserDto
import ua.kpi.transformermonitoringsystem.auth.jwt.JwtService

@Component
class RefreshTokenGuard(
    private val jwtService: JwtService
) {

    fun validateAndExtract(authHeader: String?, bodyRefreshToken: String?): AuthUserDto {
        val token = when {
            !authHeader.isNullOrBlank() -> {
                val parts = authHeader.trim().split(" ")
                if (parts.size != 2 || parts[0] != "Bearer" || parts[1].isBlank()) {
                    throw ResponseStatusException(HttpStatus.FORBIDDEN, "Невірний тип авторизації")
                }
                parts[1]
            }
            !bodyRefreshToken.isNullOrBlank() -> bodyRefreshToken
            else -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Немає заголовка авторизації для оновлення")
        }

        val userId = jwtService.extractUserIdFromRefreshToken(token)
        return AuthUserDto(sub = userId, refreshToken = token)
    }
}
