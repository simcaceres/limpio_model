package com.example.limpio_moviles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.ui.components.SelectorFotoPerfil
import com.example.limpio_moviles.ui.components.colorAmbito
import com.example.limpio_moviles.ui.components.iconoAmbito
import com.example.limpio_moviles.viewmodel.RegistroViewModel

private const val TEXTO_PRIVACIDAD =
    "Entiendo que este MVP usa datos ficticios o sintéticos, no es diagnóstico " +
        "ni tratamiento y no debo ingresar datos reales de salud."

@Composable
fun OnboardingScreen(
    viewModel: RegistroViewModel,
    onFinalizar: () -> Unit
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var aceptaPrivacidad by rememberSaveable { mutableStateOf(false) }
    var errorAlias by rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val mensaje = viewModel.uiState.mensaje

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensaje()
        }
    }

    val aliasValido = alias.trim().length >= 3
    val puedeComenzar = aliasValido && aceptaPrivacidad

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    text = "Lemac DataLab",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tu espacio privado de autorregistro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Registra en pocos pasos cómo te sientes y qué estrategias usas " +
                    "en cuatro ámbitos de tu bienestar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Ambito.entries.forEach { ambito ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = iconoAmbito(ambito),
                                contentDescription = null,
                                tint = colorAmbito(ambito),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ambito.etiqueta,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = ambito.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            SelectorFotoPerfil(
                bitmap = viewModel.fotoPerfil,
                textoBoton = "Añadir foto (opcional)",
                onFotoCapturada = { viewModel.actualizarFoto(it) },
                onPermisoDenegado = {
                    viewModel.mostrarMensaje("Permiso de cámara rechazado")
                }
            )

            OutlinedTextField(
                value = alias,
                onValueChange = { texto ->
                    alias = texto
                    errorAlias = if (texto.isNotBlank() && texto.trim().length < 3) {
                        "El alias debe tener al menos 3 caracteres"
                    } else {
                        null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Alias o seudónimo") },
                placeholder = { Text("Ej.: Luna") },
                singleLine = true,
                isError = errorAlias != null,
                supportingText = {
                    Text(errorAlias ?: "Mínimo 3 caracteres. No uses tu nombre real.")
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .toggleable(
                        value = aceptaPrivacidad,
                        role = Role.Checkbox,
                        onValueChange = { aceptaPrivacidad = it }
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceptaPrivacidad,
                    onCheckedChange = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = TEXTO_PRIVACIDAD,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    viewModel.guardarAlias(alias)
                    onFinalizar()
                },
                enabled = puedeComenzar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Comenzar",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Tus registros se guardan únicamente en este dispositivo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
