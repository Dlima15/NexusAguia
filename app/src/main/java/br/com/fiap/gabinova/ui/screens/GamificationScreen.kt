package br.com.fiap.gabinova.ui.screens

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.BadgeDto
import br.com.fiap.gabinova.data.remote.dto.GamificationDto
import br.com.fiap.gabinova.data.remote.dto.RankingDto
import br.com.fiap.gabinova.data.remote.dto.ScoreHistoryDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.repository.GamificationRepository
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.components.BadgeCard
import br.com.fiap.gabinova.ui.components.RankingCard
import br.com.fiap.gabinova.ui.components.SectionTitle
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
    if (cur.number == LEVEL_DEFS.size) return 1f
    val next = LEVEL_DEFS[cur.number]
    val inLevel = (points - cur.minPoints).toFloat()
    val span    = (next.minPoints - cur.minPoints).toFloat()
    return (inLevel / span).coerceIn(0f, 1f)
}

private fun pointsToNext(points: Int): Int {
    val cur = levelOf(points)
    if (cur.number == LEVEL_DEFS.size) return 0
    val next = LEVEL_DEFS[cur.number]
    return (next.minPoints - points).coerceAtLeast(0)
}

// ── Score events ───────────────────────────────────────────────────────────────

enum class ScoreEventType {
    IDEA_CREATED, IDEA_DESCRIBED, IDEA_PRIORITIZED,
    IDEA_APPROVED, IDEA_TO_PROJECT, PROJECT_COMPLETED
}

data class ScoreEvent(
    val points: Int,
    val description: String,
    val date: String,
    val type: ScoreEventType
)

private fun eventIcon(t: ScoreEventType): ImageVector = when (t) {
    ScoreEventType.IDEA_CREATED      -> Icons.Filled.Lightbulb
    ScoreEventType.IDEA_DESCRIBED    -> Icons.Filled.Edit
    ScoreEventType.IDEA_PRIORITIZED  -> Icons.Filled.TrendingUp
    ScoreEventType.IDEA_APPROVED     -> Icons.Filled.CheckCircle
    ScoreEventType.IDEA_TO_PROJECT   -> Icons.Filled.RocketLaunch
    ScoreEventType.PROJECT_COMPLETED -> Icons.Filled.EmojiEvents
}

private fun eventColor(t: ScoreEventType): Color = when (t) {
    ScoreEventType.IDEA_CREATED      -> GabBlue
    ScoreEventType.IDEA_DESCRIBED    -> GabLightBlue
    ScoreEventType.IDEA_PRIORITIZED  -> Color(0xFFFF9800)
    ScoreEventType.IDEA_APPROVED     -> GabGreen
    ScoreEventType.IDEA_TO_PROJECT   -> Color(0xFF9C27B0)
    ScoreEventType.PROJECT_COMPLETED -> Color(0xFFFFB300)
}

// ── Local screen models ────────────────────────────────────────────────────────

data class LocalBadge(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val earned: Boolean,
    val earnedAt: String? = null
)

data class LocalRankingItem(
    val position: Int,
    val userName: String,
    val points: Int,
    val department: String,
    val isCurrentUser: Boolean
)

// ── UI State ───────────────────────────────────────────────────────────────────

data class GamificationUiState(
    val userName: String                    = "Usuário",
    val userId: String                      = "",
    val points: Int                         = 0,
    val rankingPosition: Int               = 0,
    val badges: List<LocalBadge>           = emptyList(),
    val rankingList: List<LocalRankingItem> = emptyList(),
    val history: List<ScoreEvent>          = emptyList(),
    val isLoading: Boolean                 = false,
    val error: String?                     = null
) {
    val level: LevelDef       get() = levelOf(points)
    val progress: Float       get() = levelProgress(points)
    val ptsToNext: Int        get() = pointsToNext(points)
    val earnedCount: Int      get() = badges.count { it.earned }
    val isMaxLevel: Boolean   get() = level.number == LEVEL_DEFS.size
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

private fun badgeIcon(name: String): ImageVector = when {
    name.contains("Primeira",    ignoreCase = true) -> Icons.Filled.Lightbulb
    name.contains("Aprovada",    ignoreCase = true) -> Icons.Filled.CheckCircle
    name.contains("Impacto",     ignoreCase = true) -> Icons.Filled.TrendingUp
    name.contains("Custo",       ignoreCase = true) -> Icons.Filled.Savings
    name.contains("Produtividade",ignoreCase = true)-> Icons.Filled.EmojiEvents
    name.contains("Inovador",    ignoreCase = true) -> Icons.Filled.Star
    name.contains("Projeto",     ignoreCase = true) -> Icons.Filled.RocketLaunch
    else                                             -> Icons.Filled.Star
}

private fun BadgeDto.toLocalBadge() = LocalBadge(
    id       = id,
    name     = name,
    description = if (earned && earnedAt != null) earnedAt else name,
    icon     = badgeIcon(name),
    earned   = earned,
    earnedAt = earnedAt
)

private fun ScoreHistoryDto.toScoreEvent() = ScoreEvent(
    points      = points,
    description = description,
    date        = date,
    type        = runCatching { ScoreEventType.valueOf(eventType) }.getOrDefault(ScoreEventType.IDEA_CREATED)
)

private fun RankingDto.toLocalRanking(currentUserId: String) = LocalRankingItem(
    position      = position,
    userName      = userName,
    points        = points,
    department    = department,
    isCurrentUser = userId == currentUserId
)

class GamificationViewModel(
    private val sessionManager: SessionManager,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    var state by mutableStateOf(GamificationUiState())
        private set

    init {
        viewModelScope.launch {
            combine(
                sessionManager.userIdFlow,
                sessionManager.userNameFlow
            ) { id, name -> Pair(id, name) }.collect { (id, name) ->
                val displayName = name.ifBlank { "Usuário" }
                state = state.copy(userId = id, userName = displayName)
                if (id.isNotBlank()) loadData(id)
            }
        }
    }

    fun retry() { viewModelScope.launch { loadData(state.userId) } }

    private suspend fun loadData(userId: String) {
        state = state.copy(isLoading = true, error = null)
        val gamResult = gamificationRepository.getGamification(userId)
        val rankResult = gamificationRepository.getRanking()

        if (gamResult is ApiResult.Error) {
            state = state.copy(isLoading = false, error = gamResult.message)
            return
        }

        val gam     = (gamResult as ApiResult.Success).data
        val ranking = if (rankResult is ApiResult.Success) rankResult.data else emptyList()

        state = state.copy(
            isLoading       = false,
            points          = gam.points,
            rankingPosition = ranking.firstOrNull { it.userId == userId }?.position ?: 0,
            badges          = gam.badges.map { it.toLocalBadge() },
            history         = gam.history.map { it.toScoreEvent() },
            rankingList     = ranking.map { it.toLocalRanking(userId) }
        )
    }
}

// ── Factory ────────────────────────────────────────────────────────────────────

private class GamificationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GamificationViewModel(SessionManager(context), GamificationRepository(RetrofitClient.api)) as T
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun GamificationScreen() {
    val context = LocalContext.current
    val vm: GamificationViewModel = viewModel(factory = GamificationViewModelFactory(context))

    when {
        vm.state.isLoading -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = GabBlue) }

        vm.state.error != null -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(vm.state.error!!, color = GabError, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = vm::retry,
                    colors  = ButtonDefaults.buttonColors(containerColor = GabBlue)
                ) { Text("Tentar novamente", color = GabOnPrimary) }
            }
        }

        else -> GamificationContent(state = vm.state)
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
internal fun GamificationContent(state: GamificationUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeroSection(state = state)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            PointsReferenceCard()

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(
                title       = "Meus Badges",
                actionLabel = "${state.earnedCount} / ${state.badges.size} conquistados"
            )
            Spacer(modifier = Modifier.height(10.dp))
            BadgesSection(badges = state.badges)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Ranking Geral")
            Spacer(modifier = Modifier.height(10.dp))
            RankingSection(items = state.rankingList)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Histórico de Pontuação")
            Spacer(modifier = Modifier.height(10.dp))
            HistorySection(events = state.history)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Profile hero ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroSection(state: GamificationUiState) {
    val lv = state.level

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GabBlue)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {

            // Avatar + name + level badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(GabYellow)
                ) {
                    Text(
                        text       = state.userName.take(1).uppercase(),
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = GabTextDark
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text       = state.userName,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = GabOnPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(lv.badgeColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = "Nível ${lv.number} · ${lv.name}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = lv.textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Points + ranking
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "${state.points}",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = GabYellow
                    )
                    Text(
                        text  = "pontos totais",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.65f)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "#${state.rankingPosition}",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = GabOnPrimary
                    )
                    Text(
                        text  = "ranking geral",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.65f)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "${state.earnedCount}/${state.badges.size}",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = GabOnPrimary
                    )
                    Text(
                        text  = "badges",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Level progress
            if (!state.isMaxLevel) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Progresso para Nível ${lv.number + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.75f)
                    )
                    Text(
                        text       = "${state.ptsToNext} pts restantes",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = GabYellow
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress   = { state.progress },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color      = GabYellow,
                    trackColor = Color.White.copy(alpha = 0.22f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "${LEVEL_DEFS[lv.number - 1].minPoints} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.5f)
                    )
                    Text(
                        text  = "${LEVEL_DEFS[lv.number].minPoints} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnPrimary.copy(alpha = 0.5f)
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .background(GabYellow, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text       = "🏆 Nível máximo atingido!",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = GabTextDark
                    )
                }
            }
        }
    }
}

// ── Points reference card ──────────────────────────────────────────────────────

@Composable
private fun PointsReferenceCard() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = "Como Ganhar Pontos",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = GabTextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                Triple(Icons.Filled.Lightbulb,    "Cadastrar Ideia",            "+10 pts", GabBlue),
                Triple(Icons.Filled.Edit,          "Ideia Bem Descrita",          "+5 pts",  GabLightBlue),
                Triple(Icons.Filled.TrendingUp,    "Ideia Priorizada",           "+20 pts", Color(0xFFFF9800)),
                Triple(Icons.Filled.CheckCircle,   "Ideia Aprovada",             "+50 pts", GabGreen),
                Triple(Icons.Filled.RocketLaunch,  "Ideia Virou Projeto",       "+100 pts", Color(0xFF9C27B0)),
                Triple(Icons.Filled.EmojiEvents,   "Projeto Concluído c/ Sucesso","+200 pts", Color(0xFFFFB300))
            ).forEachIndexed { index, (icon, label, pts, color) ->
                PointRuleRow(icon = icon, action = label, points = pts, color = color)
                if (index < 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PointRuleRow(icon: ImageVector, action: String, points: String, color: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(17.dp)
                )
            }
            Text(
                text  = action,
                style = MaterialTheme.typography.bodySmall,
                color = GabTextDark
            )
        }
        Text(
            text       = points,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = color
        )
    }
}

// ── Badges section ─────────────────────────────────────────────────────────────

@Composable
private fun BadgesSection(badges: List<LocalBadge>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { badge ->
                    Column(modifier = Modifier.weight(1f)) {
                        BadgeCard(
                            title       = badge.name,
                            description = if (badge.earned && badge.earnedAt != null)
                                badge.earnedAt
                            else
                                badge.description,
                            icon        = badge.icon,
                            earned      = badge.earned
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Ranking section ────────────────────────────────────────────────────────────

@Composable
private fun RankingSection(items: List<LocalRankingItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            RankingCard(
                position      = item.position,
                name          = item.userName,
                score         = item.points,
                isCurrentUser = item.isCurrentUser
            )
        }
    }
}

// ── History section ────────────────────────────────────────────────────────────

@Composable
private fun HistorySection(events: List<ScoreEvent>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            events.forEachIndexed { index, event ->
                HistoryItem(event = event)
                if (index < events.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color    = GabSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(event: ScoreEvent) {
    val color = eventColor(event.type)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector        = eventIcon(event.type),
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = event.description,
                style    = MaterialTheme.typography.bodySmall,
                color    = GabTextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = event.date,
                style = MaterialTheme.typography.labelSmall,
                color = GabOnSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text       = "+${event.points} pts",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = color
        )
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Gamificação — Nível 3")
@Composable
private fun GamificationLevel3Preview() {
    GabInovaTheme {
        GamificationContent(
            state = GamificationUiState(
                userName        = "Operador Padrão",
                points          = 235,
                rankingPosition = 7,
                badges          = listOf(
                    LocalBadge("b1", "Primeira Ideia",         "Cadastrou sua primeira ideia",           Icons.Filled.Lightbulb,    true,  "15/03/2025"),
                    LocalBadge("b2", "Ideia Aprovada",          "Teve uma ideia aprovada",                Icons.Filled.CheckCircle,  true,  "02/04/2025"),
                    LocalBadge("b3", "Solução de Alto Impacto", "Ideia com alto impacto",                 Icons.Filled.TrendingUp,   true,  "02/04/2025"),
                    LocalBadge("b4", "Redução de Custos",       "Ideia na categoria Redução de Custo",    Icons.Filled.Savings,      false, null),
                    LocalBadge("b5", "Campeão Produtividade",   "Ganho validado em projeto",              Icons.Filled.EmojiEvents,  false, null),
                    LocalBadge("b6", "Top Inovador do Mês",     "Maior pontuação no mês",                 Icons.Filled.Star,         false, null),
                    LocalBadge("b7", "Ideia que Virou Projeto", "Ideia se tornou projeto oficial",        Icons.Filled.RocketLaunch, false, null)
                ),
                rankingList = listOf(
                    LocalRankingItem(1, "Mariana Costa",   480, "Comercial",  false),
                    LocalRankingItem(2, "Carlos Ferreira", 375, "Operações",  false),
                    LocalRankingItem(3, "Ana Lima",        310, "RH",         false),
                    LocalRankingItem(7, "Operador Padrão", 235, "GAB Inova",  true),
                    LocalRankingItem(8, "Luciana Melo",    190, "Atendimento",false)
                ),
                history = listOf(
                    ScoreEvent(50, "Ideia aprovada: Checklist digital para embarque",          "02/04/2025", ScoreEventType.IDEA_APPROVED),
                    ScoreEvent(20, "Ideia priorizada: Sistema de monitoramento de frota",       "18/03/2025", ScoreEventType.IDEA_PRIORITIZED),
                    ScoreEvent(10, "Nova ideia cadastrada: Checklist digital para embarque",    "10/03/2025", ScoreEventType.IDEA_CREATED),
                    ScoreEvent(5,  "Descrição detalhada: Checklist digital para embarque",      "10/03/2025", ScoreEventType.IDEA_DESCRIBED)
                )
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Gamificação — Nível 5 (Máximo)")
@Composable
private fun GamificationLevel5Preview() {
    GabInovaTheme {
        GamificationContent(
            state = GamificationUiState(
                userName        = "Líder Estratégico",
                points          = 620,
                rankingPosition = 1,
                badges          = listOf(
                    LocalBadge("b1", "Primeira Ideia",         "15/01/2025", Icons.Filled.Lightbulb,    true, "15/01/2025"),
                    LocalBadge("b2", "Ideia Aprovada",          "20/01/2025", Icons.Filled.CheckCircle,  true, "20/01/2025"),
                    LocalBadge("b3", "Solução de Alto Impacto", "20/01/2025", Icons.Filled.TrendingUp,   true, "20/01/2025"),
                    LocalBadge("b4", "Redução de Custos",       "01/02/2025", Icons.Filled.Savings,      true, "01/02/2025"),
                    LocalBadge("b5", "Campeão Produtividade",   "15/02/2025", Icons.Filled.EmojiEvents,  true, "15/02/2025"),
                    LocalBadge("b6", "Top Inovador do Mês",     "01/03/2025", Icons.Filled.Star,         true, "01/03/2025"),
                    LocalBadge("b7", "Ideia que Virou Projeto", "01/04/2025", Icons.Filled.RocketLaunch, true, "01/04/2025")
                ),
                rankingList = listOf(
                    LocalRankingItem(1, "Líder Estratégico", 620, "Estratégia", true),
                    LocalRankingItem(2, "Mariana Costa",     480, "Comercial",  false),
                    LocalRankingItem(3, "Carlos Ferreira",   375, "Operações",  false)
                ),
                history = listOf(
                    ScoreEvent(200, "Projeto concluído: Plataforma de Treinamento",    "30/04/2025", ScoreEventType.PROJECT_COMPLETED),
                    ScoreEvent(100, "Ideia virou projeto: Checklist digital",           "01/03/2025", ScoreEventType.IDEA_TO_PROJECT),
                    ScoreEvent(50,  "Ideia aprovada: Checklist digital",               "20/01/2025", ScoreEventType.IDEA_APPROVED)
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Gamificação — Histórico e Regras")
@Composable
private fun GamificationCardsPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PointsReferenceCard()
        }
    }
}
