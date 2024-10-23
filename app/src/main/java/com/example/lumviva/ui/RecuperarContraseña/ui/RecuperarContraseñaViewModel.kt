package com.example.lumviva.ui.RecuperarContrasena.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RecuperarContraseñaViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _emailSentStatus = MutableLiveData<RecuperarContraseñaState>()
    val emailSentStatus: LiveData<RecuperarContraseñaState> = _emailSentStatus

    fun recuperarContraseña(email: String) {
        _emailSentStatus.value = RecuperarContraseñaState.Loading

        // Usamos Firebase para enviar el correo de recuperación de contraseña
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _emailSentStatus.value = RecuperarContraseñaState.Success
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    _emailSentStatus.value = RecuperarContraseñaState.Error(errorMessage)
                }
            }
    }
}

sealed class RecuperarContraseñaState {
    object Initial : RecuperarContraseñaState()
    object Loading : RecuperarContraseñaState()
    object Success : RecuperarContraseñaState()
    data class Error(val message: String) : RecuperarContraseñaState()
}
