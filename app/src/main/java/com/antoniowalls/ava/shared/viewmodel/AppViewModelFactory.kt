package com.antoniowalls.ava.shared.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoniowalls.ava.chat.viewmodel.ChatViewModel
import com.antoniowalls.ava.profile.viewmodel.EditProfileViewModel
import com.antoniowalls.ava.feed.viewmodel.CreatePostViewModel // Asegúrate de tener este import

/**
 * Factory para crear instancias de ViewModels que requieren el Application context.
 * Específicamente para ViewModels que heredan de AndroidViewModel.
 */
@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(application) as T
            }
            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
                EditProfileViewModel(application) as T
            }
            // --- AÑADE ESTE CASO ---
            modelClass.isAssignableFrom(CreatePostViewModel::class.java) -> {
                CreatePostViewModel(application) as T
            }
            // --- FIN DEL CASO AÑADIDO ---
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
