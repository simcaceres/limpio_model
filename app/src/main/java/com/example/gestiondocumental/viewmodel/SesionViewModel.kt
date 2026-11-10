package com.example.gestiondocumental.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiondocumental.data.repository.SesionRepository
import com.example.gestiondocumental.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SesionViewModel(private val sesion: SesionRepository) : ViewModel() {

    val actual: StateFlow<Usuario?> = sesion.actual

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    fun iniciarSesion(correo: String, clave: String) {
        if (correo.isBlank() || clave.isBlank()) {
            _error.value = "Ingresa tu correo y tu clave."
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            sesion.iniciarSesion(correo, clave)
                .onFailure { _error.value = it.message ?: "No se pudo iniciar sesión." }
            _cargando.value = false
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sesion.cerrarSesion()
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}
