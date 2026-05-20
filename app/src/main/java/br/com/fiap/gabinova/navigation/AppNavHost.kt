package br.com.fiap.gabinova.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.gabinova.model.UserRole
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import br.com.fiap.gabinova.ui.screens.ProfileScreen

private val authenticatedRoutes = setOf(
    Routes.HOME,
    Routes.GUIDELINES,
    Routes.IDEAS,
    Routes.PROJECTS,
    Routes.DASHBOARD,
    Routes.GAMIFICATION,
    Routes.PROFILE
)

private fun canAccess(route: String?, role: UserRole): Boolean {
    return when (route) {
        Routes.HOME -> true
        Routes.GUIDELINES -> true

        Routes.IDEAS -> role == UserRole.COLLABORATOR || role == UserRole.MANAGER
        Routes.GAMIFICATION -> role == UserRole.COLLABORATOR

        Routes.PROJECTS -> role == UserRole.MANAGER || role == UserRole.ADMIN
        Routes.DASHBOARD -> role == UserRole.ADMIN

        else -> true
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBars = currentRoute in authenticatedRoutes

    val userName by sessionManager.userNameFlow.collectAsState(initial = "")
    val userRoleString by sessionManager.userRoleFlow.collectAsState(initial = "")

    val userRole = runCatching {
        UserRole.valueOf(userRoleString)
    }.getOrDefault(UserRole.COLLABORATOR)

    GabScaffold(
        topBar = {
            if (showBars) {

                GabTopBar(
                    userName = userName.ifBlank { "Usuário" },
                    userRole = userRoleString,

                    onProfileClick = {
                        navController.navigate(Routes.PROFILE)
                    },

                    onLogoutClick = {
                        scope.launch {
                            sessionManager.clearSession()

                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                GabBottomBar(
                    selectedRoute = currentRoute ?: Routes.HOME,
                    userRole = userRoleString,
                    onItemSelected = { item ->
                        if (canAccess(item.route, userRole)) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
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

            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToGuidelines = {
                        navController.navigate(Routes.GUIDELINES)
                    },
                    onNavigateToIdeas = {
                        if (canAccess(Routes.IDEAS, userRole)) {
                            navController.navigate(Routes.IDEAS)
                        }
                    },
                    onNavigateToProjects = {
                        if (canAccess(Routes.PROJECTS, userRole)) {
                            navController.navigate(Routes.PROJECTS)
                        }
                    },
                    onNavigateToDashboard = {
                        if (canAccess(Routes.DASHBOARD, userRole)) {
                            navController.navigate(Routes.DASHBOARD)
                        }
                    },
                    onNavigateToGamification = {
                        if (canAccess(Routes.GAMIFICATION, userRole)) {
                            navController.navigate(Routes.GAMIFICATION)
                        }
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }

            composable(Routes.GUIDELINES) {
                StrategicGuidelinesScreen()
            }

            composable(Routes.IDEAS) {
                if (canAccess(Routes.IDEAS, userRole)) {
                    IdeasScreen()
                } else {
                    AccessDeniedScreen()
                }
            }

            composable(Routes.PROJECTS) {
                if (canAccess(Routes.PROJECTS, userRole)) {
                    ProjectsScreen()
                } else {
                    AccessDeniedScreen()
                }
            }

            composable(Routes.DASHBOARD) {
                if (canAccess(Routes.DASHBOARD, userRole)) {
                    DashboardScreen()
                } else {
                    AccessDeniedScreen()
                }
            }

            composable(Routes.GAMIFICATION) {
                if (canAccess(Routes.GAMIFICATION, userRole)) {
                    GamificationScreen()
                } else {
                    AccessDeniedScreen()
                }
            }
        }
    }
}

@Composable
private fun AccessDeniedScreen() {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Acesso restrito para este perfil.",
            style = MaterialTheme.typography.titleMedium
        )
    }
}