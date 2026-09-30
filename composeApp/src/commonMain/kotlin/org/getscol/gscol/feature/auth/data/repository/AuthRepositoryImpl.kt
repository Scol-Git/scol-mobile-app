package org.getscol.gscol.feature.auth.data.repository

import org.getscol.gscol.core.data.session.Session
import org.getscol.gscol.core.domain.DataError
import org.getscol.gscol.core.domain.Result
import org.getscol.gscol.feature.auth.data.AuthTokenProvider
import org.getscol.gscol.feature.auth.data.api_service.AuthApiService
import org.getscol.gscol.feature.auth.domain.model.ForgotPasswordResponse
import org.getscol.gscol.feature.auth.domain.model.RegistrationResponse
import org.getscol.gscol.feature.auth.domain.model.ResendOtpResponse
import org.getscol.gscol.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val authTokenProvider: AuthTokenProvider,
    private val session: Session
) : AuthRepository {

    override suspend fun register(
        phone: String,
        password: String,
        fullName: String
    ): Result<RegistrationResponse, DataError> {
        return when (val result = authApiService.register(phone, password, fullName)) {
            is Result.Success -> {
                session.otpAccessToken = result.data.data.otpAccessToken
                Result.Success(result.data)
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun login(
        phoneNumber: String,
        password: String
    ): Result<Unit, DataError> {
        return when (val result = authApiService.login(phoneNumber, password)) {
            is Result.Success -> {
                val resultData = result.data.data
                val academicFormStatus = resultData?.user?.academicFormStatus?.lowercase()
                val isUserFillUpAcademicForm = academicFormStatus == "completed" || academicFormStatus == "partially_completed"

                authTokenProvider.saveTokens(accessToken = resultData?.accessToken, refreshToken = resultData?.refreshToken)
                session.setUserProfile(
                    fullName = resultData?.user?.fullName,
                    joinedAt = resultData?.user?.joinedAt,
                )

                if(isUserFillUpAcademicForm) {
                    session.triggerAcademicFormSubmission()
                }

                Result.Success(Unit)
            }

            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun verifyOtp(otp: String): Result<Unit, DataError> {
        val otpAccessToken = session.otpAccessToken
            ?: return Result.Error(otpSessionExpiredError())

        return when (val result = authApiService.verifyOtp(otp, otpAccessToken)) {
            is Result.Success -> {
                val resultData = result.data.data
                val isUserFillUpAcademicForm = resultData?.user?.academicFormStatus?.lowercase() == "completed"

                authTokenProvider.saveTokens(accessToken = resultData?.accessToken, refreshToken = resultData?.refreshToken)
                session.otpAccessToken = null
                session.setUserProfile(
                    fullName = resultData?.user?.fullName,
                    joinedAt = resultData?.user?.joinedAt,
                )
                if(isUserFillUpAcademicForm) {
                    session.triggerAcademicFormSubmission()
                }

                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun resendOtp(): Result<ResendOtpResponse, DataError> {
        val otpAccessToken = session.otpAccessToken ?: return Result.Error(otpSessionExpiredError())

        return when (val result = authApiService.resendOtp(otpAccessToken)) {
            is Result.Success -> {
                session.otpAccessToken = result.data.data?.otpAccessToken
                Result.Success(result.data)
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun forgotPassword(phone: String, newPassword: String): Result<ForgotPasswordResponse, DataError> {
        return when (val result = authApiService.forgotPassword(phone, newPassword)) {
            is Result.Success -> {
                session.otpAccessToken = result.data.data?.otpAccessToken
                Result.Success(result.data)
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    private fun otpSessionExpiredError(): DataError =
        DataError.RemoteMessage(
            message = "Your OTP session has expired. Please request a new code.",
            statusCode = null
        )
}