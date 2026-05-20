package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.repository.GamificationRepository
import br.com.fiap.gabinova.session.SessionManager

enum class ScoreEventType {
    IDEA_CREATED,
    IDEA_DESCRIBED,
    IDEA_PRIORITIZED,
    IDEA_APPROVED,
    IDEA_TO_PROJECT,
    PROJECT_COMPLETED
}

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

data class GamificationUiState(
    val userName: String = "Operador",
    val userId: String = "1",
    val points: Int = 3250,
    val rankingPosition: Int = 3,
    val badges: List<LocalBadge> = emptyList(),
    val rankingList: List<LocalRankingItem> = emptyList(),
    val history: List<ScoreEvent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val earnedCount: Int get() = badges.count { it.earned }
}

class GamificationViewModel(
    private val sessionManager: SessionManager,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    var state by mutableStateOf(GamificationUiState())
        private set

    init {
        loadMockData()
    }

    fun retry() {
        loadMockData()
    }

    private fun loadMockData() {
        val badges = listOf(
            LocalBadge(
                id = "1",
                name = "Primeira Ideia",
                description = "Enviou sua primeira contribuição para o funil de inovação.",
                icon = Icons.Filled.Lightbulb,
                earned = true,
                earnedAt = "02/05/2026"
            ),
            LocalBadge(
                id = "2",
                name = "Ideia Aprovada",
                description = "Teve uma ideia validada por um gestor.",
                icon = Icons.Filled.CheckCircle,
                earned = true,
                earnedAt = "05/05/2026"
            ),
            LocalBadge(
                id = "3",
                name = "Alto Impacto Estratégico",
                description = "Contribuiu com uma ideia alinhada aos objetivos estratégicos do Grupo.",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                earned = true,
                earnedAt = "08/05/2026"
            ),
            LocalBadge(
                id = "4",
                name = "Redução de Custo",
                description = "Propôs uma solução com potencial de economia operacional.",
                icon = Icons.Filled.Savings,
                earned = true,
                earnedAt = "10/05/2026"
            ),
            LocalBadge(
                id = "5",
                name = "Ideia Virou Projeto",
                description = "Teve uma ideia transformada em iniciativa estruturada.",
                icon = Icons.Filled.RocketLaunch,
                earned = true,
                earnedAt = "12/05/2026"
            ),
            LocalBadge(
                id = "6",
                name = "Resultado Mensurável",
                description = "Participou de um projeto com impacto comprovado em produtividade, custo ou eficiência.",
                icon = Icons.Filled.EmojiEvents,
                earned = false
            ),
            LocalBadge(
                id = "7",
                name = "ROI Positivo",
                description = "Ideia implementada com retorno financeiro positivo para o negócio.",
                icon = Icons.Filled.Star,
                earned = false
            )
        )

        val ranking = listOf(
            LocalRankingItem(
                position = 1,
                userName = "Fernanda Lima",
                points = 8200,
                department = "Operações",
                isCurrentUser = false
            ),
            LocalRankingItem(
                position = 2,
                userName = "Carlos Souza",
                points = 6400,
                department = "Tecnologia",
                isCurrentUser = false
            ),
            LocalRankingItem(
                position = 3,
                userName = "Operador",
                points = 3250,
                department = "Operações",
                isCurrentUser = true
            ),
            LocalRankingItem(
                position = 4,
                userName = "Mariana Alves",
                points = 2850,
                department = "RH",
                isCurrentUser = false
            ),
            LocalRankingItem(
                position = 5,
                userName = "João Pereira",
                points = 2100,
                department = "Logística",
                isCurrentUser = false
            )
        )

        val history = listOf(
            ScoreEvent(
                points = 2000,
                description = "Projeto concluído com ganho operacional mensurável",
                date = "13/05/2026",
                type = ScoreEventType.PROJECT_COMPLETED
            ),
            ScoreEvent(
                points = 1000,
                description = "Ideia transformada em projeto estratégico",
                date = "12/05/2026",
                type = ScoreEventType.IDEA_TO_PROJECT
            ),
            ScoreEvent(
                points = 500,
                description = "Ideia aprovada com potencial de redução operacional",
                date = "10/05/2026",
                type = ScoreEventType.IDEA_APPROVED
            ),
            ScoreEvent(
                points = 300,
                description = "Ideia priorizada pelo gestor por alto alinhamento estratégico",
                date = "08/05/2026",
                type = ScoreEventType.IDEA_PRIORITIZED
            ),
            ScoreEvent(
                points = 100,
                description = "Descrição completa do problema e do impacto esperado",
                date = "05/05/2026",
                type = ScoreEventType.IDEA_DESCRIBED
            ),
            ScoreEvent(
                points = 50,
                description = "Ideia enviada: Checklist digital de ônibus",
                date = "02/05/2026",
                type = ScoreEventType.IDEA_CREATED
            )
        )

        state = state.copy(
            userName = "Operador",
            userId = "1",
            points = 3250,
            rankingPosition = 3,
            badges = badges,
            rankingList = ranking,
            history = history,
            isLoading = false,
            error = null
        )
    }
}

class GamificationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GamificationViewModel(
            SessionManager(context),
            GamificationRepository(RetrofitClient.api)
        ) as T
}