package ua.kpi.transformermonitoringsystem.user.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ua.kpi.transformermonitoringsystem.user.domain.User
import ua.kpi.transformermonitoringsystem.user.dto.UpdateUserDto
import ua.kpi.transformermonitoringsystem.user.dto.UserProfileDto
import ua.kpi.transformermonitoringsystem.user.repository.UserRepository
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findById(id: String): User? {
        val uuid = parseUserId(id)
        return userRepository.findById(uuid).orElse(null)
    }

    fun findByLogin(login: String): User? {
        return userRepository.findByLogin(login)
    }

    fun getProfileById(id: String): UserProfileDto {
        val user = findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Користувача не знайдено")
        return UserProfileDto.from(user)
    }

    @Transactional
    fun updateProfile(id: String, dto: UpdateUserDto): UserProfileDto {
        val uuid = parseUserId(id)
        val user = userRepository.findById(uuid)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Користувача не знайдено") }

        // Якщо користувач хоче оновити пароль або логін, перевіряємо поточний пароль (як у NestJS)
        if (!dto.password.isNullOrBlank() || !dto.login.isNullOrBlank()) {
            val currentPassword = dto.currentPassword
            if (currentPassword.isNullOrBlank() || !passwordEncoder.matches(currentPassword, user.password)) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Невірний поточний пароль")
            }
        }

        // Оновлення логіну з перевіркою унікальності
        dto.login?.takeIf { it.isNotBlank() }?.let { newLogin ->
            if (newLogin != user.login) {
                val existing = userRepository.findByLogin(newLogin)
                if (existing != null && existing.id != user.id) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Користувач з таким логіном вже існує")
                }
                user.login = newLogin
            }
        }

        // Оновлення пароля та інвалідація старих refresh-токенів
        dto.password?.takeIf { it.isNotBlank() }?.let { newPassword ->
            user.password = passwordEncoder.encode(newPassword)
            user.refreshTokens.clear()
        }

        val savedUser = userRepository.save(user)
        return UserProfileDto.from(savedUser)
    }

    @Transactional
    fun delete(id: String): UserProfileDto {
        val uuid = parseUserId(id)
        val user = userRepository.findById(uuid)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Користувача не знайдено") }

        userRepository.delete(user)
        return UserProfileDto.from(user)
    }

    private fun parseUserId(userId: String): UUID {
        return try {
            UUID.fromString(userId)
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Невірний формат ID користувача")
        }
    }
}
