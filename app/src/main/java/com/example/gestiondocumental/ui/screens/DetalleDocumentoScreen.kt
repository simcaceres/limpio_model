package com.example.gestiondocumental.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.FlujoModificacion
import com.example.gestiondocumental.model.SolicitudModificacion
import com.example.gestiondocumental.model.TipoCambio
import com.example.gestiondocumental.ui.components.AvisoMensaje
import com.example.gestiondocumental.ui.components.BadgeEstado
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.ui.components.EstadoVacio
import com.example.gestiondocumental.ui.components.EtiquetaCategoria
import com.example.gestiondocumental.ui.components.TarjetaSolicitud
import com.example.gestiondocumental.util.archivoDocumento
import com.example.gestiondocumental.util.mimeDe
import com.example.gestiondocumental.viewmodel.DocumentosViewModel

@Composable
fun DetalleDocumentoScreen(vm: DocumentosViewModel, onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val usuario by vm.usuario.collectAsState()
    val doc by vm.seleccionado.collectAsState()
    val versiones by vm.versiones.collectAsState()
    val solicitudes by vm.solicitudes.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val cargando by vm.cargando.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var textoCambio by remember { mutableStateOf("") }
    var rechazoPara by remember { mutableStateOf<SolicitudModificacion?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }

    val d = doc
    val u = usuario

    Scaffold(topBar = { BarraSuperior(d?.titulo ?: "Documento", onVolver) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (d == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecciona un documento para ver su detalle.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (mensaje != null) {
                        AvisoMensaje(mensaje!!, onCerrar = { vm.limpiarMensaje() })
                    }

                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = d.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                BadgeEstado(d.estado)
                            }
                            Spacer(Modifier.height(8.dp))
                            EtiquetaCategoria(d.categoria)
                            Spacer(Modifier.height(10.dp))
                            FilaDato("Propietario", d.propietario)
                            FilaDato("Dependencia", d.dependencia)
                            FilaDato("Versión actual", "v${d.versionActual}")
                            if (d.fechaVencimiento != null) {
                                FilaDato("Vencimiento", d.fechaVencimiento)
                            }
                            if (d.archivoNombre != null) {
                                FilaDato("Archivo adjunto", d.archivoNombre)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val tipo = FlujoModificacion.tipoCambioPara(d.categoria)
                    Button(
                        onClick = {
                            mostrarDialogo = true
                            textoCambio = ""
                        },
                        enabled = !cargando && u != null,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Solicitar modificación")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (tipo == TipoCambio.DIRECTO) {
                            "Esta categoría permite edición directa: el cambio se aplica al instante y queda auditado."
                        } else {
                            "Esta categoría requiere revisión: la versión actual sigue vigente hasta que RR.HH. o Jefatura apruebe."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val delDocumento = solicitudes.filter { it.documentoId == d.id }
                    if (delDocumento.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Solicitudes de modificación",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        val puedeResolver = u != null && FlujoModificacion.puedeResolver(u.rol)
                        delDocumento.forEach { s ->
                            TarjetaSolicitud(
                                solicitud = s,
                                puedeResolver = puedeResolver,
                                onAprobar = { vm.aprobar(s) },
                                onRechazar = {
                                    rechazoPara = s
                                    motivoRechazo = ""
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                val nombre = d.archivoNombre
                                val archivo = nombre?.let { archivoDocumento(contexto, it) }
                                if (nombre != null && archivo != null && archivo.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        contexto,
                                        "${contexto.packageName}.fileprovider",
                                        archivo
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = mimeDe(nombre)
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    contexto.startActivity(
                                        Intent.createChooser(intent, "Compartir documento")
                                    )
                                } else {
                                    val resumen = buildString {
                                        appendLine("Documento: ${d.titulo}")
                                        appendLine("Propietario: ${d.propietario}")
                                        appendLine("Categoría: ${d.categoria.etiqueta}")
                                        appendLine("Estado: ${d.estado.etiqueta}")
                                        appendLine("Versión: v${d.versionActual}")
                                        appendLine("Vencimiento: ${d.fechaVencimiento ?: "Sin vencimiento"}")
                                        appendLine("Dependencia: ${d.dependencia}")
                                        if (versiones.isNotEmpty()) {
                                            appendLine()
                                            appendLine("Historial de versiones:")
                                            versiones.forEach { v ->
                                                val comentario = if (v.comentario.isNotBlank()) {
                                                    " — ${v.comentario}"
                                                } else {
                                                    ""
                                                }
                                                appendLine("v${v.numeroVersion} · ${v.fecha} · ${v.autor}$comentario")
                                            }
                                        }
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, resumen)
                                    }
                                    contexto.startActivity(
                                        Intent.createChooser(intent, "Compartir documento")
                                    )
                                }
                            }.onFailure {
                                Toast.makeText(
                                    contexto,
                                    "No se pudo compartir el documento.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir / descargar")
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Historial de versiones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    if (versiones.isEmpty()) {
                        EstadoVacio("Sin versiones registradas.")
                    } else {
                        versiones.forEach { v ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = "v${v.numeroVersion} · ${v.autor}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = v.fecha,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (v.comentario.isNotBlank()) {
                                        Text(v.comentario, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo && d != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Solicitar modificación") },
            text = {
                Column {
                    Text(
                        text = "Describe el cambio propuesto para «${d.titulo}».",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textoCambio,
                        onValueChange = { textoCambio = it },
                        label = { Text("Datos propuestos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.solicitarModificacion(textoCambio)
                    mostrarDialogo = false
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    val rechazo = rechazoPara
    if (rechazo != null) {
        AlertDialog(
            onDismissRequest = { rechazoPara = null },
            title = { Text("Rechazar solicitud #${rechazo.id}") },
            text = {
                Column {
                    Text(
                        text = "Indica el motivo. La versión vigente se mantendrá y se notificará al solicitante.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Motivo del rechazo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.rechazar(rechazo, motivoRechazo)
                    rechazoPara = null
                }) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { rechazoPara = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
