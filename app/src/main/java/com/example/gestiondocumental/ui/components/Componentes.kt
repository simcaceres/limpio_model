package com.example.gestiondocumental.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.Rol

/** Marca de la aplicación: círculo blanco con un documento institucional. */
@Composable
fun LogoGestion(modifier: Modifier = Modifier, tamano: Dp = 96.dp) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = "Logo Gestión Documental",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(tamano * 0.55f)
        )
    }
}

/** Etiqueta de color según el estado del documento. */
@Composable
fun BadgeEstado(estado: EstadoDocumento, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        EstadoDocumento.VIGENTE -> Color(0xFFE3F2E5) to Color(0xFF1E6B24)
        EstadoDocumento.PENDIENTE_REVISION -> Color(0xFFFFF3D6) to Color(0xFF8A5A00)
        EstadoDocumento.APROBADO -> Color(0xFFDDEEF9) to Color(0xFF14496B)
        EstadoDocumento.RECHAZADO -> Color(0xFFFBE0DD) to Color(0xFF8C1D18)
        EstadoDocumento.ARCHIVADO -> Color(0xFFE7E9EC) to Color(0xFF454C53)
    }
    Text(
        text = estado.etiqueta,
        color = texto,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Etiqueta del perfil/rol. */
@Composable
fun BadgeRol(rol: Rol, modifier: Modifier = Modifier) {
    Text(
        text = rol.etiqueta,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Categoría documental en texto discreto. */
@Composable
fun EtiquetaCategoria(categoria: CategoriaDocumento, modifier: Modifier = Modifier) {
    Text(
        text = categoria.etiqueta,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
    )
}

/** Barra superior con botón de volver opcional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String, onVolver: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

/** Aviso de éxito o error con cierre opcional. */
@Composable
fun AvisoMensaje(
    mensaje: String,
    esError: Boolean = false,
    onCerrar: (() -> Unit)? = null
) {
    val fondo = if (esError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val colorTexto = if (esError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(fondo, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = mensaje, color = colorTexto, modifier = Modifier.weight(1f))
        if (onCerrar != null) {
            IconButton(onClick = onCerrar) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = colorTexto)
            }
        }
    }
}

/** Estado vacío con ícono y mensaje. */
@Composable
fun EstadoVacio(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
