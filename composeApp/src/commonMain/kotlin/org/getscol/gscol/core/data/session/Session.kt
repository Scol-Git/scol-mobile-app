package org.getscol.gscol.core.data.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Session {
    val isUserLoggedIn: StateFlow<Boolean>
    suspend fun setUserLoggedIn(value: Boolean)

    val triggerApplicationListScreen: SharedFlow<Unit>
    suspend fun applicationApplyTrigger()

    val academicFormSubmitTrigger: StateFlow<Int>
    suspend fun triggerAcademicFormSubmission()

    val userFullName: Flow<String?>
    val userJoinedAt: Flow<Int?>

    var otpAccessToken: String?

    suspend fun setUserProfile(fullName: String?, joinedAt: Int?)
    suspend fun resetUserPref()
}
