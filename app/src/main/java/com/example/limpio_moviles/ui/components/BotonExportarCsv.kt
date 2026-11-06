package com.example.limpio_moviles.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.limpio_moviles.viewmodel.RegistroViewModel

/**
 * Botón para exportar los registros locales a un archivo CSV.
 *
 * Abre el selector de documentos del sistema para elegir dónde guardar el
 * archivo. Si el usuario cancela, se muestra un mensaje discreto. Mientras el
 * ViewModel está cargando (por ejemplo, sincronizando o exportando) el botón
 * queda deshabilitado para evitar acciones duplicadas.
 */
@Composable
fun BotonExportarCsv(
    viewModel: RegistroViewModel,
    modifier: Modifier = Modifier,
    primario: Boolean = false,
    hayRegistros: Boolean = true,
    texto: String = "Exportar datos (CSV)"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportarCsv(uri)
        } else {
            viewModel.mostrarMensaje("Exportación cancelada")
        }
    }

    val onClick: () -> Unit = {
        runCatching { launcher.launch(viewModel.nombreArchivoCsv()) }
            .onFailure {
                viewModel.mostrarMensaje("No se pudo abrir el selector de archivos")
            }
    }
    val habilitado = !viewModel.uiState.cargando && hayRegistros

    if (primario) {
        Button(
            onClick = onClick,
            enabled = habilitado,
            modifier = modifier
        ) {
            ContenidoBoton(texto)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = habilitado,
            modifier = modifier
        ) {
            ContenidoBoton(texto)
        }
    }
}

@Composable
private fun ContenidoBoton(texto: String) {
    Icon(
        imageVector = Icons.Default.Download,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
    )
    Spacer(Modifier.width(8.dp))
    Text(texto)
}
