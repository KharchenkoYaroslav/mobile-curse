package ua.kpi.transformermonitoringsystem.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ua.kpi.transformermonitoringsystem.user.domain.AdminKey
import java.util.UUID

@Repository
interface AdminKeyRepository : JpaRepository<AdminKey, UUID> {
    fun findByKey(key: String): AdminKey?
}
