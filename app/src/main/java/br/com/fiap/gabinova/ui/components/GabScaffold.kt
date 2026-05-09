package br.com.fiap.gabinova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabPrimaryContainer
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow

sealed class GabNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    data object Home        : GabNavItem("Home",        Icons.Filled.Home,      "home")
    data object Estrategias : GabNavItem("Estratégias", Icons.Filled.TrendingUp,"estrategias")
    data object Ideias      : GabNavItem("Ideias",      Icons.Filled.Lightbulb, "ideias")
    data object Projetos    : GabNavItem("Projetos",    Icons.Filled.Work,      "projetos")
    data object Dashboard   : GabNavItem("Dashboard",   Icons.Filled.BarChart,  "dashboard")
}

val gabNavItems = listOf(
    GabNavItem.Home,
    GabNavItem.Estrategias,
    GabNavItem.Ideias,
    GabNavItem.Projetos,
    GabNavItem.Dashboard
)

@Composable
fun GabScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = GabBackground,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GabTopBar(
    userName: String,
    userRole: String = "",
    onAvatarClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Olá, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GabOnPrimary
                )
                if (userRole.isNotBlank()) {
                    Text(
                        text = userRole,
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onAvatarClick) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GabYellow)
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = GabTextDark
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = GabBlue)
    )
}

@Composable
fun GabBottomBar(
    selectedRoute: String = GabNavItem.Home.route,
    onItemSelected: (GabNavItem) -> Unit = {}
) {
    NavigationBar(
        containerColor = GabSurface,
        tonalElevation = 4.dp
    ) {
        gabNavItems.forEach { item ->
            NavigationBarItem(
                selected = item.route == selectedRoute,
                onClick = { onItemSelected(item) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = GabBlue,
                    selectedTextColor   = GabBlue,
                    indicatorColor      = GabPrimaryContainer,
                    unselectedIconColor = GabOnSurfaceVariant,
                    unselectedTextColor = GabOnSurfaceVariant
                )
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun GabTopBarPreview() {
    GabInovaTheme {
        GabTopBar(userName = "Danilo", userRole = "Analista de Inovação")
    }
}

@Preview(showBackground = true)
@Composable
private fun GabBottomBarPreview() {
    GabInovaTheme {
        GabBottomBar(selectedRoute = GabNavItem.Home.route)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GabScaffoldPreview() {
    GabInovaTheme {
        GabScaffold(
            topBar    = { GabTopBar(userName = "Danilo", userRole = "Analista") },
            bottomBar = { GabBottomBar() }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Conteúdo da tela", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
