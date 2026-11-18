package ua.kpi.transformermonitoringsystem.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ua.kpi.transformermonitoringsystem.user.domain.User
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByLogin(login: String): User?
    fun existsByLogin(login: String): Boolean
}
