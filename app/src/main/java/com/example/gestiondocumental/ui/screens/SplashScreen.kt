package com.example.gestiondocumental.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiondocumental.ui.components.LogoGestion
import kotlinx.coroutines.delay

/**
 * Pantalla de apertura animada: el logo aparece con fade + escala y un pulso
 * suave, junto a un indicador de carga ("Cargando expedientes…").
 * Tras ~2,2 s navega a la pantalla siguiente.
 */
@Composable
fun SplashScreen(onTerminar: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val transicion = rememberInfiniteTransition(label = "splash")
    val pulso by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulso"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        onTerminar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) +
                    scaleIn(initialScale = 0.6f, animationSpec = tween(600))
            ) {
                Box(modifier = Modifier.graphicsLayer { scaleX = pulso; scaleY = pulso }) {
                    LogoGestion(tamano = 120.dp)
                }
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 250))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Gestión Documental",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recursos Humanos",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(44.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Cargando expedientes…",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
