package com.example.limpio_moviles.model

object Validadores {

    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun correoValido(correo: String): Boolean = CORREO.matches(correo.trim())

    fun requerido(valor: String): Boolean = valor.isNotBlank()

    /** Validación básica de RUT chileno. */
    fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 8) return false
        val cuerpo = limpio.dropLast(1)
        return cuerpo.all { it.isDigit() }
    }

    /** RUT canónico para almacenar/comparar. */
    fun rutCanonico(rut: String): String {
        val limpio = rut.replace(".", "").replace(" ", "").replace("-", "")
        if (limpio.length < 2) return limpio
        return limpio.dropLast(1) + "-" + limpio.last()
    }

    /** Valida los campos mínimos para crear un usuario. */
    fun usuarioValido(nombre: String, correo: String, rut: String): String? = when {
        !requerido(nombre) -> "El nombre es obligatorio."
        !correoValido(correo) -> "El correo no es válido."
        !rutValido(rut) -> "El RUT no es válido."
        else -> null
    }
}