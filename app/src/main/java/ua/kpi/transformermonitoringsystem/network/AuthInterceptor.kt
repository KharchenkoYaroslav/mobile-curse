package ua.kpi.transformermonitoringsystem.network

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ua.kpi.transformermonitoringsystem.database.AuthDataStore

/**
 * Interceptor для автоматичної додачі заголовка авторизації Authorization: Bearer <accessToken> до кожного HTTP-запиту.
 */
class AuthInterceptor(private val authDataStore: AuthDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        if (path.endsWith("auth/login") || path.endsWith("auth/register") || path.endsWith("auth/refresh")) {
            return chain.proceed(original)
        }

        val token = try {
            runBlocking { authDataStore.accessTokenFlow.firstOrNull() }
        } catch (_: Exception) {
            null
        }

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
