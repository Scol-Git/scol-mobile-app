package org.getscol.gscol.core.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.getscol.gscol.core.data.storage.LocalStorage
import org.getscol.gscol.core.data.storage.StorageKeys

class AppSession(
    private val localStorage: LocalStorage,
    appScope: CoroutineScope
) : Session {


    private val _isUserLoggedIn = MutableStateFlow(false)
    override val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _academicFormSubmitTrigger = MutableStateFlow(0)
    override val academicFormSubmitTrigger = _academicFormSubmitTrigger.asStateFlow()

    private val _triggerApplicationListScreen = MutableSharedFlow<Unit>()
    override val triggerApplicationListScreen = _triggerApplicationListScreen.asSharedFlow()

    private val _userFullName = MutableStateFlow<String?>(null)
    override val userFullName: StateFlow<String?> = _userFullName.asStateFlow()

    private val _userJoinedAt = MutableStateFlow<Int?>(null)
    override val userJoinedAt: StateFlow<Int?> = _userJoinedAt.asStateFlow()

    override var otpAccessToken: String? = null

    init {
        appScope.launch(Dispatchers.Default) {
            _isUserLoggedIn.value = localStorage.getBoolean(StorageKeys.IS_USER_LOGGED_IN) ?: false
            _academicFormSubmitTrigger.value = localStorage.getInt(StorageKeys.ACADEMIC_FORM_SUBMIT_COUNT) ?: 0
            _userFullName.value = localStorage.getString(StorageKeys.USER_FULL_NAME)
            _userJoinedAt.value = localStorage.getInt(StorageKeys.USER_JOINED_AT)
        }
    }

    override suspend fun setUserLoggedIn(value: Boolean) {
        _isUserLoggedIn.value = value
        localStorage.setBoolean(StorageKeys.IS_USER_LOGGED_IN, value)
    }

    override suspend fun triggerAcademicFormSubmission() {
        val current = _academicFormSubmitTrigger.value
        _academicFormSubmitTrigger.value = current + 1
        localStorage.setInt(StorageKeys.ACADEMIC_FORM_SUBMIT_COUNT, current + 1)
    }

    override suspend fun applicationApplyTrigger() {
        _triggerApplicationListScreen.emit(Unit)
    }

    override suspend fun setUserProfile(fullName: String?, joinedAt: Int?) {
        _userFullName.value = fullName
        _userJoinedAt.value = joinedAt

        if (fullName.isNullOrBlank()) {
            localStorage.remove(StorageKeys.USER_FULL_NAME)
        } else {
            localStorage.setString(StorageKeys.USER_FULL_NAME, fullName)
        }

        if (joinedAt == null) {
            localStorage.remove(StorageKeys.USER_JOINED_AT)
        } else {
            localStorage.setInt(StorageKeys.USER_JOINED_AT, joinedAt)
        }
    }

    override suspend fun resetUserPref() {
        _isUserLoggedIn.value = false
        _academicFormSubmitTrigger.value = 0

        _userFullName.value = null
        _userJoinedAt.value = null
        otpAccessToken = null
        localStorage.clear()
    }

}