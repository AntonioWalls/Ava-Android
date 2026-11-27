package com.antoniowalls.ava.auth.viewmodel

import com.antoniowalls.ava.R

import android.util.Log
import android.util.Patterns
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.ava.profile.model.ChatUser
import com.antoniowalls.ava.profile.data.directoryFields
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
        data class Authenticated(
                val isNewUser: Boolean = false
        ) : AuthState()

        object Unauthenticated : AuthState()
        object Loading : AuthState()
}

data class AuthUiState(
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val noticeMessage: String? = null
)

class AuthViewModel(private val application: Application) : AndroidViewModel(application) {

        private val auth = FirebaseAuth.getInstance()
        private val firestore = FirebaseFirestore.getInstance()

        private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
        val authState: StateFlow<AuthState> = _authState.asStateFlow()

        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        private var signUpInProgress = false

        private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser

                when {
                        currentUser == null && !signUpInProgress -> {
                                _authState.value = AuthState.Unauthenticated
                        }

                        currentUser != null && !signUpInProgress -> {
                                _authState.value = AuthState.Authenticated(isNewUser = false)
                        }
                }
        }

        init {
                auth.addAuthStateListener(authStateListener)
        }

        fun login(email: String, password: String) {
                val normalizedEmail = email.trim()

                when {
                        normalizedEmail.isBlank() || password.isBlank() -> {
                                showError(application.getString(R.string.auth_login_required))
                                return
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                                showError(application.getString(R.string.auth_invalid_email))
                                return
                        }
                }

                _uiState.value = AuthUiState(isSubmitting = true)
                _authState.value = AuthState.Loading

                viewModelScope.launch {
                        try {
                                val result = auth
                                        .signInWithEmailAndPassword(normalizedEmail, password)
                                        .await()

                                updateUserStatus(result.user?.uid, "Online")

                                _uiState.value = AuthUiState()
                                _authState.value = AuthState.Authenticated(isNewUser = false)
                        } catch (exception: Exception) {
                                Log.e("AuthViewModel", "No se pudo iniciar sesión", exception)

                                _authState.value = AuthState.Unauthenticated
                                showError(loginErrorMessage(exception))
                        }
                }
        }

        fun signup(
                email: String,
                password: String,
                fullName: String,
                username: String,
                phone: String
        ) {
                val normalizedEmail = email.trim()
                val normalizedFullName = fullName.trim()
                val normalizedUsername = username.trim()
                val normalizedPhone = phone.trim()

                when {
                        normalizedEmail.isBlank() ||
                                password.isBlank() ||
                                normalizedFullName.isBlank() ||
                                normalizedUsername.isBlank() -> {
                                showError(
                                        application.getString(R.string.auth_signup_required)
                                )
                                return
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                                showError(application.getString(R.string.auth_invalid_email))
                                return
                        }

                        password.length < 6 -> {
                                showError(application.getString(R.string.auth_short_password))
                                return
                        }
                }

                signUpInProgress = true
                _uiState.value = AuthUiState(isSubmitting = true)
                _authState.value = AuthState.Loading

                viewModelScope.launch {
                        var createdUser: FirebaseUser? = null

                        try {
                                val result = auth
                                        .createUserWithEmailAndPassword(normalizedEmail, password)
                                        .await()

                                createdUser = result.user
                                        ?: error("Firebase no devolvió el usuario recién creado.")

                                val userProfile = ChatUser(
                                        userid = createdUser.uid,
                                        userMail = createdUser.email ?: normalizedEmail.lowercase(),
                                        fullName = normalizedFullName,
                                        userName = normalizedUsername,
                                        phone = normalizedPhone,
                                        status = "Online",
                                        imageUrl = ""
                                )

                                firestore.runBatch { batch ->
                                        batch.set(firestore.collection("Users").document(userProfile.userid), userProfile)
                                        batch.set(
                                                firestore.collection("PublicProfiles").document(userProfile.userid),
                                                userProfile.directoryFields()
                                        )
                                }.await()

                                _uiState.value = AuthUiState()
                                _authState.value = AuthState.Authenticated(isNewUser = true)
                        } catch (exception: Exception) {
                                Log.e("AuthViewModel", "No se pudo completar el registro", exception)

                                if (createdUser != null) {
                                        runCatching {
                                                createdUser.delete().await()
                                        }.onFailure {
                                                Log.e(
                                                        "AuthViewModel",
                                                        "Could not remove incomplete account",
                                                        it
                                                )
                                        }
                                }

                                auth.signOut()
                                _authState.value = AuthState.Unauthenticated
                                showError(signupErrorMessage(exception))
                        } finally {
                                signUpInProgress = false
                        }
                }
        }

        fun sendPasswordResetEmail(email: String) {
                val normalizedEmail = email.trim()

                when {
                        normalizedEmail.isBlank() -> {
                                showError(application.getString(R.string.auth_reset_email_required))
                                return
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                                showError(application.getString(R.string.auth_invalid_email))
                                return
                        }
                }

                _uiState.value = AuthUiState(isSubmitting = true)

                viewModelScope.launch {
                        try {
                                auth.sendPasswordResetEmail(normalizedEmail).await()

                                _uiState.value = AuthUiState(
                                        noticeMessage = application.getString(R.string.auth_reset_sent)
                                )
                        } catch (exception: Exception) {
                                Log.e(
                                        "AuthViewModel",
                                        "Password reset request failed",
                                        exception
                                )

                                showError(passwordResetErrorMessage(exception))
                        }
                }
        }

        fun clearFeedback() {
                _uiState.value = _uiState.value.copy(
                        errorMessage = null,
                        noticeMessage = null
                )
        }

        fun signOut() {
                updateUserStatus(auth.currentUser?.uid, "Offline")
                auth.signOut()

                _uiState.value = AuthUiState()
                _authState.value = AuthState.Unauthenticated
        }

        private fun updateUserStatus(userId: String?, status: String) {
                if (userId == null) return

                firestore
                        .collection("Users")
                        .document(userId)
                        .update("status", status)
                        .addOnFailureListener { exception ->
                                Log.w(
                                        "AuthViewModel",
                                        "Could not update account status",
                                        exception
                                )
                        }
        }

        private fun showError(message: String) {
                _uiState.value = AuthUiState(errorMessage = message)
        }

        private fun loginErrorMessage(exception: Exception): String {
                return when (exception) {
                        is FirebaseAuthInvalidUserException ->
                                application.getString(R.string.auth_account_missing)

                        is FirebaseAuthInvalidCredentialsException ->
                                application.getString(R.string.auth_invalid_credentials)

                        is FirebaseNetworkException ->
                                application.getString(R.string.auth_network_error)

                        else ->
                                application.getString(R.string.auth_login_failed)
                }
        }

        private fun signupErrorMessage(exception: Exception): String {
                return when (exception) {
                        is FirebaseAuthUserCollisionException ->
                                application.getString(R.string.auth_account_exists)

                        is FirebaseAuthWeakPasswordException ->
                                application.getString(R.string.auth_weak_password)

                        is FirebaseAuthInvalidCredentialsException ->
                                application.getString(R.string.auth_invalid_email)

                        is FirebaseNetworkException ->
                                application.getString(R.string.auth_network_error)

                        else ->
                                application.getString(R.string.auth_signup_failed)
                }
        }

        private fun passwordResetErrorMessage(exception: Exception): String {
                return when (exception) {
                        is FirebaseAuthInvalidUserException ->
                                application.getString(R.string.auth_account_missing)

                        is FirebaseAuthInvalidCredentialsException ->
                                application.getString(R.string.auth_invalid_email)

                        is FirebaseNetworkException ->
                                application.getString(R.string.auth_network_error)

                        else ->
                                application.getString(R.string.auth_reset_failed)
                }
        }

        override fun onCleared() {
                auth.removeAuthStateListener(authStateListener)
                super.onCleared()
        }
}
