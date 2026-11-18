package ua.kpi.transformermonitoringsystem.data

import retrofit2.http.*
import ua.kpi.transformermonitoringsystem.data.model.AuthCredentials
import ua.kpi.transformermonitoringsystem.data.model.AuthResponse
import ua.kpi.transformermonitoringsystem.data.model.ChangeRoleToAdminRequest
import ua.kpi.transformermonitoringsystem.data.model.LogoutRequest
import ua.kpi.transformermonitoringsystem.data.model.RefreshRequest
import ua.kpi.transformermonitoringsystem.data.model.Transformer
import ua.kpi.transformermonitoringsystem.data.model.TransformerStats
import ua.kpi.transformermonitoringsystem.data.model.TransformerStatus
import ua.kpi.transformermonitoringsystem.data.model.UpdateUser
import ua.kpi.transformermonitoringsystem.data.model.UserProfile

/**
 * Інтерфейс REST API для авторизації користувачів.
 */
interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body credentials: AuthCredentials): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body credentials: AuthCredentials): AuthResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("auth/refresh")
    suspend fun refreshTokens(@Body request: RefreshRequest): AuthResponse

    @POST("auth/role/admin")
    suspend fun changeRoleToAdmin(@Body request: ChangeRoleToAdminRequest)

    @POST("auth/role/user")
    suspend fun changeRoleToUser()
}

/**
 * Інтерфейс REST API для роботи з профілем користувача.
 */
interface UserApiService {
    @GET("users/profile")
    suspend fun getCurrentUserProfile(): UserProfile

    @PUT("users/profile")
    suspend fun updateProfile(@Body dto: UpdateUser): UserProfile

    @DELETE("users/profile")
    suspend fun deleteProfile(): UserProfile
}

/**
 * Інтерфейс REST API для роботи з трансформаторами.
 */
interface TransformerApiService {
    @GET("transformers")
    suspend fun getTransformers(): List<Transformer>

    @GET("transformers/{id}")
    suspend fun getTransformerById(@Path("id") id: Int): Transformer

    @POST("transformers")
    suspend fun addTransformer(@Body transformer: Transformer): Transformer

    @PUT("transformers/{id}")
    suspend fun updateTransformer(@Path("id") id: Int, @Body transformer: Transformer): Transformer

    @DELETE("transformers/{id}")
    suspend fun deleteTransformer(@Path("id") id: Int)

    @GET("transformers/stats")
    suspend fun getStats(): TransformerStats

    @GET("transformers/{id}/statuses")
    suspend fun getStatuses(@Path("id") id: Int): List<TransformerStatus>

    @POST("transformers/{id}/statuses")
    suspend fun addStatus(@Path("id") id: Int, @Body status: TransformerStatus): TransformerStatus

    @DELETE("transformers/statuses/{statusId}")
    suspend fun deleteStatus(@Path("statusId") statusId: Int)
}
