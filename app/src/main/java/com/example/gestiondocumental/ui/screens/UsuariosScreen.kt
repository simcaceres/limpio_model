package com.example.gestiondocumental.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.ui.components.AvisoMensaje
import com.example.gestiondocumental.ui.components.BadgeRol
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.viewmodel.UsuariosViewModel

private val Dependencias = listOf(
    "Dirección de Salud",
    "Recursos Humanos",
    "Atención Primaria",
    "Hospital Base"
)

private val Contratos = listOf("INDEFINIDO", "PLAZO_FIJO")

@Composable
fun UsuariosScreen(vm: UsuariosViewModel, onVolver: () -> Unit) {
    val usuarios by vm.usuarios.collectAsState()
    val actor by vm.actor.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    val a = actor
    val puedeGestionar = a != null && Permisos.puedeGestionarUsuarios(a.rol)

    Scaffold(
        topBar = { BarraSuperior("Usuarios", onVolver) },
        floatingActionButton = {
            if (puedeGestionar) {
                FloatingActionButton(onClick = { mostrarDialogo = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo usuario")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (mensaje != null) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    AvisoMensaje(mensaje!!, onCerrar = { vm.limpiarMensaje() })
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuarios, key = { it.id }) { u ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = u.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = u.correo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BadgeRol(u.rol)
                                    Text(
                                        text = if (u.activo) "Activo" else "Inactivo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (u.activo) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                            Switch(
                                checked = u.activo,
                                onCheckedChange = { vm.alternarActivo(u) },
                                enabled = puedeGestionar
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoNuevoUsuario(
            onCrear = { nombre, correo, rut, rol, dependencia, contrato ->
                vm.crear(nombre, correo, rut, rol, dependencia, contrato)
                mostrarDialogo = false
            },
            onCerrar = { mostrarDialogo = false }
        )
    }
}

@Composable
private fun DialogoNuevoUsuario(
    onCrear: (String, String, String, Rol, String, String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(Rol.FUNCIONARIO) }
    var dependencia by remember { mutableStateOf(Dependencias.first()) }
    var contrato by remember { mutableStateOf(Contratos.first()) }
    var menuRol by remember { mutableStateOf(false) }
    var menuDependencia by remember { mutableStateOf(false) }
    var menuContrato by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo usuario") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { menuRol = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Rol: ${rol.etiqueta}") }
                    DropdownMenu(expanded = menuRol, onDismissRequest = { menuRol = false }) {
                        Rol.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.etiqueta) },
                                onClick = {
                                    rol = r
                                    menuRol = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { menuDependencia = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Dependencia: $dependencia") }
                    DropdownMenu(
                        expanded = menuDependencia,
                        onDismissRequest = { menuDependencia = false }
                    ) {
                        Dependencias.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    dependencia = d
                                    menuDependencia = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { menuContrato = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Contrato: $contrato") }
                    DropdownMenu(
                        expanded = menuContrato,
                        onDismissRequest = { menuContrato = false }
                    ) {
                        Contratos.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    contrato = c
                                    menuContrato = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCrear(nombre, correo, rut, rol, dependencia, contrato) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}
