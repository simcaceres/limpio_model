package com.example.limpio_moviles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.model.Catalogos
import com.example.limpio_moviles.ui.components.SelectorIntensidad
import com.example.limpio_moviles.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    ambito: Ambito,
    id: Int?,
    viewModel: RegistroViewModel,
    onVolver: () -> Unit
) {
    LaunchedEffect(ambito, id) {
        viewModel.prepararFormulario(ambito, id)
    }
    LaunchedEffect(Unit) {
        viewModel.limpiarMensaje()
    }

    val uiState = viewModel.uiState
    val rango = Catalogos.rangoIntensidad(ambito)

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            delay(900)
            viewModel.limpiarFormulario()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro: ${ambito.etiqueta}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tipo de registro",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = Catalogos.tipos(ambito)) { tipo ->
                    val seleccionado = uiState.tipo == tipo
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.actualizarTipo(tipo) },
                        label = { Text(tipo) },
                        leadingIcon = if (seleccionado) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            uiState.errores["tipo"]?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            SelectorIntensidad(
                valor = uiState.intensidad,
                rango = rango,
                onValorCambio = viewModel::actualizarIntensidad,
                error = uiState.errores["intensidad"]
            )

            OutlinedTextField(
                value = uiState.contexto,
                onValueChange = viewModel::actualizarContexto,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Catalogos.etiquetaContexto(ambito)) },
                singleLine = true,
                isError = uiState.errores["contexto"] != null,
                supportingText = {
                    val error = uiState.errores["contexto"]
                    Text(error ?: "Describe brevemente la situación")
                }
            )

            OutlinedTextField(
                value = uiState.estrategia,
                onValueChange = viewModel::actualizarEstrategia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Catalogos.etiquetaEstrategia(ambito)) },
                singleLine = true,
                isError = uiState.errores["estrategia"] != null,
                supportingText = {
                    val error = uiState.errores["estrategia"]
                    Text(error ?: "¿Qué te ayudó o qué practicaste?")
                }
            )

            if (ambito == Ambito.ADICCIONES) {
                OutlinedTextField(
                    value = uiState.estadoObjetivo,
                    onValueChange = viewModel::actualizarEstadoObjetivo,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Estado del objetivo") },
                    placeholder = { Text("Ej.: mantener la abstinencia hoy") },
                    singleLine = true,
                    supportingText = {
                        Text("Opcional. Tu meta o estado actual del proceso.")
                    }
                )
            }

            OutlinedTextField(
                value = uiState.nota,
                onValueChange = { texto ->
                    if (texto.length <= 200) {
                        viewModel.actualizarNota(texto)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Catalogos.etiquetaNota(ambito)) },
                minLines = 3,
                maxLines = 6,
                isError = uiState.errores["nota"] != null,
                supportingText = {
                    val error = uiState.errores["nota"]
                    Text(
                        text = if (error != null) {
                            "${uiState.nota.length}/200 · $error"
                        } else {
                            "${uiState.nota.length}/200 caracteres"
                        }
                    )
                }
            )

            AnimatedVisibility(visible = uiState.guardadoExitoso) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Registro guardado correctamente",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!uiState.guardadoExitoso) {
                uiState.mensaje?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = viewModel::guardar,
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text(
                        text = "Guardar registro",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
