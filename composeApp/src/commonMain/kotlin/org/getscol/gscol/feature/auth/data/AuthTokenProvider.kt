package org.getscol.gscol.feature.auth.data

import org.getscol.gscol.core.data.session.Session
import org.getscol.gscol.core.data.storage.LocalStorage
import org.getscol.gscol.core.data.storage.StorageKeys
import org.getscol.gscol.core.utils.AppLogger

class AuthTokenProvider(
    private val localStorage: LocalStorage,
    private val session: Session
) {
    /**
     * Invalidates the Ktor bearer cache so the next request re-reads tokens from storage.
     * Wired to the [io.ktor.client.HttpClient] in NetworkModule after the client is built.
     */
    var invalidateTokenCache: (() -> Unit)? = null

    suspend fun saveAccessToken(accessToken: String?) {
        if (accessToken == null) return
        localStorage.setString(StorageKeys.ACCESS_TOKEN, accessToken)
    }

    suspend fun saveRefreshToken(refreshToken: String?) {
        if (refreshToken == null) return
        localStorage.setString(StorageKeys.REFRESH_TOKEN, refreshToken)
    }

    suspend fun saveTokens(accessToken: String?, refreshToken: String?) {
        if(accessToken == null) return
        saveAccessToken(accessToken)
        saveRefreshToken(refreshToken)
        session.setUserLoggedIn(true)
        invalidateTokenCache?.invoke()
    }

    suspend fun getAccessToken(): String? {
        return localStorage.getString(StorageKeys.ACCESS_TOKEN)
    }

    suspend fun getRefreshToken(): String? {
        return localStorage.getString(StorageKeys.REFRESH_TOKEN)
    }

    suspend fun clearTokens() {
        AppLogger.d("clearTokens token ")
        localStorage.remove(StorageKeys.ACCESS_TOKEN)
        localStorage.remove(StorageKeys.REFRESH_TOKEN)
        session.setUserLoggedIn(false)
        invalidateTokenCache?.invoke()
    }
}