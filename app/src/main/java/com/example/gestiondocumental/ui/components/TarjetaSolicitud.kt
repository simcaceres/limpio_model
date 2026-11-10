package com.example.gestiondocumental.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.SolicitudModificacion

/** Tarjeta de una solicitud de modificación (RF12), reutilizable en detalle y bandeja. */
@Composable
fun TarjetaSolicitud(
    solicitud: SolicitudModificacion,
    puedeResolver: Boolean,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${solicitud.id} · ${solicitud.solicitante}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                BadgeSolicitud(solicitud.estado)
            }
            Text(
                text = "${solicitud.fecha} · ${solicitud.tipoCambio.etiqueta}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(solicitud.datosPropuestos, style = MaterialTheme.typography.bodySmall)
            if (solicitud.observacion != null) {
                Text(
                    text = "Observación: ${solicitud.observacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (solicitud.motivoRechazo != null) {
                Text(
                    text = "Motivo: ${solicitud.motivoRechazo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (puedeResolver && solicitud.estado == EstadoSolicitud.PENDIENTE) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAprobar, modifier = Modifier.weight(1f)) {
                        Text("Aprobar")
                    }
                    OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeSolicitud(estado: EstadoSolicitud) {
    val (fondo, texto) = when (estado) {
        EstadoSolicitud.PENDIENTE -> Color(0xFFFFF3D6) to Color(0xFF8A5A00)
        EstadoSolicitud.APROBADA -> Color(0xFFE3F2E5) to Color(0xFF1E6B24)
        EstadoSolicitud.RECHAZADA -> Color(0xFFFBE0DD) to Color(0xFF8C1D18)
    }
    Text(
        text = estado.etiqueta,
        color = texto,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
