package com.example.gestiondocumental.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.ui.components.BadgeRol
import com.example.gestiondocumental.util.NotificacionHelper
import com.example.gestiondocumental.viewmodel.DocumentosViewModel
import com.example.gestiondocumental.viewmodel.SesionViewModel

@Composable
fun HomeScreen(
    sesionVm: SesionViewModel,
    docsVm: DocumentosViewModel,
    onDocumentos: () -> Unit,
    onBandeja: () -> Unit,
    onAuditoria: () -> Unit,
    onUsuarios: () -> Unit,
    onReportes: () -> Unit,
    onSalir: () -> Unit
) {
    val contexto = LocalContext.current
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificacionHelper.puedeNotificar(contexto)
        ) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val usuario by sesionVm.actual.collectAsState()
    val documentos by docsVm.documentos.collectAsState()
    val pendientes by docsVm.pendientes.collectAsState()

    val u = usuario
    if (u == null) {
        LaunchedEffect(Unit) { onSalir() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val visibles = remember(documentos, u) {
        documentos.filter { d ->
            Permisos.puedeVerDocumento(
                rol = u.rol,
                categoria = d.categoria,
                propio = d.usuarioId == u.id,
                mismaDependencia = d.dependencia == u.dependencia
            )
        }
    }
    val misDocumentos = remember(documentos, u) {
        documentos.filter { it.usuarioId == u.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Hola, ${u.nombre.substringBefore(' ')}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeRol(u.rol)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = u.dependencia,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                TarjetaResumen("Visibles", visibles.size.toString(), Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                TarjetaResumen("Mis documentos", misDocumentos.size.toString(), Modifier.weight(1f))
                if (Permisos.puedeAprobar(u.rol)) {
                    Spacer(Modifier.width(12.dp))
                    TarjetaResumen("Pendientes", pendientes.size.toString(), Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Accesos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            AccesoCard(
                icono = Icons.Filled.Folder,
                titulo = "Documentos",
                subtitulo = "Consulta y gestiona expedientes",
                onClick = onDocumentos
            )
            if (Permisos.puedeAprobar(u.rol)) {
                AccesoCard(
                    icono = Icons.Filled.Inbox,
                    titulo = "Bandeja de aprobación",
                    subtitulo = "${pendientes.size} solicitud(es) pendiente(s)",
                    onClick = onBandeja
                )
            }
            if (Permisos.puedeAuditar(u.rol)) {
                AccesoCard(
                    icono = Icons.Filled.Security,
                    titulo = "Auditoría",
                    subtitulo = "Bitácora de accesos y cambios",
                    onClick = onAuditoria
                )
            }
            if (Permisos.puedeGestionarUsuarios(u.rol)) {
                AccesoCard(
                    icono = Icons.Filled.People,
                    titulo = "Usuarios",
                    subtitulo = "Cuentas, roles y permisos",
                    onClick = onUsuarios
                )
            }
            if (u.rol in setOf(Rol.RRHH, Rol.ADMIN_SISTEMA, Rol.SUPERVISOR, Rol.JEFATURA)) {
                AccesoCard(
                    icono = Icons.Filled.Assessment,
                    titulo = "Reportes y vencimientos",
                    subtitulo = "Indicadores y control de vencimientos",
                    onClick = onReportes
                )
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    sesionVm.cerrarSesion()
                    onSalir()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun TarjetaResumen(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccesoCard(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
