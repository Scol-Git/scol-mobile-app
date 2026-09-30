package org.getscol.gscol.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.getscol.gscol.core.data.dto.auth.AuthTokenResponse
import org.getscol.gscol.core.utils.AppLogger
import org.getscol.gscol.feature.auth.data.AuthTokenProvider
import org.getscol.gscol.getPlatform
import org.getscol.gscol.navigation.LogoutEventManager
import org.getscol.gscol.navigation.NavigationAction

object HttpClientFactory {

    private const val TIMEOUT_MS = 20_000L

    fun createHttpClient(
        engine: HttpClientEngine,
        tokenProvider: AuthTokenProvider,
        baseUrl: String
    ): HttpClient = HttpClient(engine) {
        installJson()
        installTimeout()
        installLogging()
        installAuth(tokenProvider)

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }

    }

    private fun HttpClientConfig<*>.installJson() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private fun HttpClientConfig<*>.installTimeout() {
        install(HttpTimeout) {
            socketTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
        }
    }

    private fun HttpClientConfig<*>.installLogging() {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = AppLogger.d(message)
            }
            level = when {
                !AppLogger.isEnabled -> LogLevel.NONE
                getPlatform().platformName == "IOS" -> LogLevel.HEADERS
                else -> LogLevel.ALL
            }
        }
    }

    /**
     * The [Auth] bearer plugin fully owns authentication:
     *  - [sendWithoutRequest] a  decider about add token in header or not
     *    [markAsNoAuth] (login, register, OTP flow, token refresh). This also records the
     *    provider's token version, which is what lets [refreshTokens] fire on a 401.
     *  - [loadTokens] reads from storage; a `null`/blank token is never cached, so it is
     *    re-read until a real token exists.
     *  - [refreshTokens] exchanges the refresh token for a new pair on a 401.
     */
    private fun HttpClientConfig<*>.installAuth(tokenProvider: AuthTokenProvider) {
        install(Auth) {
            bearer {
                sendWithoutRequest { request -> !request.isMarkedAsNoAuth() }

                loadTokens {
                    val accessToken = tokenProvider.getAccessToken().orEmpty()
                    if (accessToken.isBlank()) return@loadTokens null
                    BearerTokens(
                        accessToken = accessToken,
                        refreshToken = tokenProvider.getRefreshToken().orEmpty()
                    )
                }

                refreshTokens {
                    val refreshToken = tokenProvider.getRefreshToken()
                    if (refreshToken.isNullOrBlank()) {
                        logoutAndClear(tokenProvider)
                        return@refreshTokens null
                    }

                    try {
                        val response = client.post("auth/refresh") {
                            markAsNoAuth()
                            setBody(mapOf("refreshToken" to refreshToken))
                        }

                        if (!response.status.isSuccess()) {
                            AppLogger.e("Refresh failed with status ${response.status}")
                            logoutAndClear(tokenProvider)
                            return@refreshTokens null
                        }

                        val tokens = response.body<AuthTokenResponse>().data

                        val newRefreshToken = tokens?.refreshToken ?: refreshToken
                        tokenProvider.saveTokens(tokens?.accessToken, newRefreshToken)
                        BearerTokens(
                            accessToken = tokens?.accessToken.orEmpty(),
                            refreshToken = newRefreshToken
                        )
                    } catch (e: Exception) {
                        AppLogger.e("Refresh token failed", e)
                        logoutAndClear(tokenProvider)
                        null
                    }
                }
            }
        }
    }

    private suspend fun logoutAndClear(tokenProvider: AuthTokenProvider) {
        tokenProvider.clearTokens()
        LogoutEventManager.sendLogoutEvent(NavigationAction.NavigateToLogInScreen)
    }
}