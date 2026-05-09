package br.com.fiap.gabinova.ui.screens

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context        = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = EaseOut),
        label         = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.78f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutBack),
        label         = "splash_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2400)
        val isLoggedIn = sessionManager.isLoggedIn.first()
        if (isLoggedIn) onNavigateToHome() else onNavigateToLogin()
    }

    SplashScreenContent(alpha = alpha, scale = scale)
}

@Composable
internal fun SplashScreenContent(
    alpha: Float = 1f,
    scale: Float = 1f
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBlue),
        contentAlignment = Alignment.Center
    ) {

        // ── Conteúdo central ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            // Logo circular
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GabYellow)
            ) {
                Text(
                    text = "N",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GabTextDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Nexus Águia",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GabOnPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            HorizontalDivider(
                modifier  = Modifier.size(width = 48.dp, height = 2.dp),
                color     = GabYellow,
                thickness = 2.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Grupo Águia Branca",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = GabOnPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Inovação em Movimento",
                style = MaterialTheme.typography.bodySmall,
                color = GabOnPrimary.copy(alpha = 0.6f)
            )
        }

        // ── Rodapé ───────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
                .alpha(alpha)
        ) {
            CircularProgressIndicator(
                color       = GabYellow,
                strokeWidth = 2.5.dp,
                modifier    = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text  = "© 2025 Grupo Águia Branca",
                style = MaterialTheme.typography.labelSmall,
                color = GabLightBlue.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Splash — carregado")
@Composable
private fun SplashScreenPreview() {
    GabInovaTheme {
        SplashScreenContent(alpha = 1f, scale = 1f)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Splash — animando")
@Composable
private fun SplashScreenAnimatingPreview() {
    GabInovaTheme {
        SplashScreenContent(alpha = 0.4f, scale = 0.85f)
    }
}
