package org.getscol.gscol.core.di

import com.getscol.gscol.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.getscol.gscol.core.data.network.HttpClientFactory
import org.getscol.gscol.feature.auth.data.AuthTokenProvider
import org.koin.dsl.module


val networkModule = module {
    single<HttpClient> {
        val tokenProvider = get<AuthTokenProvider>()
        HttpClientFactory.createHttpClient(
            engine = get(),
            tokenProvider = tokenProvider,
            baseUrl = BuildKonfig.BASE_URL
        ).also { client ->
            tokenProvider.invalidateTokenCache = {
                client.authProvider<BearerAuthProvider>()?.clearToken()
            }
        }
    }
}


