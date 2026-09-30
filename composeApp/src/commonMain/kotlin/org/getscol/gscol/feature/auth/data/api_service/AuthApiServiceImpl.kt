package org.getscol.gscol.feature.auth.data.api_service

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.getscol.gscol.core.data.network.markAsNoAuth
import org.getscol.gscol.core.data.network.newSafeApiCall
import org.getscol.gscol.core.domain.DataError
import org.getscol.gscol.core.domain.Result
import org.getscol.gscol.feature.auth.domain.model.ForgotPasswordRequest
import org.getscol.gscol.feature.auth.domain.model.ForgotPasswordResponse
import org.getscol.gscol.feature.auth.domain.model.LoginRequest
import org.getscol.gscol.feature.auth.domain.model.LoginResponse
import org.getscol.gscol.feature.auth.domain.model.OtpVerificationRequest
import org.getscol.gscol.feature.auth.domain.model.OtpVerificationResponse
import org.getscol.gscol.feature.auth.domain.model.RegistrationRequest
import org.getscol.gscol.feature.auth.domain.model.RegistrationResponse
import org.getscol.gscol.feature.auth.domain.model.ResendOtpRequest
import org.getscol.gscol.feature.auth.domain.model.ResendOtpResponse

class AuthApiServiceImpl(
    private val httpClient: HttpClient
) : AuthApiService {

    override suspend fun register(
        phone: String,
        password: String,
        fullName: String
    ): Result<RegistrationResponse, DataError> {
        return newSafeApiCall {
            httpClient.post("auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegistrationRequest(
                        phone = phone,
                        password = password,
                        fullName = fullName
                    )
                )
                markAsNoAuth()
            }
        }
    }

    override suspend fun login(
        phoneNumber: String,
        password: String
    ): Result<LoginResponse, DataError> {
        return newSafeApiCall {
            httpClient.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(phoneNumber = phoneNumber, password = password))
                markAsNoAuth()
            }
        }
    }

    override suspend fun verifyOtp(
        otp: String,
        otpAccessToken: String
    ): Result<OtpVerificationResponse, DataError> {
        return newSafeApiCall {
            httpClient.post("v2/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(OtpVerificationRequest(otp = otp, otpAccessToken = otpAccessToken))
                markAsNoAuth()
            }
        }
    }

    override suspend fun resendOtp(otpAccessToken: String): Result<ResendOtpResponse, DataError> {
        return newSafeApiCall {
            httpClient.post("v2/auth/resend-otp") {
                contentType(ContentType.Application.Json)
                setBody(ResendOtpRequest(otpAccessToken = otpAccessToken))
                markAsNoAuth()
            }
        }
    }

    override suspend fun forgotPassword(
        phone: String,
        newPassword: String
    ): Result<ForgotPasswordResponse, DataError> {
        return newSafeApiCall {
            httpClient.post("auth/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequest(phone = phone, newPassword))
                markAsNoAuth()
            }
        }
    }

}