package br.com.fiap.gabinova.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.components.GabBottomBar
import br.com.fiap.gabinova.ui.components.GabScaffold
import br.com.fiap.gabinova.ui.components.GabTopBar
import br.com.fiap.gabinova.ui.screens.DashboardScreen
import br.com.fiap.gabinova.ui.screens.GamificationScreen
import br.com.fiap.gabinova.ui.screens.HomeScreen
import br.com.fiap.gabinova.ui.screens.IdeasScreen
import br.com.fiap.gabinova.ui.screens.LoginScreen
import br.com.fiap.gabinova.ui.screens.ProjectsScreen
import br.com.fiap.gabinova.ui.screens.SplashScreen
import br.com.fiap.gabinova.ui.screens.StrategicGuidelinesScreen

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
    val context        = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val showBars       = currentRoute in authenticatedRoutes

    val userName by sessionManager.userNameFlow.collectAsState(initial = "")
    val userRole by sessionManager.userRoleFlow.collectAsState(initial = "")

    GabScaffold(
        topBar = {
            if (showBars) {
                GabTopBar(
                    userName    = userName.ifBlank { "Usuário" },
                    userRole    = userRole,
                    onAvatarClick = { /* TODO: abrir perfil */ }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                GabBottomBar(
                    selectedRoute = currentRoute ?: Routes.HOME,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
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
                    },
                    onNavigateToHome = {
                        navController.navigate(Routes.HOME) {
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
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToGuidelines   = { navController.navigate(Routes.GUIDELINES) },
                    onNavigateToIdeas        = { navController.navigate(Routes.IDEAS) },
                    onNavigateToProjects     = { navController.navigate(Routes.PROJECTS) },
                    onNavigateToDashboard    = { navController.navigate(Routes.DASHBOARD) },
                    onNavigateToGamification = { navController.navigate(Routes.GAMIFICATION) }
                )
            }
            composable(Routes.GUIDELINES)   { StrategicGuidelinesScreen() }
            composable(Routes.IDEAS)        { IdeasScreen() }
            composable(Routes.PROJECTS)     { ProjectsScreen() }
            composable(Routes.DASHBOARD)    { DashboardScreen() }
            composable(Routes.GAMIFICATION) { GamificationScreen() }
        }
    }
}

