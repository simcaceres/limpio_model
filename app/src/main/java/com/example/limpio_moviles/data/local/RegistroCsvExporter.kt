package com.example.limpio_moviles.data.local

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RegistroCsvExporter {

    private const val BOM = "\uFEFF"
    private const val SALTO_LINEA = "\r\n"
    private const val PREFIJOS_FORMULA = "=+-@\t\r"
    private const val CABECERA =
        "id,alias,ambito,tipo,intensidad,contexto,estrategia,nota,objetivo,fecha_hora_iso,fecha_hora_millis,sincronizado"

    private val FORMATO_NOMBRE: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault())

    fun generar(registros: List<RegistroEntity>): String {
        val contenido = StringBuilder(BOM)
        contenido.append(CABECERA).append(SALTO_LINEA)

        registros.forEach { registro ->
            contenido
                .append(registro.id).append(',')
                .append(escapar(registro.usuarioAlias)).append(',')
                .append(escapar(registro.ambito)).append(',')
                .append(escapar(registro.tipo)).append(',')
                .append(registro.intensidad).append(',')
                .append(escapar(registro.contexto)).append(',')
                .append(escapar(registro.estrategia)).append(',')
                .append(escapar(registro.nota)).append(',')
                .append(escapar(registro.estadoObjetivo)).append(',')
                .append(Instant.ofEpochMilli(registro.fechaHora).toString()).append(',')
                .append(registro.fechaHora).append(',')
                .append(registro.sincronizado.toString())
                .append(SALTO_LINEA)
        }

        return contenido.toString()
    }

    fun nombreArchivo(millis: Long = System.currentTimeMillis()): String {
        val marcaTiempo = FORMATO_NOMBRE.format(Instant.ofEpochMilli(millis))
        return "lemac_datalab_$marcaTiempo.csv"
    }

    private fun escapar(campo: String): String {
        // Evita que Excel/Sheets interpreten como formula un texto que empieza
        // por =, +, -, @, tabulador o retorno de carro.
        val neutralizado = if (campo.isNotEmpty() && campo[0] in PREFIJOS_FORMULA) {
            "'$campo"
        } else {
            campo
        }
        val requiereComillas = neutralizado.any { caracter ->
            caracter == ',' || caracter == '"' || caracter == '\n' || caracter == '\r'
        }
        if (!requiereComillas) return neutralizado
        return "\"" + neutralizado.replace("\"", "\"\"") + "\""
    }
}
