package com.example.gestiondocumental.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiondocumental.data.repository.AuditoriaRepository
import com.example.gestiondocumental.data.repository.SesionRepository
import com.example.gestiondocumental.model.RegistroAuditoria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AuditoriaViewModel(
    private val sesion: SesionRepository,
    private val repo: AuditoriaRepository
) : ViewModel() {

    private val _registros = MutableStateFlow<List<RegistroAuditoria>>(emptyList())
    val registros: StateFlow<List<RegistroAuditoria>> = _registros.asStateFlow()

    init {
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { repo.observarPara(it) }
                .collect { _registros.value = it }
        }
    }
}
