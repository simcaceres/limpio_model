package com.example.gestiondocumental.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))

/** Fecha y hora actuales en formato legible para Chile. */
fun ahora(): String = LocalDateTime.now().format(FORMATO)
