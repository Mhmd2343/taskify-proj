package com.example.taskify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.ui.teacher.TeacherProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val changingPass: Boolean = false,
    val deletingAcc: Boolean = false,

    val username: String = "",

    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",

    val oldPass: String = "",
    val newPass: String = "",
    val confirmPass: String = "",

    val deletePassword: String = "",

    val error: String = "",
    val success: String = ""
)

class TeacherProfileViewModel(
    private val repo: TeacherProfileRepository = TeacherProfileRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(TeacherProfileUiState())
    val ui: StateFlow<TeacherProfileUiState> = _ui

    fun load(username: String) {
        _ui.value = _ui.value.copy(loading = true, error = "", success = "", username = username)
        viewModelScope.launch {
            try {
                val p = repo.loadMyProfile()
                _ui.value = _ui.value.copy(
                    loading = false,
                    firstName = p.firstName,
                    middleName = p.middleName,
                    lastName = p.lastName
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load profile")
            }
        }
    }

    fun clearMessages() {
        _ui.value = _ui.value.copy(error = "", success = "")
    }

    fun setFirstName(v: String) { _ui.value = _ui.value.copy(firstName = v, error = "", success = "") }
    fun setMiddleName(v: String) { _ui.value = _ui.value.copy(middleName = v, error = "", success = "") }
    fun setLastName(v: String) { _ui.value = _ui.value.copy(lastName = v, error = "", success = "") }

    fun setOldPass(v: String) { _ui.value = _ui.value.copy(oldPass = v, error = "", success = "") }
    fun setNewPass(v: String) { _ui.value = _ui.value.copy(newPass = v, error = "", success = "") }
    fun setConfirmPass(v: String) { _ui.value = _ui.value.copy(confirmPass = v, error = "", success = "") }

    fun setDeletePassword(v: String) { _ui.value = _ui.value.copy(deletePassword = v, error = "", success = "") }

    fun saveProfile() {
        val s = _ui.value
        if (s.firstName.trim().isBlank() || s.lastName.trim().isBlank()) {
            _ui.value = s.copy(error = "First name and last name are required", success = "")
            return
        }

        _ui.value = s.copy(saving = true, error = "", success = "")
        viewModelScope.launch {
            try {
                repo.saveMyProfile(s.firstName, s.middleName, s.lastName)
                _ui.value = _ui.value.copy(saving = false, success = "Profile updated ✅")
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.message ?: "Failed to save profile")
            }
        }
    }

    private fun validateChangePass(oldP: String, newP: String, conf: String): String? {
        if (oldP.isBlank() || newP.isBlank() || conf.isBlank()) return "All password fields are required"
        if (newP.length < 6) return "New password must be at least 6 characters"
        if (newP != conf) return "Passwords do not match"
        if (oldP == newP) return "New password must be different from old password"
        return null
    }

    fun changePassword() {
        val s = _ui.value
        val msg = validateChangePass(s.oldPass, s.newPass, s.confirmPass)
        if (msg != null) {
            _ui.value = s.copy(error = msg, success = "")
            return
        }

        _ui.value = s.copy(changingPass = true, error = "", success = "")
        viewModelScope.launch {
            try {
                repo.changePassword(s.oldPass, s.newPass)
                _ui.value = _ui.value.copy(
                    changingPass = false,
                    oldPass = "",
                    newPass = "",
                    confirmPass = "",
                    success = "Password changed successfully ✅"
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(changingPass = false, error = e.message ?: "Failed to change password")
            }
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        val s = _ui.value
        if (s.deletePassword.isBlank()) {
            _ui.value = s.copy(error = "Password is required to delete the account", success = "")
            return
        }

        _ui.value = s.copy(deletingAcc = true, error = "", success = "")
        viewModelScope.launch {
            try {
                repo.deleteAccount(s.deletePassword)
                _ui.value = _ui.value.copy(
                    deletingAcc = false,
                    deletePassword = "",
                    success = "Account deleted ✅"
                )
                onDone()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(deletingAcc = false, error = e.message ?: "Failed to delete account")
            }
        }
    }
}
