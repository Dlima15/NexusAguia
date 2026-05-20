package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.ProjectStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.ProjectsRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

val PROJECT_STAGES = listOf(
    "Planejamento",
    "Validação Estratégica",
    "Levantamento",
    "Desenvolvimento",
    "Testes",
    "Implantação",
    "Encerramento"
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
    val projects: List<ProjectItem> = emptyList(),
    val userRole: UserRole = UserRole.COLLABORATOR,
    val selectedStatus: ProjectStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFormVisible: Boolean = false,
    val editingId: String? = null,
    val formTitle: String = "",
    val formIdeaOrigin: String = "",
    val formResponsible: String = "",
    val formStatus: ProjectStatus = ProjectStatus.PLANNING,
    val formStage: String = "",
    val formInvestment: String = "",
    val formExpectedReturn: String = "",
    val formActualReturn: String = "",
    val formProductivity: String = "",
    val formProgress: Float = 0f,
    val formDeadline: String = "",
    val formError: String? = null
) {
    val visibleProjects: List<ProjectItem>
        get() = if (selectedStatus != null) {
            projects.filter { it.status == selectedStatus }
        } else {
            projects
        }

    val isManager: Boolean
        get() = userRole == UserRole.MANAGER

    val isLeadership: Boolean
        get() = userRole == UserRole.ADMIN || userRole == UserRole.ANALYST

    val canEdit: Boolean
        get() = isManager

    val isEditing: Boolean
        get() = editingId != null

    val inProgressCount: Int
        get() = projects.count { it.status == ProjectStatus.IN_PROGRESS }

    val completedCount: Int
        get() = projects.count { it.status == ProjectStatus.COMPLETED }

    val awaitingApprovalCount: Int
        get() = projects.count { it.status == ProjectStatus.AWAITING_APPROVAL }

    val strategicProjectsCount: Int
        get() = projects.count {
            it.status == ProjectStatus.COMPLETED || it.status == ProjectStatus.IN_PROGRESS
        }

    val totalExpectedReturn: String
        get() {
            val total = projects.sumOf { parseMoneyToDouble(it.expectedReturn) }
            return formatMoney(total)
        }

    val totalActualReturn: String
        get() {
            val total = projects.sumOf { parseMoneyToDouble(it.actualReturn) }
            return formatMoney(total)
        }
}

private fun parseMoneyToDouble(value: String): Double {
    return value
        .replace("R$", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()
        .toDoubleOrNull() ?: 0.0
}

private fun formatMoney(value: Double): String {
    val formatted = "%,.0f".format(value)
        .replace(",", ".")

    return "R$ $formatted"
}

class ProjectsViewModel(
    private val sessionManager: SessionManager,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    var state by mutableStateOf(ProjectsUiState())
        private set

    init {
        observeUserRole()
        loadMockProjects()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { roleStr ->
                val role = runCatching {
                    UserRole.valueOf(roleStr)
                }.getOrDefault(UserRole.COLLABORATOR)

                state = state.copy(userRole = role)
            }
        }
    }

    fun retry() {
        loadMockProjects()
    }

    private fun loadMockProjects() {
        val mockProjects = listOf(
            ProjectItem(
                id = "1",
                title = "Checklist Digital de Frota",
                ideaOrigin = "Checklist digital de ônibus",
                responsible = "Equipe de Operações",
                status = ProjectStatus.AWAITING_APPROVAL,
                stage = "Validação Estratégica",
                investment = "R$ 45.000",
                expectedReturn = "R$ 120.000",
                actualReturn = "R$ 0",
                productivity = "18%",
                progress = 65,
                deadline = "30/06/2026",
                createdAt = "01/05/2026"
            ),
            ProjectItem(
                id = "2",
                title = "Painel Estratégico Operacional",
                ideaOrigin = "Dashboard de produtividade operacional",
                responsible = "Equipe de Tecnologia",
                status = ProjectStatus.PLANNING,
                stage = "Planejamento",
                investment = "R$ 80.000",
                expectedReturn = "R$ 210.000",
                actualReturn = "R$ 0",
                productivity = "22%",
                progress = 25,
                deadline = "15/08/2026",
                createdAt = "05/05/2026"
            ),
            ProjectItem(
                id = "3",
                title = "Programa Nexus de Reconhecimento",
                ideaOrigin = "Gamificação para colaboradores",
                responsible = "RH e Inovação",
                status = ProjectStatus.COMPLETED,
                stage = "Encerramento",
                investment = "R$ 35.000",
                expectedReturn = "R$ 90.000",
                actualReturn = "R$ 110.000",
                productivity = "28%",
                progress = 100,
                deadline = "20/04/2026",
                createdAt = "10/03/2026"
            ),
            ProjectItem(
                id = "4",
                title = "Monitoramento Inteligente de Embarque",
                ideaOrigin = "Redução de filas no embarque",
                responsible = "Equipe de Operações",
                status = ProjectStatus.IN_PROGRESS,
                stage = "Implantação",
                investment = "R$ 150.000",
                expectedReturn = "R$ 380.000",
                actualReturn = "R$ 210.000",
                productivity = "35%",
                progress = 82,
                deadline = "10/07/2026",
                createdAt = "22/04/2026"
            ),
            ProjectItem(
                id = "5",
                title = "Central Inteligente de Feedback",
                ideaOrigin = "Painel de feedback de clientes por rota",
                responsible = "Experiência do Cliente",
                status = ProjectStatus.COMPLETED,
                stage = "Encerramento",
                investment = "R$ 60.000",
                expectedReturn = "R$ 140.000",
                actualReturn = "R$ 175.000",
                productivity = "31%",
                progress = 100,
                deadline = "02/05/2026",
                createdAt = "01/03/2026"
            )
        )

        state = state.copy(
            isLoading = false,
            error = null,
            projects = mockProjects
        )
    }

    fun onStatusFilter(s: ProjectStatus?) {
        state = state.copy(selectedStatus = s)
    }

    fun showCreateForm() {
        if (!state.isManager) return

        state = state.copy(
            isFormVisible = true,
            editingId = null,
            formTitle = "",
            formIdeaOrigin = "",
            formResponsible = "",
            formStatus = ProjectStatus.AWAITING_APPROVAL,
            formStage = "Validação Estratégica",
            formInvestment = "",
            formExpectedReturn = "",
            formActualReturn = "",
            formProductivity = "",
            formProgress = 0f,
            formDeadline = "",
            formError = null
        )
    }

    fun showEditForm(project: ProjectItem) {
        if (!state.isManager) return

        state = state.copy(
            isFormVisible = true,
            editingId = project.id,
            formTitle = project.title,
            formIdeaOrigin = project.ideaOrigin,
            formResponsible = project.responsible,
            formStatus = project.status,
            formStage = project.stage,
            formInvestment = project.investment,
            formExpectedReturn = project.expectedReturn,
            formActualReturn = project.actualReturn,
            formProductivity = project.productivity,
            formProgress = project.progress.toFloat(),
            formDeadline = project.deadline,
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
        state = state.copy(formTitle = v, formError = null)
    }

    fun onIdeaOriginChange(v: String) {
        state = state.copy(formIdeaOrigin = v)
    }

    fun onResponsibleChange(v: String) {
        state = state.copy(formResponsible = v)
    }

    fun onStatusChange(v: ProjectStatus) {
        state = state.copy(
            formStatus = v,
            formProgress = if (v == ProjectStatus.COMPLETED) 100f else state.formProgress,
            formStage = when (v) {
                ProjectStatus.AWAITING_APPROVAL -> "Validação Estratégica"
                ProjectStatus.COMPLETED -> "Encerramento"
                ProjectStatus.IN_PROGRESS -> state.formStage.ifBlank { "Implantação" }
                else -> state.formStage
            }
        )
    }

    fun onStageChange(v: String) {
        state = state.copy(formStage = v)
    }

    fun onInvestmentChange(v: String) {
        state = state.copy(formInvestment = v)
    }

    fun onExpectedReturnChange(v: String) {
        state = state.copy(formExpectedReturn = v)
    }

    fun onActualReturnChange(v: String) {
        state = state.copy(formActualReturn = v)
    }

    fun onProductivityChange(v: String) {
        state = state.copy(formProductivity = v)
    }

    fun onProgressChange(v: Float) {
        state = state.copy(formProgress = v)
    }

    fun onDeadlineChange(v: String) {
        state = state.copy(formDeadline = v)
    }

    fun approveProjectStrategically(id: String) {
        if (!state.isLeadership) return

        state = state.copy(
            projects = state.projects.map { project ->
                if (project.id == id && project.status == ProjectStatus.AWAITING_APPROVAL) {
                    project.copy(
                        status = ProjectStatus.IN_PROGRESS,
                        stage = "Implantação",
                        progress = if (project.progress < 40) 40 else project.progress
                    )
                } else {
                    project
                }
            }
        )
    }

    fun saveProject() {
        if (!state.isManager) return

        if (state.formTitle.isBlank()) {
            state = state.copy(formError = "Título é obrigatório.")
            return
        }

        val finalProgress = when (state.formStatus) {
            ProjectStatus.COMPLETED -> 100
            ProjectStatus.AWAITING_APPROVAL -> state.formProgress.toInt().coerceAtMost(70)
            else -> state.formProgress.toInt()
        }

        val finalStage = when (state.formStatus) {
            ProjectStatus.COMPLETED -> "Encerramento"
            ProjectStatus.AWAITING_APPROVAL -> "Validação Estratégica"
            else -> state.formStage.ifBlank { PROJECT_STAGES.first() }
        }

        if (state.editingId != null) {
            val editId = state.editingId!!

            state = state.copy(
                projects = state.projects.map { project ->
                    if (project.id == editId) {
                        project.copy(
                            title = state.formTitle.trim(),
                            ideaOrigin = state.formIdeaOrigin.trim(),
                            responsible = state.formResponsible.trim(),
                            status = state.formStatus,
                            stage = finalStage,
                            investment = state.formInvestment.trim(),
                            expectedReturn = state.formExpectedReturn.trim(),
                            actualReturn = state.formActualReturn.trim(),
                            productivity = state.formProductivity.trim(),
                            progress = finalProgress,
                            deadline = state.formDeadline.trim()
                        )
                    } else {
                        project
                    }
                },
                isFormVisible = false,
                editingId = null,
                formError = null
            )
        } else {
            val newProject = ProjectItem(
                id = System.currentTimeMillis().toString(),
                title = state.formTitle.trim(),
                ideaOrigin = state.formIdeaOrigin.trim(),
                responsible = state.formResponsible.trim(),
                status = ProjectStatus.AWAITING_APPROVAL,
                stage = "Validação Estratégica",
                investment = state.formInvestment.trim(),
                expectedReturn = state.formExpectedReturn.trim(),
                actualReturn = state.formActualReturn.trim(),
                productivity = state.formProductivity.trim(),
                progress = finalProgress,
                deadline = state.formDeadline.trim(),
                createdAt = "13/05/2026"
            )

            state = state.copy(
                projects = listOf(newProject) + state.projects,
                isFormVisible = false,
                formError = null
            )
        }
    }
}

class ProjectsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProjectsViewModel(
            SessionManager(context),
            ProjectsRepository(RetrofitClient.api)
        ) as T
}