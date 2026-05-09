package br.com.fiap.gabinova.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.gabinova.ui.components.GabBottomBar
import br.com.fiap.gabinova.ui.components.GabScaffold
import br.com.fiap.gabinova.ui.components.GabTopBar
import br.com.fiap.gabinova.ui.theme.GabBackground

// Rotas que exibem GabTopBar + GabBottomBar
private val authenticatedRoutes = setOf(
    Routes.HOME,
    Routes.GUIDELINES,
    Routes.IDEAS,
    Routes.PROJECTS,
    Routes.DASHBOARD,
    Routes.GAMIFICATION
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBars = currentRoute in authenticatedRoutes

    GabScaffold(
        topBar = {
            if (showBars) {
                GabTopBar(
                    userName = "Usuário",
                    userRole = "Colaborador",
                    onAvatarClick = { /* abrir perfil */ }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                GabBottomBar(
                    selectedRoute = currentRoute ?: Routes.HOME,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            // Evita pilha crescente ao trocar de aba
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.SPLASH,
            modifier         = Modifier.padding(padding)
        ) {

            // ── Sem barra ────────────────────────────────────────────────
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // ── Com GabScaffold ──────────────────────────────────────────
            composable(Routes.HOME)         { HomeScreen() }
            composable(Routes.GUIDELINES)   { GuidelinesScreen() }
            composable(Routes.IDEAS)        { IdeasScreen() }
            composable(Routes.PROJECTS)     { ProjectsScreen() }
            composable(Routes.DASHBOARD)    { DashboardScreen() }
            composable(Routes.GAMIFICATION) { GamificationScreen() }
        }
    }
}

// ── Placeholders ─────────────────────────────────────────────────────────────
// Substituir pelos composables reais em ui.screens quando implementados.

@Composable
internal fun SplashScreen(onNavigateToLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Splash", style = MaterialTheme.typography.headlineMedium)
    }
    // TODO: implementar animação e lógica de auto-redirect
}

@Composable
internal fun LoginScreen(onLoginSuccess: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
    }
    // TODO: implementar formulário de login
}

@Composable
internal fun HomeScreen() {
    PlaceholderScreen(label = "Home")
}

@Composable
internal fun GuidelinesScreen() {
    PlaceholderScreen(label = "Estratégias")
}

@Composable
internal fun IdeasScreen() {
    PlaceholderScreen(label = "Ideias")
}

@Composable
internal fun ProjectsScreen() {
    PlaceholderScreen(label = "Projetos")
}

@Composable
internal fun DashboardScreen() {
    PlaceholderScreen(label = "Dashboard")
}

@Composable
internal fun GamificationScreen() {
    PlaceholderScreen(label = "Gamificação")
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.headlineMedium)
    }
}
