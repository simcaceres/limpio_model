package com.example.gestiondocumental.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.Usuario
import com.example.gestiondocumental.ui.components.AvisoMensaje
import com.example.gestiondocumental.ui.components.BadgeEstado
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.ui.components.EstadoVacio
import com.example.gestiondocumental.ui.components.EtiquetaCategoria
import com.example.gestiondocumental.util.copiarArchivo
import com.example.gestiondocumental.util.nombreVisible
import com.example.gestiondocumental.viewmodel.DocumentosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val RolesCarga = setOf(
    Rol.RRHH,
    Rol.ADMIN_SISTEMA,
    Rol.ADMINISTRATIVO,
    Rol.JEFATURA
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    vm: DocumentosViewModel,
    onAbrir: (Documento) -> Unit,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val usuario by vm.usuario.collectAsState()
    val documentos by vm.documentos.collectAsState()
    val usuarios by vm.usuarios.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val cargando by vm.cargando.collectAsState()

    var filtro by remember { mutableStateOf<CategoriaDocumento?>(null) }
    var mostrarCarga by remember { mutableStateOf(false) }

    val u = usuario
    val visibles = remember(documentos, u, filtro) {
        if (u == null) {
            emptyList()
        } else {
            documentos.filter { d ->
                Permisos.puedeVerDocumento(
                    rol = u.rol,
                    categoria = d.categoria,
                    propio = d.usuarioId == u.id,
                    mismaDependencia = d.dependencia == u.dependencia
                ) && (filtro == null || d.categoria == filtro)
            }
        }
    }

    val categoriasCargables = remember(u) {
        if (u == null) {
            emptyList()
        } else {
            val esRolAdministrativo = u.rol in RolesCarga
            CategoriaDocumento.entries.filter { cat ->
                Permisos.puedeCargarDocumento(
                    rol = u.rol,
                    categoria = cat,
                    propio = true,
                    mismaDependencia = true
                ) || (
                    esRolAdministrativo && Permisos.puedeCargarDocumento(
                        rol = u.rol,
                        categoria = cat,
                        propio = false,
                        mismaDependencia = true
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Documentos", onVolver) },
        floatingActionButton = {
            if (u != null && categoriasCargables.isNotEmpty()) {
                FloatingActionButton(onClick = { mostrarCarga = true }) {
                    Icon(Icons.Filled.UploadFile, contentDescription = "Cargar documento")
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filtro == null,
                        onClick = { filtro = null },
                        label = { Text("Todos") }
                    )
                }
                CategoriaDocumento.entries.forEach { cat ->
                    item {
                        FilterChip(
                            selected = filtro == cat,
                            onClick = { filtro = cat },
                            label = { Text(cat.etiqueta) }
                        )
                    }
                }
            }

            if (visibles.isEmpty()) {
                EstadoVacio("No hay documentos visibles para tu perfil.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibles, key = { it.id }) { doc ->
                        TarjetaDocumento(doc, onClick = { onAbrir(doc) })
                    }
                }
            }
        }
    }

    if (mostrarCarga && u != null) {
        DialogoCargaDocumento(
            usuarioActual = u,
            usuarios = usuarios,
            categorias = categoriasCargables,
            cargando = cargando,
            onCerrar = { mostrarCarga = false },
            onConfirmar = { propietario, categoria, titulo, fecha, uri ->
                alcance.launch {
                    val nombre = uri?.let {
                        withContext(Dispatchers.IO) { copiarArchivo(contexto, it) }
                    }
                    vm.cargarDocumento(
                        propietario = propietario,
                        categoria = categoria,
                        titulo = titulo,
                        fechaVencimiento = fecha.takeIf { it.isNotBlank() },
                        archivoNombre = nombre
                    )
                    mostrarCarga = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCargaDocumento(
    usuarioActual: Usuario,
    usuarios: List<Usuario>,
    categorias: List<CategoriaDocumento>,
    cargando: Boolean,
    onCerrar: () -> Unit,
    onConfirmar: (Usuario, CategoriaDocumento, String, String, Uri?) -> Unit
) {
    val contexto = LocalContext.current
    val esRolAdministrativo = usuarioActual.rol in RolesCarga

    var propietario by remember { mutableStateOf(usuarioActual) }
    var categoria by remember { mutableStateOf(categorias.firstOrNull()) }
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var archivoUri by remember { mutableStateOf<Uri?>(null) }
    var archivoNombre by remember { mutableStateOf<String?>(null) }
    var menuPropietario by remember { mutableStateOf(false) }
    var menuCategoria by remember { mutableStateOf(false) }

    val selectorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            archivoUri = uri
            archivoNombre = nombreVisible(contexto, uri)
        }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Cargar documento") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (esRolAdministrativo) {
                    Box {
                        OutlinedButton(
                            onClick = { menuPropietario = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Propietario: ${propietario.nombre}") }
                        DropdownMenu(
                            expanded = menuPropietario,
                            onDismissRequest = { menuPropietario = false }
                        ) {
                            usuarios.forEach { candidato ->
                                DropdownMenuItem(
                                    text = { Text("${candidato.nombre} · ${candidato.dependencia}") },
                                    onClick = {
                                        propietario = candidato
                                        menuPropietario = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Propietario: ${usuarioActual.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { menuCategoria = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Categoría: ${categoria?.etiqueta ?: "Sin categoría"}") }
                    DropdownMenu(
                        expanded = menuCategoria,
                        onDismissRequest = { menuCategoria = false }
                    ) {
                        categorias.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.etiqueta) },
                                onClick = {
                                    categoria = opcion
                                    menuCategoria = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Vencimiento (AAAA-MM-DD, opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { selectorArchivo.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar archivo")
                }
                Text(
                    text = archivoNombre ?: "Sin archivo adjunto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    categoria?.let { onConfirmar(propietario, it, titulo, fecha, archivoUri) }
                },
                enabled = titulo.isNotBlank() && !cargando && categoria != null
            ) {
                Text(if (cargando) "Cargando…" else "Cargar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun TarjetaDocumento(doc: Documento, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = doc.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                BadgeEstado(doc.estado)
            }
            Spacer(Modifier.height(6.dp))
            EtiquetaCategoria(doc.categoria)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${doc.propietario} · v${doc.versionActual} · ${doc.dependencia}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (doc.fechaVencimiento != null) {
                Text(
                    text = "Vence: ${doc.fechaVencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
