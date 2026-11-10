package com.example.limpio_moviles.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pantalla portal de la app unificada: punto de entrada que presenta los
 * dos módulos disponibles (Bienestar y Gestión Documental) sin mezclar sus
 * datos ni su navegación interna.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalScreen(
    onBienestar: () -> Unit,
    onGestion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Centro de aplicaciones")
                        Text(
                            text = "Elige el módulo con el que quieres trabajar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaModulo(
                titulo = "Bienestar — Lemac DataLab",
                subtitulo = "Autorregistro diario de los 4 ámbitos de bienestar",
                icono = Icons.Filled.Favorite,
                descripcionAccesible = "Abrir módulo de bienestar Lemac DataLab",
                onClick = onBienestar
            )

            TarjetaModulo(
                titulo = "Gestión Documental RR.HH.",
                subtitulo = "Documentos, roles y auditoría del flujo de aprobación",
                icono = Icons.Filled.Folder,
                descripcionAccesible = "Abrir módulo de gestión documental de recursos humanos",
                onClick = onGestion
            )

            Spacer(modifier = Modifier.height(8.dp))

            AvisoPrivacidad()
        }
    }
}

@Composable
private fun TarjetaModulo(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    descripcionAccesible: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcionAccesible }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AvisoPrivacidad() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Aviso: ambos módulos operan con datos ficticios o simulados, " +
                    "con fines académicos y de demostración. No se registran datos personales reales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
