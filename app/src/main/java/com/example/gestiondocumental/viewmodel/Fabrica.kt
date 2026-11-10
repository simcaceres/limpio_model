package com.example.gestiondocumental.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fábrica genérica para construir ViewModels con dependencias sin frameworks
 * de inyección (mismo criterio que el proyecto base Lemac DataLab).
 */
class SimpleFactory<T : ViewModel>(private val crear: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = crear() as VM
}
