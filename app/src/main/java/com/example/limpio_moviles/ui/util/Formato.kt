package com.example.limpio_moviles.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatea una marca de tiempo local para mostrarla en listados y tarjetas. */
fun formatearFecha(fechaHora: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaHora))
