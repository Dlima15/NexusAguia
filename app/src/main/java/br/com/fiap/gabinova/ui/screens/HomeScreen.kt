package br.com.fiap.gabinova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.ui.components.GabCard
import br.com.fiap.gabinova.ui.components.PrimaryButton
import br.com.fiap.gabinova.ui.components.ProgressCard
import br.com.fiap.gabinova.ui.components.SectionTitle
import br.com.fiap.gabinova.ui.components.StatCard
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabPrimaryContainer
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import br.com.fiap.gabinova.ui.viewmodel.HomeUiState
import br.com.fiap.gabinova.ui.viewmodel.HomeViewModel
import br.com.fiap.gabinova.ui.viewmodel.HomeViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ── Navigation actions ────────────────────────────────────────────────────────

data class HomeNavActions(
    val onNavigateToGuidelines:   () -> Unit = {},
    val onNavigateToIdeas:        () -> Unit = {},
    val onNavigateToProjects:     () -> Unit = {},
    val onNavigateToDashboard:    () -> Unit = {},
    val onNavigateToGamification: () -> Unit = {}
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToGuidelines:   () -> Unit = {},
    onNavigateToIdeas:        () -> Unit = {},
    onNavigateToProjects:     () -> Unit = {},
    onNavigateToDashboard:    () -> Unit = {},
    onNavigateToGamification: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))

    HomeContent(
        state = vm.uiState,
        nav   = HomeNavActions(
            onNavigateToGuidelines   = onNavigateToGuidelines,
            onNavigateToIdeas        = onNavigateToIdeas,
            onNavigateToProjects     = onNavigateToProjects,
            onNavigateToDashboard    = onNavigateToDashboard,
            onNavigateToGamification = onNavigateToGamification
        )
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
internal fun HomeContent(state: HomeUiState, nav: HomeNavActions = HomeNavActions()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeSection(userName = state.userName, userRole = state.userRole)
        StatsSection(state = state)
        ProfileSection(state = state, nav = nav)
        QuickAccessSection(nav = nav)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Welcome Section ───────────────────────────────────────────────────────────

@Composable
private fun WelcomeSection(userName: String, userRole: UserRole) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = GabPrimaryContainer),
        shape    = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${greeting()}, ${userName.ifBlank { "Usuário" }}!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GabBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = GabOnSurfaceVariant
                )
            }
            Surface(
                color = GabBlue,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = roleLabel(userRole),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GabOnPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ── Stats Section ─────────────────────────────────────────────────────────────

@Composable
private fun StatsSection(state: HomeUiState) {
    SectionTitle(title = "Resumo")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (state.userRole) {
            UserRole.COLLABORATOR -> {
                StatCard(value = "${state.myIdeasCount}", label = "Minhas Ideias",  icon = Icons.Filled.Lightbulb,   iconTint = GabGreen,     modifier = Modifier.weight(1f))
                StatCard(value = "${state.points}",       label = "Pontos",          icon = Icons.Filled.Star,        iconTint = GabYellow,    modifier = Modifier.weight(1f))
                StatCard(value = "#${state.ranking}",     label = "Ranking",         icon = Icons.Filled.EmojiEvents, iconTint = GabLightBlue, modifier = Modifier.weight(1f))
            }
            UserRole.MANAGER -> {
                StatCard(value = "${state.pendingReviews}",    label = "Para Avaliar", icon = Icons.Filled.Lightbulb,   iconTint = GabYellow,    modifier = Modifier.weight(1f))
                StatCard(value = "${state.activeProjects}",    label = "Projetos",     icon = Icons.Filled.Work,        iconTint = GabLightBlue, modifier = Modifier.weight(1f))
                StatCard(value = "${state.approvedThisMonth}", label = "Aprovados",    icon = Icons.Filled.CheckCircle, iconTint = GabGreen,     modifier = Modifier.weight(1f))
            }
            else -> {
                StatCard(value = "${state.totalIdeas}",        label = "Ideias",       icon = Icons.Filled.Lightbulb,  iconTint = GabBlue,   modifier = Modifier.weight(1f))
                StatCard(value = "${state.completedProjects}", label = "Projetos",     icon = Icons.Filled.Work,       iconTint = GabGreen,  modifier = Modifier.weight(1f))
                StatCard(value = "${state.engagementRate}%",   label = "Engajamento",  icon = Icons.Filled.TrendingUp, iconTint = GabYellow, modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Profile Section ───────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(state: HomeUiState, nav: HomeNavActions) {
    SectionTitle(title = "Suas Ações")
    when (state.userRole) {
        UserRole.COLLABORATOR -> OperadorSection(state, nav)
        UserRole.MANAGER      -> GestorSection(state, nav)
        else                  -> LiderancaSection(state, nav)
    }
}

@Composable
private fun OperadorSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title       = "Seu Progresso — Nível ${state.level} · ${state.levelName}",
            current     = state.points,
            total       = 500,
            description = "${500 - state.points} pontos para o próximo nível"
        )
        GabCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GabYellow)
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GabTextDark, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Gamificação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GabTextDark)
                    Text("Conquiste badges e suba no ranking!", style = MaterialTheme.typography.bodySmall, color = GabOnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(text = "Ver Gamificação", onClick = nav.onNavigateToGamification)
        }
        PrimaryButton(text = "Enviar Nova Ideia", onClick = nav.onNavigateToIdeas, icon = Icons.Filled.Lightbulb)
    }
}

@Composable
private fun GestorSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title       = "Meta de Avaliações do Mês",
            current     = state.approvedThisMonth,
            total       = state.approvalGoal,
            description = "${state.approvalGoal - state.approvedThisMonth} restantes"
        )
        GabCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0))
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${state.pendingReviews} ideias aguardam avaliação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GabTextDark)
                    Text("Responda para desbloquear pontos do time", style = MaterialTheme.typography.bodySmall, color = GabOnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(text = "Avaliar Ideias", onClick = nav.onNavigateToIdeas, icon = Icons.Filled.CheckCircle)
        }
        PrimaryButton(text = "Ver Projetos Ativos", onClick = nav.onNavigateToProjects, icon = Icons.Filled.Work)
    }
}

@Composable
private fun LiderancaSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title       = "Meta Trimestral de Inovação",
            current     = state.engagementRate,
            total       = 100,
            description = "${state.engagementRate}% de engajamento atingido"
        )
        GabCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GabPrimaryContainer)
                ) {
                    Icon(Icons.Filled.BarChart, contentDescription = null, tint = GabBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${state.totalIdeas} ideias geradas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GabTextDark)
                    Text("${state.completedProjects} projetos concluídos neste ciclo", style = MaterialTheme.typography.bodySmall, color = GabOnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(text = "Ver Dashboard Completo", onClick = nav.onNavigateToDashboard, icon = Icons.Filled.BarChart)
        }
        PrimaryButton(text = "Ver Estratégias", onClick = nav.onNavigateToGuidelines, icon = Icons.Filled.TrendingUp)
    }
}

// ── Quick Access Section ──────────────────────────────────────────────────────

private data class ShortcutItem(
    val icon:     ImageVector,
    val label:    String,
    val iconBg:   Color,
    val iconTint: Color = Color.White,
    val onClick:  () -> Unit
)

@Composable
private fun QuickAccessSection(nav: HomeNavActions) {
    val shortcuts = listOf(
        ShortcutItem(Icons.Filled.TrendingUp,  "Estratégias", GabBlue,                onClick = nav.onNavigateToGuidelines),
        ShortcutItem(Icons.Filled.Lightbulb,   "Ideias",      GabGreen,               onClick = nav.onNavigateToIdeas),
        ShortcutItem(Icons.Filled.Work,        "Projetos",    GabLightBlue,           onClick = nav.onNavigateToProjects),
        ShortcutItem(Icons.Filled.BarChart,    "Dashboard",   Color(0xFF6A1B9A),      onClick = nav.onNavigateToDashboard),
        ShortcutItem(Icons.Filled.EmojiEvents, "Gamificação", GabYellow, GabTextDark, onClick = nav.onNavigateToGamification)
    )

    SectionTitle(title = "Acesso Rápido")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shortcuts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    ShortcutCard(item, modifier = Modifier.weight(1f))
                }
                if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ShortcutCard(item: ShortcutItem, modifier: Modifier = Modifier) {
    Card(
        onClick   = item.onClick,
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.iconBg)
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.label,
                    tint               = item.iconTint,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Text(
                text       = item.label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = GabTextDark,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else      -> "Boa noite"
    }
}

private fun formattedDate(): String =
    SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

private fun roleLabel(role: UserRole) = when (role) {
    UserRole.COLLABORATOR -> "Operador"
    UserRole.ANALYST      -> "Analista"
    UserRole.MANAGER      -> "Gestor"
    UserRole.ADMIN        -> "Liderança"
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Home — Operador")
@Composable
private fun HomeOperadorPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(userName = "Danilo", userRole = UserRole.COLLABORATOR,
                myIdeasCount = 5, points = 320, ranking = 12,
                level = 3, levelName = "Colaborador", levelProgress = 0.64f)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — Gestor")
@Composable
private fun HomeGestorPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(userName = "Carlos", userRole = UserRole.MANAGER,
                pendingReviews = 8, activeProjects = 4, approvedThisMonth = 12, approvalGoal = 20)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — Liderança")
@Composable
private fun HomeLiderancaPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(userName = "Ana", userRole = UserRole.ADMIN,
                totalIdeas = 147, completedProjects = 23,
                engagementRate = 78, quarterlyProgress = 0.78f)
        )
    }
}
