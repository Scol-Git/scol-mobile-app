package org.getscol.gscol.feature.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResendOtpRequest(
    @SerialName("otpAccessToken") val otpAccessToken: String
)
