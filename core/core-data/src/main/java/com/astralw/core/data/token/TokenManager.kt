package com.astralw.core.data.token

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.astralw.core.network.token.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "astralw_tokens",
)

/**
 * JWT Token 管理器 — DataStore 持久化 + 实现 TokenProvider 供拦截器使用
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenProvider {

    private val dataStore get() = context.tokenDataStore

    override fun getAccessToken(): String? = runBlocking {
        dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()
    }

    override fun getRefreshTokenSync(): String? = runBlocking {
        dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()
    }

    override fun updateAccessTokenSync(newToken: String) {
        runBlocking {
            dataStore.edit { prefs -> prefs[KEY_ACCESS_TOKEN] = newToken }
        }
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()
    }

    suspend fun getMt5Login(): Int {
        return dataStore.data.map { it[KEY_MT5_LOGIN] ?: 0 }.first()
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, mt5Login: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_MT5_LOGIN] = mt5Login
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }

    suspend fun hasTokens(): Boolean {
        return dataStore.data.map { it[KEY_ACCESS_TOKEN] != null }.first()
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_MT5_LOGIN = intPreferencesKey("mt5_login")
    }
}
