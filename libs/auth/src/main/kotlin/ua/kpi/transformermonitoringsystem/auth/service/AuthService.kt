package ua.kpi.transformermonitoringsystem.auth.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.auth.dto.AuthResponseDto
import ua.kpi.transformermonitoringsystem.auth.jwt.JwtService
import ua.kpi.transformermonitoringsystem.shared.auth.Role
import ua.kpi.transformermonitoringsystem.user.domain.User
import ua.kpi.transformermonitoringsystem.user.repository.AdminKeyRepository
import ua.kpi.transformermonitoringsystem.user.repository.UserRepository
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val adminKeyRepository: AdminKeyRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    fun generateTokens(userId: String): AuthResponseDto {
        return jwtService.generateTokens(userId)
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    @Transactional
    fun addRefreshToken(userId: String, refreshToken: String) {
        val tokenHash = sha256Hex(refreshToken)
        val hashedRefreshToken = passwordEncoder.encode(tokenHash)

        val userUuid = parseUserId(userId)
        val user = userRepository.findById(userUuid)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Користувача не знайдено") }

        user.refreshTokens.add(hashedRefreshToken)
        if (user.refreshTokens.size > 5) {
            user.refreshTokens = user.refreshTokens.takeLast(5).toMutableList()
        }
        userRepository.save(user)
    }

    @Transactional
    fun register(login: String, pass: String): AuthResponseDto {
        if (userRepository.existsByLogin(login)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Користувач з таким логіном вже існує")
        }

        val hashedPassword = passwordEncoder.encode(pass)
        val user = userRepository.save(
            User(
                login = login,
                password = hashedPassword,
                role = Role.USER,
                refreshTokens = mutableListOf()
            )
        )

        val userId = user.id.toString()
        val tokens = generateTokens(userId)
        addRefreshToken(userId, tokens.refreshToken)

        return tokens
    }

    @Transactional
    fun login(login: String, pass: String): AuthResponseDto {
        val user = userRepository.findByLogin(login)
        if (user == null || !passwordEncoder.matches(pass, user.password)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Невірний логін або пароль")
        }

        val userId = user.id.toString()
        val tokens = generateTokens(userId)
        addRefreshToken(userId, tokens.refreshToken)

        return tokens
    }

    @Transactional
    fun logout(userId: String, refreshToken: String) {
        val userUuid = try {
            UUID.fromString(userId)
        } catch (_: Exception) {
            return
        }

        val user = userRepository.findById(userUuid).orElse(null) ?: return
        if (user.refreshTokens.isEmpty()) {
            return
        }

        val tokenHash = sha256Hex(refreshToken)
        val activeTokens = user.refreshTokens.filterNot { storedTokenHash ->
            passwordEncoder.matches(tokenHash, storedTokenHash)
        }.toMutableList()

        user.refreshTokens = activeTokens
        userRepository.save(user)
    }

    @Transactional
    fun refreshTokens(userId: String, refreshToken: String): AuthResponseDto {
        val userUuid = parseUserId(userId)
        val user = userRepository.findById(userUuid)
            .orElseThrow { ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ заборонено") }

        if (user.refreshTokens.isEmpty()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ заборонено")
        }

        val tokenHash = sha256Hex(refreshToken)
        var tokenIndex = -1

        for (i in user.refreshTokens.indices) {
            if (passwordEncoder.matches(tokenHash, user.refreshTokens[i])) {
                tokenIndex = i
                break
            }
        }

        if (tokenIndex == -1) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ заборонено")
        }

        val tokens = generateTokens(userId)

        val newTokenHash = sha256Hex(tokens.refreshToken)
        val hashedNewRefreshToken = passwordEncoder.encode(newTokenHash)

        user.refreshTokens[tokenIndex] = hashedNewRefreshToken
        userRepository.save(user)

        return tokens
    }

    @Transactional
    fun changeRoleToAdmin(userId: String, key: String) {
        val adminKey = adminKeyRepository.findByKey(key)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Невірний ключ адміністратора")

        val userUuid = parseUserId(userId)
        val user = userRepository.findById(userUuid)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Користувача не знайдено") }

        user.role = Role.ADMIN
        userRepository.save(user)

        // Одноразовий ключ автоматично видаляється після успішного підвищення
        adminKeyRepository.delete(adminKey)
    }

    @Transactional
    fun changeRoleToUser(userId: String) {
        val userUuid = parseUserId(userId)
        val user = userRepository.findById(userUuid)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Користувача не знайдено") }

        user.role = Role.USER
        userRepository.save(user)
    }

    private fun parseUserId(userId: String): UUID {
        return try {
            UUID.fromString(userId)
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Невірний формат ID користувача")
        }
    }
}
