package org.getscol.gscol.feature.home.data.api_service

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.getscol.gscol.core.data.network.markAsNoAuth
import org.getscol.gscol.core.data.network.safeApiCall
import org.getscol.gscol.core.domain.DataError
import org.getscol.gscol.core.domain.Result
import org.getscol.gscol.feature.home.data.dto.HomeResponseDto
import org.getscol.gscol.feature.home.domain.model.CourseRequest

class HomeApiServiceImpl(private val httpClient: HttpClient) : HomeApiService {
    override suspend fun getHomeData(courseRequest: CourseRequest, isLogin: Boolean): Result<HomeResponseDto, DataError.Remote> {
        return safeApiCall {
            httpClient.post("/home") {
                contentType(ContentType.Application.Json)
                setBody(courseRequest)
                if (!isLogin) markAsNoAuth()
            }
        }
    }
}