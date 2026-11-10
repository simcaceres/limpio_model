package com.example.gestiondocumental.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.Reportes
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.ui.components.BarraSuperior
import com.example.gestiondocumental.ui.components.EstadoVacio
import com.example.gestiondocumental.viewmodel.DocumentosViewModel
import java.time.LocalDate

private val RolesReportes = setOf(
    Rol.RRHH,
    Rol.ADMIN_SISTEMA,
    Rol.SUPERVISOR,
    Rol.JEFATURA
)

@Composable
fun ReportesScreen(vm: DocumentosViewModel, onVolver: () -> Unit) {
    val usuario by vm.usuario.collectAsState()
    val documentos by vm.documentos.collectAsState()

    val u = usuario
    if (u == null || u.rol !in RolesReportes) {
        Scaffold(topBar = { BarraSuperior("Reportes", onVolver) }) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                EstadoVacio("Tu perfil no tiene acceso a reportes institucionales.")
            }
        }
        return
    }

    val hoy = remember { LocalDate.now() }
    val ind = remember(documentos) { Reportes.indicadores(documentos, hoy) }
    val porVencer = remember(documentos) { Reportes.documentosPorVencer(documentos, hoy, 60) }

    Scaffold(topBar = { BarraSuperior("Reportes", onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaIndicador("Total", ind.total, Modifier.weight(1f))
                    TarjetaIndicador(
                        titulo = "Vencidos",
                        valor = ind.vencimientos.vencidos,
                        modifier = Modifier.weight(1f),
                        colorValor = MaterialTheme.colorScheme.error
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaIndicador(
                        titulo = "Por vencer ≤30",
                        valor = ind.vencimientos.porVencer30,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaIndicador(
                        titulo = "Por vencer ≤60",
                        valor = ind.vencimientos.porVencer60,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Seccion("Documentos por estado") {
                EstadoDocumento.entries.forEach { estado ->
                    FilaReporte(
                        etiqueta = estado.etiqueta,
                        valor = ind.porEstado[estado] ?: 0
                    )
                }
            }

            Seccion("Documentos por categoría") {
                CategoriaDocumento.entries.forEach { categoria ->
                    FilaReporte(
                        etiqueta = categoria.etiqueta,
                        valor = ind.porCategoria[categoria] ?: 0
                    )
                }
            }

            Seccion("Próximos vencimientos (60 días)") {
                if (porVencer.isEmpty()) {
                    EstadoVacio("No hay documentos por vencer en los próximos 60 días.")
                } else {
                    porVencer.forEach { documento ->
                        TarjetaVencimiento(documento, hoy)
                    }
                }
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        contenido()
    }
}

@Composable
private fun TarjetaIndicador(
    titulo: String,
    valor: Int,
    modifier: Modifier = Modifier,
    colorValor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorValor
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
private fun FilaReporte(etiqueta: String, valor: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TarjetaVencimiento(documento: Documento, hoy: LocalDate) {
    val dias = Reportes.diasHastaVencimiento(documento.fechaVencimiento, hoy)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = documento.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${documento.propietario} · ${documento.dependencia}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${documento.fechaVencimiento} · vence en ${dias ?: "?"} días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
