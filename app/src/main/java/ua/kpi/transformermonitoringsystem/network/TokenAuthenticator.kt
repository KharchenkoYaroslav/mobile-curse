package ua.kpi.transformermonitoringsystem.network

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ua.kpi.transformermonitoringsystem.data.AuthApiService
import ua.kpi.transformermonitoringsystem.data.model.RefreshRequest
import ua.kpi.transformermonitoringsystem.database.AuthDataStore
import javax.inject.Provider

// Автоматичне оновлення сесії
class TokenAuthenticator(
    private val authDataStore: AuthDataStore,
    private val authApiProvider: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("auth/refresh") || path.endsWith("auth/login") || path.endsWith("auth/register")) {
            return null
        }

        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(this) {
            val currentAccessToken = runBlocking { authDataStore.accessTokenFlow.firstOrNull() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            if (!currentAccessToken.isNullOrBlank() && currentAccessToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val refreshToken = runBlocking { authDataStore.refreshTokenFlow.firstOrNull() }
            if (refreshToken.isNullOrBlank()) {
                return null
            }

            return try {
                val newTokens = runBlocking {
                    authApiProvider.get().refreshTokens(RefreshRequest(refreshToken))
                }
                if (newTokens.accessToken.isBlank() || newTokens.refreshToken.isBlank()) {
                    runBlocking { authDataStore.clearAuthData() }
                    return null
                }
                runBlocking {
                    val role = authDataStore.roleFlow.firstOrNull() ?: "USER"
                    val login = authDataStore.loginFlow.firstOrNull() ?: ""
                    authDataStore.saveAuthData(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                        role = role,
                        login = login
                    )
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } catch (_: Exception) {
                runBlocking { authDataStore.clearAuthData() }
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
