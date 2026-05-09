package br.com.fiap.gabinova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.fiap.gabinova.navigation.AppNavHost
import br.com.fiap.gabinova.ui.theme.GabInovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GabInovaTheme {
                AppNavHost()
            }
        }
    }
}
