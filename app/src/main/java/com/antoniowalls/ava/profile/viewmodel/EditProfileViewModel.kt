package com.antoniowalls.ava.profile.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.profile.data.ProfileRepository
import com.antoniowalls.ava.profile.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object LoadingUserProfile : EditProfileUiState()
    data class ProfileLoaded(val user: ChatUser) : EditProfileUiState()
    object UpdatingProfile : EditProfileUiState()
    data class UpdateSuccess(val user: ChatUser, val successMessage: String) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

class EditProfileViewModel(private val application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance())
    private val auth = FirebaseAuth.getInstance()
    private var original: ChatUser? = null

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _fullName = MutableStateFlow("")
    private val _userName = MutableStateFlow("")
    private val _phone = MutableStateFlow("")
    private val _currentImageUrl = MutableStateFlow<String?>(null)
    private val _email = MutableStateFlow("")
    val fullName = _fullName.asStateFlow()
    val userName = _userName.asStateFlow()
    val phone = _phone.asStateFlow()
    val currentImageUrl = _currentImageUrl.asStateFlow()
    val email = _email.asStateFlow()
    val canEdit get() = original?.userid != null && original?.userid == auth.currentUser?.uid

    init { loadUserProfile() }

    fun changeName(value: String) { _fullName.value = value }
    fun changeUsername(value: String) { _userName.value = value }
    fun changePhone(value: String) { _phone.value = value }

    fun loadUserProfile() {
        if (busy) return
        val uid = auth.currentUser?.uid ?: return showError(R.string.profile_sign_in)
        _uiState.value = EditProfileUiState.LoadingUserProfile
        viewModelScope.launch {
            try {
                val user = repository.load(uid)?.copy(userid = uid)
                if (user == null || auth.currentUser?.uid != uid) {
                    showError(R.string.profile_load_error)
                    return@launch
                }
                setOriginal(user)
                _uiState.value = EditProfileUiState.ProfileLoaded(user)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.w("EditProfileViewModel", "Profile load failed", failure)
                showError(R.string.profile_load_error)
            }
        }
    }

    fun updateProfile(newImageUri: Uri? = null, newImageBitmap: Bitmap? = null) {
        if (busy || !canEdit) return
        val loaded = requireNotNull(original)
        val draft = loaded.copy(
            fullName = fullName.value.trim(), userName = userName.value.trim(), phone = phone.value.trim()
        )
        if (draft.fullName.isNullOrBlank() || draft.userName.isNullOrBlank()) {
            showError(R.string.profile_missing_name)
            return
        }
        _uiState.value = EditProfileUiState.UpdatingProfile
        viewModelScope.launch {
            try {
                val updated = if (newImageUri != null || newImageBitmap != null) {
                    draft.copy(imageUrl = repository.upload(loaded.userid, newImageUri, newImageBitmap))
                } else {
                    draft
                }
                if (auth.currentUser?.uid != loaded.userid) return@launch
                if (updated != loaded) repository.save(loaded, updated)
                setOriginal(updated)
                _uiState.value = EditProfileUiState.UpdateSuccess(
                    updated, application.getString(if (updated == loaded) R.string.profile_unchanged else R.string.profile_saved)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.w("EditProfileViewModel", "Profile save failed", failure)
                showError(R.string.profile_save_error)
            }
        }
    }

    private fun setOriginal(user: ChatUser) {
        original = user
        _fullName.value = user.fullName.orEmpty()
        _userName.value = user.userName.orEmpty()
        _phone.value = user.phone.orEmpty()
        _currentImageUrl.value = user.imageUrl
        _email.value = user.userMail.orEmpty()
    }

    private val busy get() = uiState.value == EditProfileUiState.LoadingUserProfile ||
        uiState.value == EditProfileUiState.UpdatingProfile

    private fun showError(resourceId: Int) {
        _uiState.value = EditProfileUiState.Error(application.getString(resourceId))
    }

    fun resetUiStateToProfileLoaded() {
        val current = uiState.value
        if (current is EditProfileUiState.UpdateSuccess) {
            _uiState.value = EditProfileUiState.ProfileLoaded(current.user)
        }
    }

    fun signOut(authViewModel: AuthViewModel) {
        authViewModel.signOut()
    }
}
