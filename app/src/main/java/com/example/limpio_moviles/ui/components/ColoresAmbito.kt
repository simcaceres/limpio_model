package com.example.limpio_moviles.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.ui.theme.AzulLemac
import com.example.limpio_moviles.ui.theme.MoradoLemac
import com.example.limpio_moviles.ui.theme.NaranjaLemac
import com.example.limpio_moviles.ui.theme.VerdeLemac

/** Color de identidad de cada ámbito, con contraste suficiente sobre fondos claros. */
fun colorAmbito(ambito: Ambito): Color = when (ambito) {
    Ambito.NEURODESARROLLO -> VerdeLemac
    Ambito.DBT -> AzulLemac
    Ambito.ANIMO -> NaranjaLemac
    Ambito.ADICCIONES -> MoradoLemac
}

/** Icono representativo de cada ámbito. */
fun iconoAmbito(ambito: Ambito): ImageVector = when (ambito) {
    Ambito.NEURODESARROLLO -> Icons.Default.Psychology
    Ambito.DBT -> Icons.Default.SelfImprovement
    Ambito.ANIMO -> Icons.Default.Mood
    Ambito.ADICCIONES -> Icons.Default.Warning
}
