package ua.kpi.transformermonitoringsystem.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Збереження JWT токенів та ролей у DataStore.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthDataStore(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val LOGIN_KEY = stringPreferencesKey("user_login")
    }

    /**
     * Потік доступу до JWT Access Token.
     */
    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]?.takeIf { it.isNotBlank() }
    }

    /**
     * Потік доступу до Refresh Token.
     */
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]?.takeIf { it.isNotBlank() }
    }

    /**
     * Потік доступу до ролі користувача ("ADMIN", "USER", або "GUEST").
     */
    val roleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY] ?: "GUEST"
    }

    /**
     * Потік доступу до логіну користувача.
     */
    val loginFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LOGIN_KEY]
    }

    /**
     * Збереження токенів, ролі та логін.
     */
    suspend fun saveAuthData(accessToken: String, refreshToken: String, role: String, login: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[ROLE_KEY] = role
            preferences[LOGIN_KEY] = login
        }
    }

    /**
     * Оновлення ролі користувача при зміні прав.
     */
    suspend fun updateRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role
        }
    }

    /**
     * Очищення даних авторизації при виході з системи (Logout).
     */
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(LOGIN_KEY)
        }
    }
}
