package com.example.gestiondocumental.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.SolicitudModificacion
import com.example.gestiondocumental.ui.components.AvisoMensaje
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.ui.components.EstadoVacio
import com.example.gestiondocumental.ui.components.TarjetaSolicitud
import com.example.gestiondocumental.viewmodel.DocumentosViewModel

@Composable
fun BandejaAprobacionScreen(vm: DocumentosViewModel, onVolver: () -> Unit) {
    val usuario by vm.usuario.collectAsState()
    val pendientes by vm.pendientes.collectAsState()
    val mensaje by vm.mensaje.collectAsState()

    var rechazoPara by remember { mutableStateOf<SolicitudModificacion?>(null) }
    var motivo by remember { mutableStateOf("") }

    val puedeResolver = usuario?.let { Permisos.puedeAprobar(it.rol) } == true

    Scaffold(topBar = { BarraSuperior("Bandeja de aprobación", onVolver) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (mensaje != null) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    AvisoMensaje(mensaje!!, onCerrar = { vm.limpiarMensaje() })
                }
            }
            if (pendientes.isEmpty()) {
                EstadoVacio("No hay solicitudes pendientes de revisión.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pendientes, key = { it.id }) { s ->
                        TarjetaSolicitud(
                            solicitud = s,
                            puedeResolver = puedeResolver,
                            onAprobar = { vm.aprobar(s) },
                            onRechazar = {
                                rechazoPara = s
                                motivo = ""
                            }
                        )
                    }
                }
            }
        }
    }

    val rechazo = rechazoPara
    if (rechazo != null) {
        AlertDialog(
            onDismissRequest = { rechazoPara = null },
            title = { Text("Rechazar solicitud #${rechazo.id}") },
            text = {
                Column {
                    Text(
                        text = "La versión vigente se mantendrá y se notificará al solicitante.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = { Text("Motivo del rechazo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.rechazar(rechazo, motivo)
                    rechazoPara = null
                }) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { rechazoPara = null }) { Text("Cancelar") }
            }
        )
    }
}
