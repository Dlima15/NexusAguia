package br.com.fiap.gabinova.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.R
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

data class HomeNavActions(
    val onNavigateToGuidelines: () -> Unit = {},
    val onNavigateToIdeas: () -> Unit = {},
    val onNavigateToProjects: () -> Unit = {},
    val onNavigateToDashboard: () -> Unit = {},
    val onNavigateToGamification: () -> Unit = {},
    val onNavigateToInstitutional: () -> Unit = {}
)

@Composable
fun HomeScreen(
    onNavigateToGuidelines: () -> Unit = {},
    onNavigateToIdeas: () -> Unit = {},
    onNavigateToProjects: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToGamification: () -> Unit = {},
    onNavigateToInstitutional: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))

    HomeContent(
        state = vm.uiState,
        nav = HomeNavActions(
            onNavigateToGuidelines = onNavigateToGuidelines,
            onNavigateToIdeas = onNavigateToIdeas,
            onNavigateToProjects = onNavigateToProjects,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToGamification = onNavigateToGamification,
            onNavigateToInstitutional = onNavigateToInstitutional
        )
    )
}

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
        ImpactBanner(userRole = state.userRole)
        InstitutionalBanner(onClick = nav.onNavigateToInstitutional)
        StatsSection(state = state)
        ProfileSection(state = state, nav = nav)
        QuickAccessSection(userRole = state.userRole, nav = nav)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WelcomeSection(userName: String, userRole: UserRole) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabPrimaryContainer),
        shape = MaterialTheme.shapes.medium,
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strategicMessage(userRole),
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

@Composable
private fun ImpactBanner(userRole: UserRole) {
    val title = when (userRole) {
        UserRole.COLLABORATOR ->
            "Sua participação fortalece a inovação da companhia."

        UserRole.MANAGER ->
            "Gestores impulsionam a transformação através das ideias."

        UserRole.ADMIN ->
            "Resultados estratégicos acompanhados em tempo real."

        else ->
            "Acompanhe iniciativas, inovação e resultados da plataforma Nexus."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(GabGreen, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = GabTextDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InstitutionalBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.team),
                    contentDescription = "Equipe corporativa do Programa Nexus",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GabBlue.copy(alpha = 0.35f))
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Programa Nexus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Transformando ideias em impacto real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Conheça a iniciativa que conecta estratégia, colaboradores, projetos e resultados mensuráveis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GabOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    MiniTag("Estratégia")
                    MiniTag("Inovação")
                    MiniTag("Projetos")
                    MiniTag("Impacto")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "O Nexus conecta colaboradores, estratégia e inovação em uma plataforma corporativa voltada para resultados reais.\n" +
                            "\n" +
                            "Ideias alinhadas às diretrizes da companhia podem evoluir para projetos estratégicos e gerar reconhecimento, pontuação e benefícios internos aos colaboradores.",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GabBlue
                )
            }
        }
    }
}

@Composable
private fun MiniTag(text: String) {
    Box(
        modifier = Modifier
            .background(GabPrimaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = GabBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsSection(state: HomeUiState) {
    SectionTitle(title = "Indicadores Estratégicos")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (state.userRole) {
            UserRole.COLLABORATOR -> {
                StatCard(
                    value = "${state.myIdeasCount}",
                    label = "Minhas Ideias",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Lightbulb,
                    iconTint = GabGreen
                )

                StatCard(
                    value = "${state.points}",
                    label = "Pontos Nexus",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Star,
                    iconTint = GabYellow
                )

                StatCard(
                    value = "#${state.ranking}",
                    label = "Ranking",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.EmojiEvents,
                    iconTint = GabLightBlue
                )
            }

            UserRole.MANAGER -> {
                StatCard(
                    value = "${state.pendingReviews}",
                    label = "Para Avaliar",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Lightbulb,
                    iconTint = GabYellow
                )

                StatCard(
                    value = "${state.activeProjects}",
                    label = "Projetos",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Work,
                    iconTint = GabLightBlue
                )

                StatCard(
                    value = "${state.approvedThisMonth}",
                    label = "Priorizadas",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = GabGreen
                )
            }

            else -> {
                StatCard(
                    value = "${state.totalIdeas}",
                    label = "Ideias",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Lightbulb,
                    iconTint = GabBlue
                )

                StatCard(
                    value = "${state.completedProjects}",
                    label = "Impacto Medido",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Work,
                    iconTint = GabGreen
                )

                StatCard(
                    value = "${state.engagementRate}%",
                    label = "Engajamento",
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconTint = GabYellow
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(state: HomeUiState, nav: HomeNavActions) {
    SectionTitle(title = "Suas Ações")

    when (state.userRole) {
        UserRole.COLLABORATOR -> OperadorSection(state, nav)
        UserRole.MANAGER -> GestorSection(state, nav)
        else -> LiderancaSection(state, nav)
    }
}

@Composable
private fun OperadorSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title = "Seu Progresso — Nível ${state.level} · ${state.levelName}",
            current = state.points,
            total = 500,
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
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = GabTextDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Programa Nexus de Reconhecimento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GabTextDark
                    )

                    Text(
                        "Seus resultados podem gerar reconhecimento, benefícios e destaque interno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Ver Reconhecimento",
                onClick = nav.onNavigateToGamification
            )
        }

        PrimaryButton(
            text = "Enviar Nova Ideia",
            onClick = nav.onNavigateToIdeas,
            icon = Icons.Filled.Lightbulb
        )
    }
}

@Composable
private fun GestorSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title = "Meta de Avaliações do Mês",
            current = state.approvedThisMonth,
            total = state.approvalGoal,
            description = "${state.approvalGoal - state.approvedThisMonth} avaliações restantes"
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
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "${state.pendingReviews} ideias aguardam avaliação",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GabTextDark
                    )

                    Text(
                        "Acelere ideias com potencial estratégico e impacto operacional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Avaliar Ideias",
                onClick = nav.onNavigateToIdeas,
                icon = Icons.Filled.CheckCircle
            )
        }

        PrimaryButton(
            text = "Ver Projetos Estratégicos",
            onClick = nav.onNavigateToProjects,
            icon = Icons.Filled.Work
        )
    }
}

@Composable
private fun LiderancaSection(state: HomeUiState, nav: HomeNavActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            title = "Meta Trimestral de Inovação",
            current = state.engagementRate,
            total = 100,
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
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = GabBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "${state.totalIdeas} ideias geradas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GabTextDark
                    )

                    Text(
                        "${state.completedProjects} projetos entregando impacto mensurável",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Ver Dashboard Executivo",
                onClick = nav.onNavigateToDashboard,
                icon = Icons.Filled.BarChart
            )
        }

        PrimaryButton(
            text = "Gerenciar Diretrizes",
            onClick = nav.onNavigateToGuidelines,
            icon = Icons.AutoMirrored.Filled.TrendingUp
        )
    }
}

private data class ShortcutItem(
    val icon: ImageVector,
    val label: String,
    val iconBg: Color,
    val iconTint: Color = Color.White,
    val onClick: () -> Unit
)

@Composable
private fun QuickAccessSection(userRole: UserRole, nav: HomeNavActions) {
    val shortcuts = when (userRole) {
        UserRole.COLLABORATOR -> listOf(
            ShortcutItem(
                Icons.AutoMirrored.Filled.TrendingUp,
                "Estratégias",
                GabBlue,
                onClick = nav.onNavigateToGuidelines
            ),
            ShortcutItem(
                Icons.Filled.Lightbulb,
                "Ideias",
                GabGreen,
                onClick = nav.onNavigateToIdeas
            ),
            ShortcutItem(
                Icons.Filled.EmojiEvents,
                "Reconhecimento",
                GabYellow,
                GabTextDark,
                onClick = nav.onNavigateToGamification
            )
        )

        UserRole.MANAGER -> listOf(
            ShortcutItem(
                Icons.AutoMirrored.Filled.TrendingUp,
                "Estratégias",
                GabBlue,
                onClick = nav.onNavigateToGuidelines
            ),
            ShortcutItem(
                Icons.Filled.Lightbulb,
                "Ideias",
                GabGreen,
                onClick = nav.onNavigateToIdeas
            ),
            ShortcutItem(
                Icons.Filled.Work,
                "Projetos",
                GabLightBlue,
                onClick = nav.onNavigateToProjects
            )
        )

        UserRole.ADMIN -> listOf(
            ShortcutItem(
                Icons.AutoMirrored.Filled.TrendingUp,
                "Estratégias",
                GabBlue,
                onClick = nav.onNavigateToGuidelines
            ),
            ShortcutItem(
                Icons.Filled.Work,
                "Projetos",
                GabLightBlue,
                onClick = nav.onNavigateToProjects
            ),
            ShortcutItem(
                Icons.Filled.BarChart,
                "Dashboard",
                Color(0xFF6A1B9A),
                onClick = nav.onNavigateToDashboard
            )
        )

        else -> listOf(
            ShortcutItem(
                Icons.AutoMirrored.Filled.TrendingUp,
                "Estratégias",
                GabBlue,
                onClick = nav.onNavigateToGuidelines
            )
        )
    }

    SectionTitle(title = "Central Nexus")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shortcuts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    ShortcutCard(item, modifier = Modifier.weight(1f))
                }

                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(item: ShortcutItem, modifier: Modifier = Modifier) {
    Card(
        onClick = item.onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
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
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GabTextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    return when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
}

private fun formattedDate(): String =
    SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

private fun roleLabel(role: UserRole) = when (role) {
    UserRole.COLLABORATOR -> "Operador"
    UserRole.ANALYST -> "Analista"
    UserRole.MANAGER -> "Gestor"
    UserRole.ADMIN -> "Liderança"
}

private fun strategicMessage(role: UserRole): String =
    when (role) {
        UserRole.COLLABORATOR ->
            "Transforme desafios do dia a dia em ideias com impacto real."

        UserRole.MANAGER ->
            "Priorize iniciativas e acompanhe projetos estratégicos."

        UserRole.ADMIN ->
            "Conecte inovação, execução e resultados mensuráveis."

        else ->
            "Acompanhe iniciativas e resultados da plataforma Nexus."
    }

@Preview(showBackground = true, showSystemUi = true, name = "Home — Operador")
@Composable
private fun HomeOperadorPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Danilo",
                userRole = UserRole.COLLABORATOR,
                myIdeasCount = 5,
                points = 320,
                ranking = 12,
                level = 3,
                levelName = "Colaborador",
                levelProgress = 0.64f
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — Gestor")
@Composable
private fun HomeGestorPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Carlos",
                userRole = UserRole.MANAGER,
                pendingReviews = 8,
                activeProjects = 4,
                approvedThisMonth = 12,
                approvalGoal = 20
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home — Liderança")
@Composable
private fun HomeLiderancaPreview() {
    GabInovaTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Ana",
                userRole = UserRole.ADMIN,
                totalIdeas = 147,
                completedProjects = 23,
                engagementRate = 78,
                quarterlyProgress = 0.78f
            )
        )
    }
}