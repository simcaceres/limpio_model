package com.example.limpio_moviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.limpio_moviles.navigation.UnifiedApp
import com.example.limpio_moviles.ui.theme.LemacTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LemacTheme {
                UnifiedApp()
            }
        }
    }
}
