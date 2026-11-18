@file:Suppress("unused")

package ua.kpi.transformermonitoringsystem.di

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.kpi.transformermonitoringsystem.BuildConfig
import ua.kpi.transformermonitoringsystem.data.AuthApiService
import ua.kpi.transformermonitoringsystem.database.AuthDataStore
import ua.kpi.transformermonitoringsystem.network.AuthInterceptor
import ua.kpi.transformermonitoringsystem.network.BackendHostManager
import ua.kpi.transformermonitoringsystem.network.TokenAuthenticator
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

// Мережевий провайдер
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authDataStore: AuthDataStore,
        authApiProvider: Provider<AuthApiService>,
        backendHostManager: BackendHostManager,
    ): OkHttpClient {
        val candidateHosts = BuildConfig.BACKEND_HOSTS.split(",")
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url

                val hostsToTry = mutableListOf<String>()
                val currentActive = backendHostManager.activeBackendHost
                if (!hostsToTry.contains(currentActive)) {
                    hostsToTry.add(currentActive)
                }

                candidateHosts.forEach { h ->
                    if (!hostsToTry.contains(h)) hostsToTry.add(h)
                }

                var lastException: IOException? = null
                for (host in hostsToTry) {
                    try {
                        val newUrl = url.newBuilder().host(host).build()
                        val newRequest = request.newBuilder().url(newUrl).build()
                        val response = chain.proceed(newRequest)
                        backendHostManager.activeBackendHost = host
                        return@addInterceptor response
                    } catch (e: IOException) {
                        lastException = e
                    }
                }
                throw lastException ?: IOException("Не вдалося підключитися до сервера")
            }
            .addInterceptor(AuthInterceptor(authDataStore))
            .authenticator(TokenAuthenticator(authDataStore, authApiProvider))
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, object : TypeAdapter<Instant>() {
                override fun write(out: JsonWriter, value: Instant?) {
                    if (value == null) {
                        out.nullValue()
                    } else {
                        out.value(value.toString())
                    }
                }

                override fun read(reader: JsonReader): Instant? {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull()
                        return null
                    }
                    val str = reader.nextString()
                    return try {
                        Instant.parse(str)
                    } catch (_: Exception) {
                        null
                    }
                }
            })
            .create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
