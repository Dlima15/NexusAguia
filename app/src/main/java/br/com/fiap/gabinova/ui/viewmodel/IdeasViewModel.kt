package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.IdeaDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.IdeaStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.IdeasRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

val IDEA_SECTORS = listOf(
    "Operações", "Tecnologia", "RH", "Financeiro",
    "Comercial", "Logística", "Atendimento", "Jurídico"
)

val IDEA_CATEGORIES = listOf(
    "Melhoria de Processo", "Redução de Custo", "Experiência do Cliente",
    "Inovação Tecnológica", "Engajamento", "Segurança"
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
    val ideas: List<IdeaItem>   = emptyList(),
    val userRole: UserRole      = UserRole.COLLABORATOR,
    val currentUserId: String   = "",
    val currentUserName: String = "",
    val selectedStatus: IdeaStatus? = null,
    val isFormVisible: Boolean  = false,
    val formTitle: String       = "",
    val formDescription: String = "",
    val formSector: String      = "",
    val formCategory: String    = "",
    val formExpectedImpact: String = "",
    val formUrgency: Int        = 3,
    val formError: String?      = null,
    val isLoading: Boolean      = false,
    val error: String?          = null
) {
    val visibleIdeas: List<IdeaItem>
        get() {
            val base = if (userRole == UserRole.COLLABORATOR)
                ideas.filter { it.authorId == currentUserId }
            else
                ideas
            return if (selectedStatus != null) base.filter { it.status == selectedStatus } else base
        }

    val isCollaborator get() = userRole == UserRole.COLLABORATOR
    val isManager      get() = userRole == UserRole.MANAGER
}

private fun IdeaDto.toIdeaItem() = IdeaItem(
    id             = id,
    title          = title,
    description    = description,
    sector         = sector,
    category       = category,
    expectedImpact = expectedImpact,
    urgency        = urgency,
    status         = runCatching { IdeaStatus.valueOf(status) }.getOrDefault(IdeaStatus.PENDING),
    authorId       = authorId,
    authorName     = authorName,
    createdAt      = createdAt,
    score          = score
)

class IdeasViewModel(
    private val sessionManager: SessionManager,
    private val ideasRepository: IdeasRepository
) : ViewModel() {

    var state by mutableStateOf(IdeasUiState())
        private set

    init {
        viewModelScope.launch {
            combine(
                sessionManager.userRoleFlow,
                sessionManager.userIdFlow,
                sessionManager.userNameFlow
            ) { roleStr, userId, userName ->
                Triple(
                    runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.COLLABORATOR),
                    userId,
                    userName
                )
            }.collect { (role, userId, userName) ->
                state = state.copy(
                    userRole        = role,
                    currentUserId   = userId,
                    currentUserName = userName
                )
                loadIdeas(role, userId)
            }
        }
    }

    fun retry() { viewModelScope.launch { loadIdeas(state.userRole, state.currentUserId) } }

    private suspend fun loadIdeas(role: UserRole, userId: String) {
        state = state.copy(isLoading = true, error = null)
        val result = if (role == UserRole.COLLABORATOR && userId.isNotBlank())
            ideasRepository.getIdeasByUser(userId)
        else
            ideasRepository.getIdeas()
        when (result) {
            is ApiResult.Success -> state = state.copy(
                isLoading = false,
                ideas     = result.data.map { it.toIdeaItem() }
            )
            is ApiResult.Error   -> state = state.copy(isLoading = false, error = result.message)
        }
    }

    fun onStatusFilter(status: IdeaStatus?) { state = state.copy(selectedStatus = status) }

    fun showCreateForm() {
        state = state.copy(
            isFormVisible      = true,
            formTitle          = "",
            formDescription    = "",
            formSector         = "",
            formCategory       = "",
            formExpectedImpact = "",
            formUrgency        = 3,
            formError          = null
        )
    }

    fun hideForm() { state = state.copy(isFormVisible = false, formError = null) }

    fun onTitleChange(v: String)          { state = state.copy(formTitle = v, formError = null) }
    fun onDescriptionChange(v: String)    { state = state.copy(formDescription = v) }
    fun onSectorChange(v: String)         { state = state.copy(formSector = v, formError = null) }
    fun onCategoryChange(v: String)       { state = state.copy(formCategory = v, formError = null) }
    fun onExpectedImpactChange(v: String) { state = state.copy(formExpectedImpact = v) }
    fun onUrgencyChange(v: Int)           { state = state.copy(formUrgency = v) }

    fun saveIdea() {
        when {
            state.formTitle.isBlank()    -> { state = state.copy(formError = "Título é obrigatório."); return }
            state.formSector.isBlank()   -> { state = state.copy(formError = "Selecione um setor."); return }
            state.formCategory.isBlank() -> { state = state.copy(formError = "Selecione uma categoria."); return }
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true, formError = null)
            val result = ideasRepository.createIdea(
                title          = state.formTitle.trim(),
                description    = state.formDescription.trim(),
                sector         = state.formSector,
                category       = state.formCategory,
                expectedImpact = state.formExpectedImpact.trim(),
                urgency        = state.formUrgency
            )
            when (result) {
                is ApiResult.Success -> state = state.copy(
                    isLoading     = false,
                    ideas         = listOf(result.data.toIdeaItem()) + state.ideas,
                    isFormVisible = false
                )
                is ApiResult.Error -> state = state.copy(isLoading = false, formError = result.message)
            }
        }
    }

    fun approveIdea(id: String) {
        state = state.copy(ideas = state.ideas.map {
            if (it.id == id) it.copy(status = IdeaStatus.APPROVED, score = it.score + 50) else it
        })
        viewModelScope.launch { ideasRepository.updateStatus(id, "APPROVED") }
    }

    fun rejectIdea(id: String) {
        state = state.copy(ideas = state.ideas.map {
            if (it.id == id) it.copy(status = IdeaStatus.REJECTED) else it
        })
        viewModelScope.launch { ideasRepository.updateStatus(id, "REJECTED") }
    }

    fun prioritizeIdea(id: String) {
        state = state.copy(ideas = state.ideas.map {
            if (it.id == id) it.copy(status = IdeaStatus.IN_REVIEW, score = it.score + 20) else it
        })
        viewModelScope.launch { ideasRepository.updatePriority(id, "HIGH") }
    }
}

class IdeasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IdeasViewModel(SessionManager(context), IdeasRepository(RetrofitClient.api)) as T
}
