package ua.kpi.transformermonitoringsystem.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.auth.dto.AuthResponseDto
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    private val environment: Environment
) {
    private fun getAccessSecret(): String {
        return environment.getProperty("JWT_ACCESS_SECRET")?.takeIf { it.isNotBlank() }
            ?: environment.getProperty("jwt.access.secret")?.takeIf { it.isNotBlank() }
            ?: System.getenv("JWT_ACCESS_SECRET")?.takeIf { it.isNotBlank() }
            ?: "default_jwt_access_secret_key_transformer_monitoring_system_2025_kpi"
    }

    private fun getRefreshSecret(): String {
        return environment.getProperty("JWT_REFRESH_SECRET")?.takeIf { it.isNotBlank() }
            ?: environment.getProperty("jwt.refresh.secret")?.takeIf { it.isNotBlank() }
            ?: System.getenv("JWT_REFRESH_SECRET")?.takeIf { it.isNotBlank() }
            ?: "default_jwt_refresh_secret_key_transformer_monitoring_system_2025_kpi"
    }

    private fun getAccessExpirationMs(): Long {
        return environment.getProperty("JWT_ACCESS_EXPIRATION_MS", Long::class.java)
            ?: (15 * 60 * 1000L) // 15m
    }

    private fun getRefreshExpirationMs(): Long {
        return environment.getProperty("JWT_REFRESH_EXPIRATION_MS", Long::class.java)
            ?: (7 * 24 * 60 * 60 * 1000L) // 7d
    }

    private fun buildKey(secret: String): SecretKey {
        val bytes = secret.toByteArray(StandardCharsets.UTF_8)
        val keyBytes = if (bytes.size >= 32) bytes else MessageDigest.getInstance("SHA-256").digest(bytes)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateAccessToken(userId: String): String {
        val now = Instant.now()
        val exp = now.plusMillis(getAccessExpirationMs())
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(buildKey(getAccessSecret()))
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val now = Instant.now()
        val exp = now.plusMillis(getRefreshExpirationMs())
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(buildKey(getRefreshSecret()))
            .compact()
    }

    fun generateTokens(userId: String): AuthResponseDto {
        val accessToken = generateAccessToken(userId)
        val refreshToken = generateRefreshToken(userId)
        return AuthResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun validateAccessToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(buildKey(getAccessSecret()))
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не валідний токен авторизації")
        }
    }

    fun validateRefreshToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(buildKey(getRefreshSecret()))
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Не валідний або прострочений Refresh токен")
        }
    }

    fun extractUserIdFromAccessToken(token: String): String {
        return validateAccessToken(token).subject
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не валідний токен авторизації")
    }

    fun extractUserIdFromRefreshToken(token: String): String {
        return validateRefreshToken(token).subject
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Не валідний або прострочений Refresh токен")
    }
}
