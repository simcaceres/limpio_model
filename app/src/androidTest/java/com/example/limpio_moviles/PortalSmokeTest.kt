package com.example.limpio_moviles

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de humo del portal de la app unificada.
 *
 * Valida que la pantalla de entrada muestre las dos tarjetas de módulos sin
 * depender de que la siembra asíncrona haya terminado.
 */
@RunWith(AndroidJUnit4::class)
class PortalSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun elPortalMuestraLasDosTarjetasDeModulos() {
        // Arrange: esperar a que el portal componga su primer frame.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithText("Centro de aplicaciones", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Act + Assert
        composeRule
            .onNodeWithText("Bienestar — Lemac DataLab", substring = true, useUnmergedTree = true)
            .assertExists()
        composeRule
            .onNodeWithText(
                "Autorregistro diario de los 4 ámbitos de bienestar",
                substring = true,
                useUnmergedTree = true
            )
            .assertExists()
        composeRule
            .onNodeWithText("Gestión Documental RR.HH.", substring = true, useUnmergedTree = true)
            .assertExists()
        composeRule
            .onNodeWithText(
                "Documentos, roles y auditoría del flujo de aprobación",
                substring = true,
                useUnmergedTree = true
            )
            .assertExists()
    }
}
