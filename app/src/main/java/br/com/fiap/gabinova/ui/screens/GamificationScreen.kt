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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import br.com.fiap.gabinova.ui.components.BadgeCard
import br.com.fiap.gabinova.ui.components.RankingCard
import br.com.fiap.gabinova.ui.components.SectionTitle
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import br.com.fiap.gabinova.ui.viewmodel.GamificationUiState
import br.com.fiap.gabinova.ui.viewmodel.GamificationViewModelFactory
import br.com.fiap.gabinova.ui.viewmodel.LocalBadge
import br.com.fiap.gabinova.ui.viewmodel.LocalRankingItem
import br.com.fiap.gabinova.ui.viewmodel.ScoreEvent
import br.com.fiap.gabinova.ui.viewmodel.ScoreEventType
import br.com.fiap.gabinova.ui.viewmodel.GamificationViewModel

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun eventColor(type: ScoreEventType): Color = when (type) {
    ScoreEventType.IDEA_CREATED      -> GabBlue
    ScoreEventType.IDEA_DESCRIBED    -> GabLightBlue
    ScoreEventType.IDEA_PRIORITIZED  -> Color(0xFFFF9800)
    ScoreEventType.IDEA_APPROVED     -> GabGreen
    ScoreEventType.IDEA_TO_PROJECT   -> Color(0xFF9C27B0)
    ScoreEventType.PROJECT_COMPLETED -> Color(0xFFFFB300)
}

private fun eventIcon(type: ScoreEventType): ImageVector = when (type) {
    ScoreEventType.IDEA_CREATED      -> Icons.Filled.Lightbulb
    ScoreEventType.IDEA_DESCRIBED    -> Icons.Filled.Edit
    ScoreEventType.IDEA_PRIORITIZED  -> Icons.AutoMirrored.Filled.TrendingUp
    ScoreEventType.IDEA_APPROVED     -> Icons.Filled.CheckCircle
    ScoreEventType.IDEA_TO_PROJECT   -> Icons.Filled.RocketLaunch
    ScoreEventType.PROJECT_COMPLETED -> Icons.Filled.EmojiEvents
}

// ── Level system ───────────────────────────────────────────────────────────────

private data class LevelDef(
    val number: Int,
    val name: String,
    val minPoints: Int,
    val maxPoints: Int,
    val badgeColor: Color,
    val textColor: Color
)

private val LEVEL_DEFS = listOf(
    LevelDef(1, "Explorador da Inovação",   0,   49,  Color(0xFFB3E5FC), GabBlue),
    LevelDef(2, "Colaborador Criativo",     50,  149, Color(0xFFC8E6C9), GabGreen),
    LevelDef(3, "Agente de Melhoria",       150, 299, Color(0xFFFFE0B2), Color(0xFFE65100)),
    LevelDef(4, "Inovador Águia",           300, 499, GabYellow,         GabTextDark),
    LevelDef(5, "Embaixador da Inovação",   500, Int.MAX_VALUE, Color(0xFFE1BEE7), Color(0xFF6A1B9A))
)

private fun levelOf(points: Int): LevelDef =
    LEVEL_DEFS.lastOrNull { points >= it.minPoints } ?: LEVEL_DEFS.first()

private fun levelProgress(points: Int): Float {
    val cur = levelOf(points)
    val curIdx = LEVEL_DEFS.indexOf(cur)
    if (curIdx == LEVEL_DEFS.size - 1) return 1f
    val next = LEVEL_DEFS[curIdx + 1]
    val inLevel = (points - cur.minPoints).toFloat()
    val span    = (next.minPoints - cur.minPoints).toFloat()
    return (inLevel / span).coerceIn(0f, 1f)
}

// ── Screen Principal ───────────────────────────────────────────────────────────

@Composable
fun GamificationScreen() {
    val context = LocalContext.current
    val vm: GamificationViewModel = viewModel(factory = GamificationViewModelFactory(context))

    Surface(modifier = Modifier.fillMaxSize(), color = GabBackground) {
        when {
            vm.state.isLoading -> {
                Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GabBlue) }
            }
            vm.state.error != null -> {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(vm.state.error!!, color = GabError, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = vm::retry) { Text("Tentar novamente") }
                }
            }
            else -> GamificationContent(state = vm.state)
        }
    }
}

@Composable
internal fun GamificationContent(state: GamificationUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeroSection(state = state)

        Column(modifier = Modifier.padding(16.dp)) {
            PointsReferenceCard()

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Meus Badges")
            BadgesSection(badges = state.badges)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Ranking Geral")
            RankingSection(items = state.rankingList)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Histórico")
            HistorySection(events = state.history)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Seções de UI ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroSection(state: GamificationUiState) {
    val lv = levelOf(state.points)
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(GabBlue)
        .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(GabYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.userName.isNotEmpty()) state.userName.take(1).uppercase() else "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GabTextDark
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = state.userName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(shape = RoundedCornerShape(4.dp), color = lv.badgeColor) {
                        Text(
                            text = lv.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = lv.textColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { levelProgress(state.points) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = GabYellow,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun BadgesSection(badges: List<LocalBadge>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { badge ->
                    Box(modifier = Modifier.weight(1f)) {
                        BadgeCard(
                            title = badge.name,
                            description = badge.description,
                            icon = badge.icon,
                            earned = badge.earned
                        )
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RankingSection(items: List<LocalRankingItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            RankingCard(
                position = item.position,
                name = item.userName,
                score = item.points,
                isCurrentUser = item.isCurrentUser
            )
        }
    }
}

@Composable
private fun PointsReferenceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Como pontuar", fontWeight = FontWeight.Bold)
            val rules = listOf(
                "Ideia Criada"      to "+10",
                "Ideia Aprovada"    to "+50",
                "Projeto Concluído" to "+200"
            )
            rules.forEach { (label, pts) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(pts, fontWeight = FontWeight.Bold, color = GabBlue)
                }
            }
        }
    }
}

@Composable
private fun HistorySection(events: List<ScoreEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface)
    ) {
        Column {
            events.forEachIndexed { index, event ->
                HistoryItem(event)
                if (index < events.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(event: ScoreEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = eventIcon(event.type),
            contentDescription = null,
            tint = eventColor(event.type),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(event.description, style = MaterialTheme.typography.bodySmall)
            Text(event.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Text("+${event.points}", fontWeight = FontWeight.Bold, color = eventColor(event.type))
    }
}

// ── Preview ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GamificationPreview() {
    GabInovaTheme {
        GamificationContent(
            state = GamificationUiState(
                userName = "Usuário Teste",
                points = 180,
                rankingPosition = 12,
                badges = listOf(
                    LocalBadge("1", "Pioneiro", "Primeira ideia", Icons.Filled.Lightbulb, true, "10/10"),
                    LocalBadge("2", "Mestre", "Ideia aprovada", Icons.Filled.Star, false, null)
                ),
                rankingList = listOf(
                    LocalRankingItem(1, "Ana Paula", 500, "Inovação", false),
                    LocalRankingItem(12, "Usuário Teste", 180, "TI", true)
                ),
                history = listOf(
                    ScoreEvent(10, "Nova ideia", "12/10", ScoreEventType.IDEA_CREATED)
                )
            )
        )
    }
}
