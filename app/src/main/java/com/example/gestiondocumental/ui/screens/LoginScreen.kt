package com.example.gestiondocumental.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.ui.components.AvisoMensaje
import com.example.gestiondocumental.ui.components.LogoGestion
import com.example.gestiondocumental.viewmodel.SesionViewModel

private val CuentasDemo = listOf(
    Rol.FUNCIONARIO to "ana.perez@salud.cl",
    Rol.JEFATURA to "bruno.diaz@salud.cl",
    Rol.ADMINISTRATIVO to "carla.soto@salud.cl",
    Rol.RRHH to "diego.rojas@salud.cl",
    Rol.ADMIN_SISTEMA to "elena.munoz@salud.cl",
    Rol.TI_CIBERSEGURIDAD to "felipe.vega@salud.cl",
    Rol.SUPERVISOR to "gabriela.paz@salud.cl"
)

@Composable
fun LoginScreen(vm: SesionViewModel, onIngresado: () -> Unit) {
    val usuario by vm.actual.collectAsState()
    val error by vm.error.collectAsState()
    val cargando by vm.cargando.collectAsState()

    LaunchedEffect(usuario) {
        if (usuario != null) onIngresado()
    }

    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }

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
                .padding(vertical = 44.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LogoGestion(tamano = 88.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Gestión Documental",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acceso de funcionarios",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            if (error != null) {
                AvisoMensaje(error!!, esError = true, onCerrar = { vm.limpiarError() })
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo institucional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave") },
                singleLine = true,
                visualTransformation = if (claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Mostrar u ocultar clave"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { vm.iniciarSesion(correo, clave) },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(if (cargando) "Ingresando…" else "Ingresar")
            }

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Cuentas de demostración (clave 1234)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            CuentasDemo.forEach { (rol, correoDemo) ->
                OutlinedButton(
                    onClick = {
                        correo = correoDemo
                        clave = "1234"
                        vm.limpiarError()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "${rol.etiqueta} · $correoDemo",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
