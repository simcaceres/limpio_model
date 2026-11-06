package com.example.limpio_moviles.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.limpio_moviles.ui.components.BotonExportarCsv
import com.example.limpio_moviles.ui.components.SelectorFotoPerfil
import com.example.limpio_moviles.viewmodel.RegistroViewModel

private const val TEXTO_PRIVACIDAD_AJUSTES =
    "Esta app es un MVP de autorregistro con datos ficticios o sintéticos. " +
        "No es diagnóstico ni tratamiento y no debes ingresar datos reales de salud. " +
        "La información se guarda únicamente en este dispositivo."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var alias by rememberSaveable { mutableStateOf(viewModel.perfil.alias) }
    var mostrarDialogoBorrado by remember { mutableStateOf(false) }
    val registros by viewModel.registros.collectAsStateWithLifecycle()

    val mensaje = viewModel.uiState.mensaje

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    val notificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            viewModel.activarRecordatorio(true)
        } else {
            viewModel.mostrarMensaje("Permiso de notificaciones rechazado")
        }
    }

    val cambiarRecordatorio: (Boolean) -> Unit = { activo ->
        if (!activo) {
            viewModel.activarRecordatorio(false)
        } else {
            val concedido = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (concedido) {
                viewModel.activarRecordatorio(true)
            } else {
                notificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SeccionAjustes(titulo = "Perfil") {
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alias o seudónimo") },
                    singleLine = true,
                    isError = alias.isNotBlank() && alias.trim().length < 3,
                    supportingText = {
                        Text(
                            if (alias.isNotBlank() && alias.trim().length < 3) {
                                "El alias debe tener al menos 3 caracteres"
                            } else {
                                "Mínimo 3 caracteres"
                            }
                        )
                    }
                )

                Button(
                    onClick = {
                        viewModel.guardarAlias(alias)
                        viewModel.mostrarMensaje("Alias actualizado")
                    },
                    enabled = alias.trim().length >= 3,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar alias")
                }

                SelectorFotoPerfil(
                    bitmap = viewModel.fotoPerfil,
                    textoBoton = "Cambiar foto",
                    onFotoCapturada = { viewModel.actualizarFoto(it) },
                    onCamaraNoDisponible = {
                        viewModel.mostrarMensaje("No se capturo la foto")
                    }
                )
            }

            SeccionAjustes(titulo = "Recordatorios") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recordatorio diario",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Recordatorio diario discreto a las 20:00, " +
                                "sin contenido sensible en pantalla bloqueada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = viewModel.perfil.recordatorioActivo,
                        onCheckedChange = cambiarRecordatorio,
                        modifier = Modifier.semantics {
                            contentDescription = "Activar recordatorio diario"
                        }
                    )
                }
            }

            SeccionAjustes(titulo = "Datos") {
                BotonExportarCsv(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                    hayRegistros = registros.isNotEmpty()
                )

                Text(
                    text = "Guarda un archivo CSV en este dispositivo para " +
                        "analizarlo después. No se envía nada a Internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { viewModel.sincronizar() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar")
                }

                OutlinedButton(
                    onClick = { viewModel.cargarDemo() },
                    enabled = !viewModel.uiState.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cargar datos de ejemplo")
                }

                OutlinedButton(
                    onClick = { mostrarDialogoBorrado = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Borrar todos los registros")
                }
            }

            SeccionAjustes(titulo = "Privacidad") {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = TEXTO_PRIVACIDAD_AJUSTES,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (mostrarDialogoBorrado) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrado = false },
            title = { Text("Borrar todos los registros") },
            text = {
                Text(
                    "Se eliminarán permanentemente todos tus registros en este " +
                        "dispositivo. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoBorrado = false
                        viewModel.borrarTodos()
                    }
                ) {
                    Text(
                        text = "Borrar todo",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SeccionAjustes(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = contenido
            )
        }
    }
}
