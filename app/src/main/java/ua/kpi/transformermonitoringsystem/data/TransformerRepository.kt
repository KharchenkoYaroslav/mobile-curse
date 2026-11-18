package ua.kpi.transformermonitoringsystem.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ua.kpi.transformermonitoringsystem.data.model.AuthCredentials
import ua.kpi.transformermonitoringsystem.data.model.AuthResponse
import ua.kpi.transformermonitoringsystem.data.model.ChangeRoleToAdminRequest
import ua.kpi.transformermonitoringsystem.data.model.LogoutRequest
import ua.kpi.transformermonitoringsystem.data.model.TransformerStats as TransformerStatsDto
import ua.kpi.transformermonitoringsystem.data.model.UpdateUser
import ua.kpi.transformermonitoringsystem.data.model.UserProfile
import ua.kpi.transformermonitoringsystem.database.AuthDataStore
import ua.kpi.transformermonitoringsystem.database.TransformerDao
import ua.kpi.transformermonitoringsystem.database.entity.Transformer
import ua.kpi.transformermonitoringsystem.database.entity.TransformerStatus
import ua.kpi.transformermonitoringsystem.network.ErrorHandler.retryRequest
import ua.kpi.transformermonitoringsystem.util.toDto
import ua.kpi.transformermonitoringsystem.util.toEntity

interface TransformerRepository {
    //  Session & Auth State (AuthDataStore)
    val accessTokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
    val roleFlow: Flow<String>
    val loginFlow: Flow<String?>

    //  Auth (AuthApiService + AuthDataStore)
    suspend fun register(credentials: AuthCredentials): Result<AuthResponse>
    suspend fun login(credentials: AuthCredentials): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    suspend fun changeRoleToAdmin(secretKey: String): Result<Unit>
    suspend fun changeRoleToUser(): Result<Unit>

    //  User Profile (UserApiService + AuthDataStore)
    suspend fun getCurrentUserProfile(): Result<UserProfile>
    suspend fun updateProfile(dto: UpdateUser): Result<UserProfile>
    suspend fun deleteProfile(): Result<UserProfile>

    //  Transformers (TransformerApiService + Room)
    suspend fun getAllTransformers(
        forceRefresh: Boolean = false,
        localOnly: Boolean = false
    ): Result<List<Transformer>>
    suspend fun getTransformerById(
        id: Int,
        localOnly: Boolean = false
    ): Result<Transformer>
    suspend fun addTransformer(transformer: Transformer): Result<Transformer>
    suspend fun insertTransformerWithData(
        transformer: Transformer,
        statuses: List<TransformerStatus> = emptyList()
    ): Result<Transformer>
    suspend fun updateTransformer(transformer: Transformer): Result<Transformer>
    suspend fun deleteTransformer(transformer: Transformer): Result<Unit>
    suspend fun deleteTransformerById(id: Int): Result<Unit>

    //  Stats & Telemetry (TransformerApiService + Room)
    suspend fun getNetworkStats(
        transformerEntities: List<Transformer>,
        localOnly: Boolean = false
    ): Result<NetworkStats>
    fun calculateNetworkStatsLocally(transformerEntities: List<Transformer>): NetworkStats
    suspend fun getAdminStats(): Result<TransformerStatsDto>
    suspend fun getStatuses(
        transformerId: Int,
        localOnly: Boolean = false
    ): Result<List<TransformerStatus>>
    suspend fun addStatus(status: TransformerStatus): Result<TransformerStatus>
    suspend fun deleteStatus(status: TransformerStatus): Result<Unit>
    suspend fun getAverageTemperature(transformerId: Int): Result<Double>
    suspend fun getAverageLoad(transformerId: Int): Result<Double>
}

class TransformerRepositoryImpl(
    private val authApi: AuthApiService,
    private val userApi: UserApiService,
    private val transformerApi: TransformerApiService,
    private val dao: TransformerDao,
    private val authDataStore: AuthDataStore
) : TransformerRepository {

    override val accessTokenFlow: Flow<String?> = authDataStore.accessTokenFlow
    override val refreshTokenFlow: Flow<String?> = authDataStore.refreshTokenFlow
    override val roleFlow: Flow<String> = authDataStore.roleFlow
    override val loginFlow: Flow<String?> = authDataStore.loginFlow

    private suspend fun isAuthenticated(): Boolean =
        !authDataStore.accessTokenFlow.firstOrNull().isNullOrBlank()

    private fun extractRoleFromJwt(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return "USER"
            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val json = JSONObject(String(payloadBytes, Charsets.UTF_8))
            val rawRole = json.optString("role", "").uppercase().removePrefix("ROLE_")
            if (rawRole == "ADMIN" || rawRole == "USER") rawRole else "USER"
        } catch (_: Exception) {
            "USER"
        }
    }

    private suspend inline fun <T> safeCall(
        crossinline block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // AuthApiService + AuthDataStore

    override suspend fun register(credentials: AuthCredentials): Result<AuthResponse> = safeCall {
        val response = authApi.register(credentials)
        authDataStore.saveAuthData(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            role = extractRoleFromJwt(response.accessToken),
            login = credentials.login
        )
        response
    }

    override suspend fun login(credentials: AuthCredentials): Result<AuthResponse> = safeCall {
        val response = authApi.login(credentials)
        authDataStore.saveAuthData(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            role = extractRoleFromJwt(response.accessToken),
            login = credentials.login
        )
        response
    }

    override suspend fun logout(): Result<Unit> = safeCall {
        try {
            val refreshToken = authDataStore.refreshTokenFlow.firstOrNull()
            if (!refreshToken.isNullOrBlank()) {
                authApi.logout(LogoutRequest(refreshToken))
            }
        } finally {
            authDataStore.clearAuthData()
        }
    }

    override suspend fun changeRoleToAdmin(secretKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isAuthenticated()) {
            return@withContext Result.failure(IllegalStateException("Користувач не авторизований"))
        }
        try {
            authApi.changeRoleToAdmin(ChangeRoleToAdminRequest(secretKey))
            authDataStore.updateRole("ADMIN")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeRoleToUser(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isAuthenticated()) {
            return@withContext Result.failure(IllegalStateException("Користувач не авторизований"))
        }
        try {
            authApi.changeRoleToUser()
            authDataStore.updateRole("USER")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // UserApiService + AuthDataStore


    override suspend fun getCurrentUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        if (!isAuthenticated()) {
            return@withContext Result.failure(IllegalStateException("Користувач не авторизований"))
        }
        try {
            val profile = retryRequest(times = 2) { userApi.getCurrentUserProfile() }
            authDataStore.updateRole(profile.role.name)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(dto: UpdateUser): Result<UserProfile> = withContext(Dispatchers.IO) {
        if (!isAuthenticated()) {
            return@withContext Result.failure(IllegalStateException("Користувач не авторизований"))
        }
        try {
            val updated = retryRequest(times = 2) { userApi.updateProfile(dto) }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        if (!isAuthenticated()) {
            return@withContext Result.failure(IllegalStateException("Користувач не авторизований"))
        }
        try {
            val deleted = userApi.deleteProfile()
            authDataStore.clearAuthData()
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TransformerApiService + Room

    override suspend fun getAllTransformers(
        forceRefresh: Boolean,
        localOnly: Boolean
    ): Result<List<Transformer>> =
        withContext(Dispatchers.IO) {
            if (localOnly) {
                val localData = dao.getAllTransformers()
                return@withContext if (localData.isNotEmpty()) {
                    Result.success(localData)
                } else {
                    Result.failure(NoSuchElementException("Локальний кеш трансформаторів порожній"))
                }
            }

            try {
                val remoteDtos = retryRequest(times = 3) { transformerApi.getTransformers() }
                val remoteEntities = remoteDtos.map { it.toEntity() }

                val localMap = dao.getAllTransformers().associateBy { it.id }
                val remoteIds = remoteEntities.map { it.id }.toSet()

                // Видаляємо лише ті вузли, яких більше немає на сервері
                localMap.values
                    .filter { it.id !in remoteIds }
                    .forEach { dao.deleteTransformer(it) }

                remoteEntities.forEach { remote ->
                    if (localMap.containsKey(remote.id)) {
                        dao.updateTransformer(remote)
                    } else {
                        dao.insertTransformer(remote)
                    }
                }

                Result.success(remoteEntities)
            } catch (e: Exception) {
                if (!forceRefresh) {
                    val localData = dao.getAllTransformers()
                    if (localData.isNotEmpty()) {
                        return@withContext Result.success(localData)
                    }
                }
                Result.failure(e)
            }
        }

    override suspend fun getTransformerById(
        id: Int,
        localOnly: Boolean
    ): Result<Transformer> =
        withContext(Dispatchers.IO) {
            if (localOnly) {
                val localEntity = dao.getAllTransformers().find { it.id == id }
                return@withContext if (localEntity != null) {
                    Result.success(localEntity)
                } else {
                    Result.failure(NoSuchElementException("Трансформатор з ID $id відсутній у кеші"))
                }
            }

            try {
                val remoteDto = retryRequest(times = 2) { transformerApi.getTransformerById(id) }
                val entity = remoteDto.toEntity()
                dao.updateTransformer(entity)
                Result.success(entity)
            } catch (e: Exception) {
                val localEntity = dao.getAllTransformers().find { it.id == id }
                if (localEntity != null) {
                    Result.success(localEntity)
                } else {
                    Result.failure(e)
                }
            }
        }

    override suspend fun addTransformer(transformer: Transformer): Result<Transformer> =
        insertTransformerWithData(transformer, emptyList())

    override suspend fun insertTransformerWithData(
        transformer: Transformer,
        statuses: List<TransformerStatus>
    ): Result<Transformer> = withContext(Dispatchers.IO) {
        try {
            val createdDto = retryRequest(times = 2) {
                transformerApi.addTransformer(transformer.toDto())
            }
            val createdEntity = createdDto.toEntity()
            dao.insertTransformer(createdEntity)

            statuses.forEach { status ->
                val createdStatusDto = retryRequest(times = 2) {
                    transformerApi.addStatus(
                        createdEntity.id,
                        status.copy(transformerId = createdEntity.id).toDto()
                    )
                }
                dao.insertStatus(createdStatusDto.toEntity())
            }
            Result.success(createdEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransformer(transformer: Transformer): Result<Transformer> =
        withContext(Dispatchers.IO) {
            try {
                val updatedDto = retryRequest(times = 2) {
                    transformerApi.updateTransformer(transformer.id, transformer.toDto())
                }
                val updatedEntity = updatedDto.toEntity()
                dao.updateTransformer(updatedEntity)
                Result.success(updatedEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteTransformer(transformer: Transformer): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                retryRequest(times = 2) { transformerApi.deleteTransformer(transformer.id) }
                dao.deleteTransformer(transformer)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteTransformerById(id: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                retryRequest(times = 2) { transformerApi.deleteTransformer(id) }
                dao.getAllTransformers().find { it.id == id }?.let { dao.deleteTransformer(it) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getNetworkStats(
        transformerEntities: List<Transformer>,
        localOnly: Boolean
    ): Result<NetworkStats> =
        withContext(Dispatchers.IO) {
            if (localOnly) {
                return@withContext if (transformerEntities.isNotEmpty()) {
                    Result.success(calculateNetworkStatsLocally(transformerEntities))
                } else {
                    Result.failure(NoSuchElementException("Локальні дані для розрахунку показників відсутні"))
                }
            }

            try {
                val dto = retryRequest(times = 2) { transformerApi.getStats() }
                Result.success(
                    NetworkStats(
                        averageLoad = dto.averageLoad,
                        averageTemperature = dto.averageTemperature,
                        criticalCount = dto.criticalIssues
                    )
                )
            } catch (e: Exception) {
                if (transformerEntities.isNotEmpty()) {
                    Result.success(calculateNetworkStatsLocally(transformerEntities))
                } else {
                    Result.failure(e)
                }
            }
        }

    override fun calculateNetworkStatsLocally(transformerEntities: List<Transformer>): NetworkStats {
        if (transformerEntities.isEmpty()) return NetworkStats(0.0, 0.0, 0)

        val avgLoad = transformerEntities.map { it.currentLoad }.average()
        val avgTemp = transformerEntities.map { it.temperature }.average()
        val criticalCount = transformerEntities.count {
            (it.temperature > 85.0) || (it.currentLoad > (it.maxLoad * 0.9))
        }

        return NetworkStats(avgLoad, avgTemp, criticalCount)
    }

    override suspend fun getAdminStats(): Result<TransformerStatsDto> = safeCall {
        retryRequest(times = 2) { transformerApi.getStats() }
    }

    override suspend fun getStatuses(
        transformerId: Int,
        localOnly: Boolean
    ): Result<List<TransformerStatus>> =
        withContext(Dispatchers.IO) {
            if (localOnly) {
                val localStatuses = dao.getStatusesForTransformer(transformerId)
                return@withContext if (localStatuses.isNotEmpty()) {
                    Result.success(localStatuses)
                } else {
                    Result.failure(NoSuchElementException("Історія показників у кеші порожня"))
                }
            }

            try {
                val remoteDtos = retryRequest(times = 2) { transformerApi.getStatuses(transformerId) }
                val remoteEntities = remoteDtos.map { it.toEntity() }
                remoteEntities.forEach { dao.insertStatus(it) }
                Result.success(remoteEntities)
            } catch (e: Exception) {
                val localStatuses = dao.getStatusesForTransformer(transformerId)
                if (localStatuses.isNotEmpty()) {
                    Result.success(localStatuses)
                } else {
                    Result.failure(e)
                }
            }
        }

    override suspend fun addStatus(status: TransformerStatus): Result<TransformerStatus> =
        withContext(Dispatchers.IO) {
            try {
                val createdDto = retryRequest(times = 2) {
                    transformerApi.addStatus(status.transformerId, status.toDto())
                }
                val createdEntity = createdDto.toEntity()
                dao.insertStatus(createdEntity)
                Result.success(createdEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteStatus(status: TransformerStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                retryRequest(times = 2) { transformerApi.deleteStatus(status.id) }
                dao.deleteStatus(status)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAverageTemperature(transformerId: Int): Result<Double> = safeCall {
        dao.getAverageTemperature(transformerId) ?: 0.0
    }

    override suspend fun getAverageLoad(transformerId: Int): Result<Double> = safeCall {
        dao.getAverageLoad(transformerId) ?: 0.0
    }
}

data class NetworkStats(
    val averageLoad: Double,
    val averageTemperature: Double,
    val criticalCount: Int
)