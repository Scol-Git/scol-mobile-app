package org.getscol.gscol.feature.auth.data.api_service

import org.getscol.gscol.core.domain.DataError
import org.getscol.gscol.core.domain.Result
import org.getscol.gscol.feature.auth.domain.model.ForgotPasswordResponse
import org.getscol.gscol.feature.auth.domain.model.LoginResponse
import org.getscol.gscol.feature.auth.domain.model.OtpVerificationResponse
import org.getscol.gscol.feature.auth.domain.model.RegistrationResponse
import org.getscol.gscol.feature.auth.domain.model.ResendOtpResponse

interface AuthApiService {
    suspend fun register(
        phone: String,
        password: String,
        fullName: String
    ): Result<RegistrationResponse, DataError>

    suspend fun login(
        phoneNumber: String,
        password: String
    ): Result<LoginResponse, DataError>

    suspend fun verifyOtp(otp: String, otpAccessToken: String): Result<OtpVerificationResponse, DataError>

    suspend fun resendOtp(otpAccessToken: String): Result<ResendOtpResponse, DataError>

    suspend fun forgotPassword(
        phone: String,
        newPassword: String
    ): Result<ForgotPasswordResponse, DataError>

}