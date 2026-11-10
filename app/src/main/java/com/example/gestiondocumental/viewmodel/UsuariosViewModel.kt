package com.example.gestiondocumental.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiondocumental.data.repository.SesionRepository
import com.example.gestiondocumental.data.repository.UsuarioRepository
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UsuariosViewModel(
    private val repo: UsuarioRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    val actor: StateFlow<Usuario?> = sesion.actual

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { repo.observarPara(it) }
                .collect { _usuarios.value = it }
        }
    }

    fun crear(
        nombre: String,
        correo: String,
        rut: String,
        rol: Rol,
        dependencia: String,
        tipoContrato: String
    ) {
        val a = actor.value ?: return
        viewModelScope.launch {
            repo.crear(nombre, correo, rut, rol, dependencia, tipoContrato, a)
                .onSuccess { _mensaje.value = "Usuario creado. Clave inicial: 1234" }
                .onFailure { _mensaje.value = it.message ?: "No se pudo crear el usuario." }
        }
    }

    fun alternarActivo(usuario: Usuario) {
        val a = actor.value ?: return
        viewModelScope.launch {
            repo.alternarActivo(usuario, a)
                .onFailure { _mensaje.value = it.message ?: "No se pudo actualizar el usuario." }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
