package com.example.gestiondocumental.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondocumental.data.remote.ApiConfig
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.DocumentosRemotosUiState
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.Usuario
import com.example.gestiondocumental.ui.components.BadgeEstado
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.ui.components.EstadoVacio
import com.example.gestiondocumental.ui.components.EtiquetaCategoria
import com.example.gestiondocumental.viewmodel.DocumentosRemotosViewModel

/**
 * Pantalla que muestra los documentos obtenidos de la API REST simulada.
 *
 * Refleja el [DocumentosRemotosViewModel] en tres estados (cargando, datos y
 * error), permite recargar manualmente desde la barra superior y ofrece un
 * botón de reintento cuando la consulta falla. Sobre los datos recibidos
 * aplica la matriz de permisos del dominio ([Permisos.puedeVerDocumento]) para
 * mostrar únicamente los documentos visibles según el rol del usuario.
 */
@Composable
fun DocumentosRemotosScreen(
    vm: DocumentosRemotosViewModel,
    usuario: Usuario?,
    onVolver: () -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    val datos = estado as? DocumentosRemotosUiState.Datos
    val visibles: List<Documento> = remember(datos?.documentos, usuario) {
        val documentos = datos?.documentos ?: return@remember emptyList()
        val u = usuario ?: return@remember emptyList()
        documentos.filter { d ->
            Permisos.puedeVerDocumento(
                rol = u.rol,
                categoria = d.categoria,
                propio = d.usuarioId == u.id,
                mismaDependencia = d.dependencia == u.dependencia
            )
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior("Documentos (API)", onVolver) {
                IconButton(onClick = { vm.cargar() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Endpoint: GET /${ApiConfig.ENDPOINT_DOCUMENTOS}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Servidor: ${ApiConfig.BASE_URL}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (datos != null) {
                        Text(
                            text = "Documentos recibidos: ${datos.documentos.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Visibles para tu rol: ${visibles.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            when (val actual = estado) {
                DocumentosRemotosUiState.Cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Cargando documentos...")
                        }
                    }
                }

                is DocumentosRemotosUiState.Datos -> {
                    when {
                        usuario == null -> EstadoVacio("No hay una sesión activa.")
                        visibles.isEmpty() ->
                            EstadoVacio("No tienes documentos visibles según tu rol.")
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(visibles, key = { it.id }) { documento ->
                                    TarjetaDocumentoRemoto(documento)
                                }
                            }
                        }
                    }
                }

                is DocumentosRemotosUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = actual.mensaje,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Revisa que la API simulada esté corriendo " +
                                    "(python3 tools/mock_api/server.py).",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.cargar() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de un documento remoto con su categoría, estado y metadatos. */
@Composable
private fun TarjetaDocumentoRemoto(documento: Documento) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = documento.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EtiquetaCategoria(documento.categoria)
                Spacer(Modifier.width(8.dp))
                BadgeEstado(documento.estado)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Propietario: ${documento.propietario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dependencia: ${documento.dependencia}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Versión: ${documento.versionActual}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (documento.fechaVencimiento != null) {
                Text(
                    text = "Vence: ${documento.fechaVencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
