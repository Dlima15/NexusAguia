package br.com.fiap.gabinova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
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
import br.com.fiap.gabinova.ui.theme.*
import br.com.fiap.gabinova.ui.viewmodel.*


private fun eventColor(type: ScoreEventType): Color = when (type) {
    ScoreEventType.IDEA_CREATED -> GabBlue
    ScoreEventType.IDEA_DESCRIBED -> GabLightBlue
    ScoreEventType.IDEA_PRIORITIZED -> Color(0xFFFF9800)
    ScoreEventType.IDEA_APPROVED -> GabGreen
    ScoreEventType.IDEA_TO_PROJECT -> Color(0xFF9C27B0)
    ScoreEventType.PROJECT_COMPLETED -> Color(0xFFFFB300)
}

private fun eventIcon(type: ScoreEventType): ImageVector = when (type) {
    ScoreEventType.IDEA_CREATED -> Icons.Filled.Lightbulb
    ScoreEventType.IDEA_DESCRIBED -> Icons.Filled.Edit
    ScoreEventType.IDEA_PRIORITIZED -> Icons.AutoMirrored.Filled.TrendingUp
    ScoreEventType.IDEA_APPROVED -> Icons.Filled.CheckCircle
    ScoreEventType.IDEA_TO_PROJECT -> Icons.Filled.RocketLaunch
    ScoreEventType.PROJECT_COMPLETED -> Icons.Filled.EmojiEvents
}

private data class LevelDef(
    val number: Int,
    val name: String,
    val minPoints: Int,
    val maxPoints: Int,
    val badgeColor: Color,
    val textColor: Color
)

private val LEVEL_DEFS = listOf(
    LevelDef(1, "Explorador Nexus", 0, 1499, Color(0xFFB3E5FC), GabBlue),
    LevelDef(2, "Inovador Operacional", 1500, 2999, Color(0xFFC8E6C9), GabGreen),
    LevelDef(3, "Especialista Nexus", 3000, 4999, Color(0xFFFFE0B2), Color(0xFFE65100)),
    LevelDef(4, "Referência em Inovação", 5000, 6999, GabYellow, GabTextDark),
    LevelDef(5, "Embaixador da Inovação", 7000, Int.MAX_VALUE, Color(0xFFE1BEE7), Color(0xFF6A1B9A))
)

private data class RewardDef(
    val title: String,
    val points: Int,
    val description: String,
    val icon: ImageVector
)

private val REWARDS = listOf(
    RewardDef("Meio período de folga", 1500, "Reconhecimento pela participação ativa no programa.", Icons.Filled.WorkspacePremium),
    RewardDef("1 dia de folga", 3000, "Benefício para colaboradores com ideias aprovadas e participação consistente.", Icons.Filled.EmojiEvents),
    RewardDef("Passagem Águia Branca", 5000, "Recompensa alinhada ao negócio e à identidade do Grupo.", Icons.Filled.CardTravel),
    RewardDef("Reconhecimento executivo", 7000, "Destaque para ideias implementadas com impacto mensurável.", Icons.Filled.Star)
)

private fun levelOf(points: Int): LevelDef =
    LEVEL_DEFS.lastOrNull { points >= it.minPoints } ?: LEVEL_DEFS.first()

private fun levelProgress(points: Int): Float {
    val cur = levelOf(points)
    val curIdx = LEVEL_DEFS.indexOf(cur)
    if (curIdx == LEVEL_DEFS.size - 1) return 1f

    val next = LEVEL_DEFS[curIdx + 1]
    val inLevel = (points - cur.minPoints).toFloat()
    val span = (next.minPoints - cur.minPoints).toFloat()

    return (inLevel / span).coerceIn(0f, 1f)
}

private fun nextReward(points: Int): RewardDef? =
    REWARDS.firstOrNull { points < it.points }

@Composable
fun GamificationScreen() {
    val context = LocalContext.current
    val vm: GamificationViewModel = viewModel(factory = GamificationViewModelFactory(context))

    Surface(modifier = Modifier.fillMaxSize(), color = GabBackground) {
        when {
            vm.state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GabBlue)
                }
            }

            vm.state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(vm.state.error!!, color = GabError, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = vm::retry) {
                        Text("Tentar novamente")
                    }
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
            RecognitionIntroCard()

            Spacer(modifier = Modifier.height(16.dp))
            NextRewardCard(points = state.points)

            Spacer(modifier = Modifier.height(16.dp))
            RewardsCatalogCard(points = state.points)

            Spacer(modifier = Modifier.height(16.dp))
            PointsReferenceCard()

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Conquistas Nexus")
            BadgesSection(badges = state.badges)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Ranking de Inovação")
            RankingSection(items = state.rankingList)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Histórico de Impacto")
            HistorySection(events = state.history)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeroSection(state: GamificationUiState) {
    val lv = levelOf(state.points)

    Box(
        modifier = Modifier
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.userName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Programa Nexus de Reconhecimento",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(shape = RoundedCornerShape(6.dp), color = lv.badgeColor) {
                        Text(
                            text = lv.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = lv.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "${state.points} pontos Nexus",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Suas ideias ajudam a transformar desafios operacionais em valor real para o Grupo Águia Branca.",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

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
private fun RecognitionIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = GabBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reconhecimento que gera engajamento",
                    fontWeight = FontWeight.Bold,
                    color = GabTextDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "O Nexus valoriza colaboradores que contribuem com ideias, melhorias e projetos capazes de gerar eficiência, economia e produtividade.",
                style = MaterialTheme.typography.bodySmall,
                color = GabTextDark
            )
        }
    }
}

@Composable
private fun NextRewardCard(points: Int) {
    val reward = nextReward(points)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabBlue),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Próxima recompensa",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (reward != null) {
                val remaining = reward.points - points

                Text(
                    text = reward.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Faltam $remaining pontos para desbloquear este benefício.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Você atingiu o nível máximo de reconhecimento.",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Continue contribuindo para ampliar seu impacto dentro da inovação corporativa.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RewardsCatalogCard(points: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Catálogo de recompensas",
                fontWeight = FontWeight.Bold,
                color = GabTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            REWARDS.forEach { reward ->
                val unlocked = points >= reward.points

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (unlocked) GabGreen.copy(alpha = 0.15f) else GabBlue.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = reward.icon,
                            contentDescription = null,
                            tint = if (unlocked) GabGreen else GabBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reward.title,
                            fontWeight = FontWeight.Bold,
                            color = GabTextDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = reward.description,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "${reward.points} pts",
                        fontWeight = FontWeight.Bold,
                        color = if (unlocked) GabGreen else GabBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesSection(badges: List<LocalBadge>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        badges.forEach { badge ->
            BadgeCard(
                title = badge.name,
                description = badge.description,
                icon = badge.icon,
                earned = badge.earned
            )
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
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Como ganhar pontos",
                fontWeight = FontWeight.Bold,
                color = GabTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            val rules = listOf(
                "Enviar uma nova ideia" to "+50",
                "Descrever o problema com clareza" to "+100",
                "Ideia priorizada pelo gestor" to "+300",
                "Ideia aprovada" to "+500",
                "Ideia transformada em projeto" to "+1000",
                "Projeto concluído com resultado mensurável" to "+2000"
            )

            rules.forEach { (label, pts) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = GabTextDark,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = pts,
                        fontWeight = FontWeight.Bold,
                        color = GabBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(events: List<ScoreEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            ImpactExampleItem()

            if (events.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

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
private fun ImpactExampleItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.RocketLaunch,
            contentDescription = null,
            tint = GabGreen,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sua ideia pode gerar impacto real",
                fontWeight = FontWeight.Bold,
                color = GabTextDark,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Exemplo: redução de 18% no tempo operacional, economia estimada e ganho de produtividade para a área.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Text(
            text = "+2000",
            fontWeight = FontWeight.Bold,
            color = GabGreen
        )
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
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = GabTextDark
            )

            Text(
                text = event.date,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Text(
            text = "+${event.points}",
            fontWeight = FontWeight.Bold,
            color = eventColor(event.type)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GamificationPreview() {
    GabInovaTheme {
        GamificationContent(
            state = GamificationUiState(
                userName = "Usuário Teste",
                points = 3250,
                rankingPosition = 12,
                badges = listOf(
                    LocalBadge("1", "Pioneiro", "Primeira ideia enviada", Icons.Filled.Lightbulb, true, "10/10"),
                    LocalBadge("2", "Ideia Aprovada", "Contribuição validada pelo gestor", Icons.Filled.CheckCircle, true, "12/10"),
                    LocalBadge("3", "Alto Impacto", "Ideia com potencial de resultado mensurável", Icons.Filled.Star, false, null)
                ),
                rankingList = listOf(
                    LocalRankingItem(1, "Ana Paula", 5200, "Operações", false),
                    LocalRankingItem(2, "Carlos Silva", 4100, "Logística", false),
                    LocalRankingItem(12, "Usuário Teste", 3250, "TI", true)
                ),
                history = listOf(
                    ScoreEvent(50, "Nova ideia enviada para melhoria operacional", "12/10", ScoreEventType.IDEA_CREATED),
                    ScoreEvent(500, "Ideia aprovada pelo gestor", "14/10", ScoreEventType.IDEA_APPROVED),
                    ScoreEvent(1000, "Ideia transformada em projeto", "18/10", ScoreEventType.IDEA_TO_PROJECT)
                )
            )
        )
    }
}