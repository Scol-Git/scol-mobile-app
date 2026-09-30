package org.getscol.gscol.core.data.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.getscol.gscol.core.domain.BaseResponse

@Serializable
data class AuthTokenResponse(
    @SerialName("data") val data: AuthTokenData? = null
) : BaseResponse()

@Serializable
data class AuthTokenData(
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("refreshToken") val refreshToken: String? = null
)