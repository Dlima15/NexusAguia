package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.ProjectDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.ProjectStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.ProjectsRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

val PROJECT_STAGES = listOf(
    "Planejamento", "Levantamento", "Desenvolvimento", "Testes", "Implantação", "Encerramento"
)

data class ProjectItem(
    val id: String,
    val title: String,
    val ideaOrigin: String,
    val responsible: String,
    val status: ProjectStatus,
    val stage: String,
    val investment: String,
    val expectedReturn: String,
    val actualReturn: String,
    val productivity: String,
    val progress: Int,
    val deadline: String,
    val createdAt: String
)

data class ProjectsUiState(
    val projects: List<ProjectItem>   = emptyList(),
    val userRole: UserRole            = UserRole.COLLABORATOR,
    val selectedStatus: ProjectStatus? = null,
    val isLoading: Boolean            = false,
    val error: String?                = null,
    val isFormVisible: Boolean        = false,
    val editingId: String?            = null,
    val formTitle: String             = "",
    val formIdeaOrigin: String        = "",
    val formResponsible: String       = "",
    val formStatus: ProjectStatus     = ProjectStatus.PLANNING,
    val formStage: String             = "",
    val formInvestment: String        = "",
    val formExpectedReturn: String    = "",
    val formActualReturn: String      = "",
    val formProductivity: String      = "",
    val formProgress: Float           = 0f,
    val formDeadline: String          = "",
    val formError: String?            = null
) {
    val visibleProjects: List<ProjectItem>
        get() = if (selectedStatus != null)
            projects.filter { it.status == selectedStatus }
        else
            projects

    val isManager       get() = userRole == UserRole.MANAGER
    val canEdit         get() = isManager
    val isEditing       get() = editingId != null
    val inProgressCount get() = projects.count { it.status == ProjectStatus.IN_PROGRESS }
    val completedCount  get() = projects.count { it.status == ProjectStatus.COMPLETED }
}

private fun String.toProjectStatus() =
    runCatching { ProjectStatus.valueOf(this) }.getOrDefault(ProjectStatus.PLANNING)

private fun ProjectDto.toProjectItem() = ProjectItem(
    id             = id,
    title          = title,
    ideaOrigin     = ideaOrigin,
    responsible    = responsible,
    status         = status.toProjectStatus(),
    stage          = stage,
    investment     = investment,
    expectedReturn = expectedReturn,
    actualReturn   = actualReturn,
    productivity   = productivity,
    progress       = progress,
    deadline       = deadline,
    createdAt      = createdAt
)

class ProjectsViewModel(
    private val sessionManager: SessionManager,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    var state by mutableStateOf(ProjectsUiState())
        private set

    init {
        viewModelScope.launch {
            combine(
                sessionManager.userRoleFlow,
                sessionManager.userIdFlow
            ) { roleStr, _ ->
                runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.COLLABORATOR)
            }.collect { role ->
                state = state.copy(userRole = role)
            }
        }
        viewModelScope.launch { loadProjects() }
    }

    fun retry() { viewModelScope.launch { loadProjects() } }

    private suspend fun loadProjects() {
        state = state.copy(isLoading = true, error = null)
        when (val result = projectsRepository.getProjects()) {
            is ApiResult.Success -> state = state.copy(
                isLoading = false,
                projects  = result.data.map { it.toProjectItem() }
            )
            is ApiResult.Error   -> state = state.copy(isLoading = false, error = result.message)
        }
    }

    fun onStatusFilter(s: ProjectStatus?) { state = state.copy(selectedStatus = s) }

    fun showCreateForm() {
        state = state.copy(
            isFormVisible    = true,
            editingId        = null,
            formTitle        = "",
            formIdeaOrigin   = "",
            formResponsible  = "",
            formStatus       = ProjectStatus.PLANNING,
            formStage        = "",
            formInvestment   = "",
            formExpectedReturn = "",
            formActualReturn = "",
            formProductivity = "",
            formProgress     = 0f,
            formDeadline     = "",
            formError        = null
        )
    }

    fun showEditForm(project: ProjectItem) {
        state = state.copy(
            isFormVisible      = true,
            editingId          = project.id,
            formTitle          = project.title,
            formIdeaOrigin     = project.ideaOrigin,
            formResponsible    = project.responsible,
            formStatus         = project.status,
            formStage          = project.stage,
            formInvestment     = project.investment,
            formExpectedReturn = project.expectedReturn,
            formActualReturn   = project.actualReturn,
            formProductivity   = project.productivity,
            formProgress       = project.progress.toFloat(),
            formDeadline       = project.deadline,
            formError          = null
        )
    }

    fun hideForm() { state = state.copy(isFormVisible = false, formError = null) }

    fun onTitleChange(v: String)          { state = state.copy(formTitle = v, formError = null) }
    fun onIdeaOriginChange(v: String)     { state = state.copy(formIdeaOrigin = v) }
    fun onResponsibleChange(v: String)    { state = state.copy(formResponsible = v) }
    fun onStatusChange(v: ProjectStatus)  { state = state.copy(formStatus = v) }
    fun onStageChange(v: String)          { state = state.copy(formStage = v) }
    fun onInvestmentChange(v: String)     { state = state.copy(formInvestment = v) }
    fun onExpectedReturnChange(v: String) { state = state.copy(formExpectedReturn = v) }
    fun onActualReturnChange(v: String)   { state = state.copy(formActualReturn = v) }
    fun onProductivityChange(v: String)   { state = state.copy(formProductivity = v) }
    fun onProgressChange(v: Float)        { state = state.copy(formProgress = v) }
    fun onDeadlineChange(v: String)       { state = state.copy(formDeadline = v) }

    fun saveProject() {
        if (state.formTitle.isBlank()) { state = state.copy(formError = "Título é obrigatório."); return }

        viewModelScope.launch {
            if (state.editingId != null) {
                val editId = state.editingId!!
                when (val result = projectsRepository.updateProject(
                    id             = editId,
                    title          = state.formTitle.trim(),
                    ideaOrigin     = state.formIdeaOrigin.trim(),
                    responsible    = state.formResponsible.trim(),
                    status         = state.formStatus.name,
                    stage          = state.formStage,
                    investment     = state.formInvestment.trim(),
                    expectedReturn = state.formExpectedReturn.trim(),
                    actualReturn   = state.formActualReturn.trim(),
                    productivity   = state.formProductivity.trim(),
                    deadline       = state.formDeadline.trim()
                )) {
                    is ApiResult.Success -> state = state.copy(
                        projects      = state.projects.map { p ->
                            if (p.id == editId) result.data.toProjectItem() else p
                        },
                        isFormVisible = false,
                        editingId     = null
                    )
                    is ApiResult.Error   -> state = state.copy(formError = result.message)
                }
            } else {
                when (val result = projectsRepository.createProject(
                    title          = state.formTitle.trim(),
                    ideaOrigin     = state.formIdeaOrigin.trim(),
                    responsible    = state.formResponsible.trim(),
                    status         = state.formStatus.name,
                    stage          = state.formStage.ifBlank { PROJECT_STAGES.first() },
                    investment     = state.formInvestment.trim(),
                    expectedReturn = state.formExpectedReturn.trim(),
                    deadline       = state.formDeadline.trim()
                )) {
                    is ApiResult.Success -> state = state.copy(
                        projects      = listOf(result.data.toProjectItem()) + state.projects,
                        isFormVisible = false
                    )
                    is ApiResult.Error   -> state = state.copy(formError = result.message)
                }
            }
        }
    }
}

class ProjectsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProjectsViewModel(SessionManager(context), ProjectsRepository(RetrofitClient.api)) as T
}
