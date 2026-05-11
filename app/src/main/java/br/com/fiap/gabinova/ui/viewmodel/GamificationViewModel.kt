package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.BadgeDto
import br.com.fiap.gabinova.data.remote.dto.RankingDto
import br.com.fiap.gabinova.data.remote.dto.ScoreHistoryDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.repository.GamificationRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// ── Score event type ───────────────────────────────────────────────────────────

enum class ScoreEventType {
    IDEA_CREATED, IDEA_DESCRIBED, IDEA_PRIORITIZED,
    IDEA_APPROVED, IDEA_TO_PROJECT, PROJECT_COMPLETED
}

// ── Screen models ──────────────────────────────────────────────────────────────

data class ScoreEvent(
    val points: Int,
    val description: String,
    val date: String,
    val type: ScoreEventType
)

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
    val rankingPosition: Int                = 0,
    val badges: List<LocalBadge>            = emptyList(),
    val rankingList: List<LocalRankingItem> = emptyList(),
    val history: List<ScoreEvent>           = emptyList(),
    val isLoading: Boolean                  = false,
    val error: String?                      = null
) {
    val earnedCount: Int get() = badges.count { it.earned }
}

// ── DTO mappers ────────────────────────────────────────────────────────────────

private fun badgeIcon(name: String): ImageVector = when {
    name.contains("Primeira",     ignoreCase = true) -> Icons.Filled.Lightbulb
    name.contains("Aprovada",     ignoreCase = true) -> Icons.Filled.CheckCircle
    name.contains("Impacto",      ignoreCase = true) -> Icons.AutoMirrored.Filled.TrendingUp
    name.contains("Custo",        ignoreCase = true) -> Icons.Filled.Savings
    name.contains("Produtividade", ignoreCase = true)-> Icons.Filled.EmojiEvents
    name.contains("Inovador",     ignoreCase = true) -> Icons.Filled.Star
    name.contains("Projeto",      ignoreCase = true) -> Icons.Filled.RocketLaunch
    else                                              -> Icons.Filled.Star
}

private fun BadgeDto.toLocalBadge() = LocalBadge(
    id          = id,
    name        = name,
    description = if (earned && earnedAt != null) earnedAt else name,
    icon        = badgeIcon(name),
    earned      = earned,
    earnedAt    = earnedAt
)

private fun ScoreHistoryDto.toScoreEvent() = ScoreEvent(
    points      = points,
    description = description,
    date        = date,
    type        = runCatching { ScoreEventType.valueOf(eventType) }
        .getOrDefault(ScoreEventType.IDEA_CREATED)
)

private fun RankingDto.toLocalRanking(currentUserId: String) = LocalRankingItem(
    position      = position,
    userName      = userName,
    points        = points,
    department    = department,
    isCurrentUser = userId == currentUserId
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

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
        val gamResult  = gamificationRepository.getGamification(userId)
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

class GamificationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GamificationViewModel(SessionManager(context), GamificationRepository(RetrofitClient.api)) as T
}
