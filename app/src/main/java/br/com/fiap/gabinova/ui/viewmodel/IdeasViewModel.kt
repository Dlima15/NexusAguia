package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.IdeaStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.IdeasRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

val IDEA_SECTORS = listOf(
    "Operações",
    "Tecnologia",
    "RH",
    "Financeiro",
    "Comercial",
    "Logística",
    "Atendimento",
    "Jurídico"
)

val IDEA_CATEGORIES = listOf(
    "Melhoria de Processo",
    "Redução de Custo",
    "Experiência do Cliente",
    "Inovação Tecnológica",
    "Engajamento",
    "Segurança"
)

data class IdeaItem(
    val id: String,
    val title: String,
    val description: String,
    val sector: String,
    val category: String,
    val expectedImpact: String,
    val urgency: Int,
    val status: IdeaStatus,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val score: Int
)

data class IdeasUiState(
    val ideas: List<IdeaItem> = emptyList(),
    val userRole: UserRole = UserRole.COLLABORATOR,
    val currentUserId: String = "1",
    val currentUserName: String = "Operador",
    val selectedStatus: IdeaStatus? = null,
    val isFormVisible: Boolean = false,
    val formTitle: String = "",
    val formDescription: String = "",
    val formSector: String = "",
    val formCategory: String = "",
    val formExpectedImpact: String = "",
    val formUrgency: Int = 3,
    val formError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val visibleIdeas: List<IdeaItem>
        get() {
            val base = if (userRole == UserRole.COLLABORATOR) {
                ideas.filter { it.authorId == currentUserId }
            } else {
                ideas
            }

            val filtered = if (selectedStatus != null) {
                base.filter { it.status == selectedStatus }
            } else {
                base
            }

            return if (userRole == UserRole.MANAGER) {
                filtered.sortedByDescending { it.score }
            } else {
                filtered
            }
        }

    val isCollaborator: Boolean
        get() = userRole == UserRole.COLLABORATOR

    val isManager: Boolean
        get() = userRole == UserRole.MANAGER
}

class IdeasViewModel(
    private val sessionManager: SessionManager,
    private val ideasRepository: IdeasRepository
) : ViewModel() {

    var state by mutableStateOf(IdeasUiState())
        private set

    init {
        observeUserSession()
        loadMockIdeas()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { roleStr ->
                val role = runCatching {
                    UserRole.valueOf(roleStr)
                }.getOrDefault(UserRole.COLLABORATOR)

                state = state.copy(userRole = role)
            }
        }

        viewModelScope.launch {
            sessionManager.userNameFlow.collect { name ->
                state = state.copy(
                    currentUserName = name.ifBlank {
                        if (state.userRole == UserRole.MANAGER) "Gestor" else "Operador"
                    }
                )
            }
        }
    }

    fun retry() {
        loadMockIdeas()
    }

    private fun loadMockIdeas() {
        val mockIdeas = listOf(
            createMockIdea(
                id = "1",
                title = "Checklist digital de ônibus",
                description = "Substituir checklist manual por um processo digital para reduzir falhas e melhorar a rastreabilidade.",
                sector = "Operações",
                category = "Melhoria de Processo",
                expectedImpact = "Mais agilidade na inspeção dos veículos e redução de falhas operacionais",
                urgency = 5,
                status = IdeaStatus.IN_REVIEW,
                authorId = "1",
                authorName = "Operador",
                createdAt = "11/05/2026"
            ),
            createMockIdea(
                id = "2",
                title = "Painel de indicadores operacionais",
                description = "Criar dashboard para acompanhar produtividade, custos e oportunidades de melhoria.",
                sector = "Tecnologia",
                category = "Inovação Tecnológica",
                expectedImpact = "Apoio à decisão dos gestores e maior visibilidade dos resultados",
                urgency = 4,
                status = IdeaStatus.APPROVED,
                authorId = "2",
                authorName = "Gestor",
                createdAt = "10/05/2026"
            ),
            createMockIdea(
                id = "3",
                title = "Programa de gamificação",
                description = "Pontuar colaboradores que enviarem ideias alinhadas à estratégia da empresa.",
                sector = "RH",
                category = "Engajamento",
                expectedImpact = "Maior participação dos colaboradores e fortalecimento da cultura de inovação",
                urgency = 3,
                status = IdeaStatus.PENDING,
                authorId = "1",
                authorName = "Operador",
                createdAt = "09/05/2026"
            )
        )

        state = state.copy(
            isLoading = false,
            error = null,
            ideas = mockIdeas
        )
    }

    private fun createMockIdea(
        id: String,
        title: String,
        description: String,
        sector: String,
        category: String,
        expectedImpact: String,
        urgency: Int,
        status: IdeaStatus,
        authorId: String,
        authorName: String,
        createdAt: String
    ): IdeaItem {
        val ideaWithoutScore = IdeaItem(
            id = id,
            title = title,
            description = description,
            sector = sector,
            category = category,
            expectedImpact = expectedImpact,
            urgency = urgency,
            status = status,
            authorId = authorId,
            authorName = authorName,
            createdAt = createdAt,
            score = 0
        )

        return ideaWithoutScore.copy(
            score = calculateNexusScore(ideaWithoutScore)
        )
    }

    private fun calculateNexusScore(idea: IdeaItem): Int {
        val urgencyScore = idea.urgency * 10

        val categoryScore = when (idea.category) {
            "Redução de Custo" -> 25
            "Melhoria de Processo" -> 22
            "Inovação Tecnológica" -> 20
            "Segurança" -> 20
            "Experiência do Cliente" -> 18
            "Engajamento" -> 15
            else -> 10
        }

        val sectorScore = when (idea.sector) {
            "Operações" -> 20
            "Logística" -> 18
            "Tecnologia" -> 16
            "Atendimento" -> 15
            "Financeiro" -> 14
            "RH" -> 12
            "Comercial" -> 12
            "Jurídico" -> 10
            else -> 8
        }

        val impactScore = when {
            idea.expectedImpact.contains("redução", ignoreCase = true) -> 15
            idea.expectedImpact.contains("economia", ignoreCase = true) -> 15
            idea.expectedImpact.contains("produtividade", ignoreCase = true) -> 15
            idea.expectedImpact.contains("agilidade", ignoreCase = true) -> 12
            idea.expectedImpact.contains("participação", ignoreCase = true) -> 10
            idea.expectedImpact.isNotBlank() -> 8
            else -> 0
        }

        val descriptionScore = when {
            idea.description.length >= 80 -> 10
            idea.description.length >= 40 -> 6
            idea.description.isNotBlank() -> 3
            else -> 0
        }

        return (urgencyScore + categoryScore + sectorScore + impactScore + descriptionScore)
            .coerceIn(0, 100)
    }

    fun onStatusFilter(status: IdeaStatus?) {
        state = state.copy(selectedStatus = status)
    }

    fun showCreateForm() {
        if (!state.isCollaborator) return

        state = state.copy(
            isFormVisible = true,
            formTitle = "",
            formDescription = "",
            formSector = "",
            formCategory = "",
            formExpectedImpact = "",
            formUrgency = 3,
            formError = null
        )
    }

    fun hideForm() {
        state = state.copy(
            isFormVisible = false,
            formError = null
        )
    }

    fun onTitleChange(v: String) {
        state = state.copy(
            formTitle = v,
            formError = null
        )
    }

    fun onDescriptionChange(v: String) {
        state = state.copy(formDescription = v)
    }

    fun onSectorChange(v: String) {
        state = state.copy(
            formSector = v,
            formError = null
        )
    }

    fun onCategoryChange(v: String) {
        state = state.copy(
            formCategory = v,
            formError = null
        )
    }

    fun onExpectedImpactChange(v: String) {
        state = state.copy(formExpectedImpact = v)
    }

    fun onUrgencyChange(v: Int) {
        state = state.copy(formUrgency = v)
    }

    fun saveIdea() {
        if (!state.isCollaborator) return

        when {
            state.formTitle.isBlank() -> {
                state = state.copy(formError = "Título é obrigatório.")
                return
            }

            state.formSector.isBlank() -> {
                state = state.copy(formError = "Selecione um setor.")
                return
            }

            state.formCategory.isBlank() -> {
                state = state.copy(formError = "Selecione uma categoria.")
                return
            }
        }

        val ideaWithoutScore = IdeaItem(
            id = System.currentTimeMillis().toString(),
            title = state.formTitle.trim(),
            description = state.formDescription.trim(),
            sector = state.formSector,
            category = state.formCategory,
            expectedImpact = state.formExpectedImpact.trim(),
            urgency = state.formUrgency,
            status = IdeaStatus.PENDING,
            authorId = state.currentUserId,
            authorName = state.currentUserName,
            createdAt = "13/05/2026",
            score = 0
        )

        val newIdea = ideaWithoutScore.copy(
            score = calculateNexusScore(ideaWithoutScore)
        )

        state = state.copy(
            ideas = listOf(newIdea) + state.ideas,
            isFormVisible = false,
            isLoading = false,
            formError = null,
            formTitle = "",
            formDescription = "",
            formSector = "",
            formCategory = "",
            formExpectedImpact = "",
            formUrgency = 3
        )
    }

    fun approveIdea(id: String) {
        if (!state.isManager) return

        state = state.copy(
            ideas = state.ideas.map { idea ->
                if (idea.id == id) {
                    idea.copy(
                        status = IdeaStatus.APPROVED,
                        score = (idea.score + 5).coerceAtMost(100)
                    )
                } else {
                    idea
                }
            }
        )
    }

    fun rejectIdea(id: String) {
        if (!state.isManager) return

        state = state.copy(
            ideas = state.ideas.map { idea ->
                if (idea.id == id) {
                    idea.copy(status = IdeaStatus.REJECTED)
                } else {
                    idea
                }
            }
        )
    }

    fun prioritizeIdea(id: String) {
        if (!state.isManager) return

        state = state.copy(
            ideas = state.ideas.map { idea ->
                if (idea.id == id) {
                    idea.copy(
                        status = IdeaStatus.IN_REVIEW,
                        score = (idea.score + 10).coerceAtMost(100)
                    )
                } else {
                    idea
                }
            }
        )
    }
}

class IdeasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IdeasViewModel(
            SessionManager(context),
            IdeasRepository(RetrofitClient.api)
        ) as T
}