package br.com.fiap.gabinova.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.model.ProjectStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.components.EmptyState
import br.com.fiap.gabinova.ui.components.GabStatus
import br.com.fiap.gabinova.ui.components.StatCard
import br.com.fiap.gabinova.ui.components.StatusChip
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

// ── Constants & helpers ────────────────────────────────────────────────────────

private val PROJECT_STAGES = listOf(
    "Planejamento", "Levantamento", "Desenvolvimento", "Testes", "Implantação", "Encerramento"
)

private val FILTER_PROJECT_STATUSES: List<ProjectStatus?> = listOf(
    null,
    ProjectStatus.PLANNING,
    ProjectStatus.IN_PROGRESS,
    ProjectStatus.ON_HOLD,
    ProjectStatus.COMPLETED,
    ProjectStatus.CANCELLED
)

private fun statusToGabStatus(s: ProjectStatus) = when (s) {
    ProjectStatus.PLANNING    -> GabStatus.PENDENTE
    ProjectStatus.IN_PROGRESS -> GabStatus.ATIVO
    ProjectStatus.ON_HOLD     -> GabStatus.EM_ANALISE
    ProjectStatus.COMPLETED   -> GabStatus.CONCLUIDO
    ProjectStatus.CANCELLED   -> GabStatus.REJEITADO
}

private fun statusLabel(s: ProjectStatus?) = when (s) {
    null                      -> "Todos"
    ProjectStatus.PLANNING    -> "Planejamento"
    ProjectStatus.IN_PROGRESS -> "Em Andamento"
    ProjectStatus.ON_HOLD     -> "Pausado"
    ProjectStatus.COMPLETED   -> "Concluído"
    ProjectStatus.CANCELLED   -> "Cancelado"
}

private fun statusStripColor(s: ProjectStatus) = when (s) {
    ProjectStatus.PLANNING    -> GabLightBlue
    ProjectStatus.IN_PROGRESS -> GabGreen
    ProjectStatus.ON_HOLD     -> Color(0xFFFF9800)
    ProjectStatus.COMPLETED   -> GabBlue
    ProjectStatus.CANCELLED   -> GabError
}

private fun progressBarColor(progress: Int) = when {
    progress >= 70 -> GabGreen
    progress >= 40 -> GabYellow
    else           -> GabError
}

private fun productivityColor(value: String): Color {
    val pct = value.replace("%", "").trim().toIntOrNull() ?: return GabSurfaceVariant
    return when {
        pct >= 80 -> GabGreen
        pct >= 50 -> GabYellow
        else      -> GabError
    }
}

private fun productivityTextColor(value: String): Color {
    val pct = value.replace("%", "").trim().toIntOrNull() ?: return GabOnSurfaceVariant
    return if (pct in 40..79) GabTextDark else Color.White
}

// ── Local model ────────────────────────────────────────────────────────────────

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

// ── UI State ───────────────────────────────────────────────────────────────────

data class ProjectsUiState(
    val projects: List<ProjectItem> = emptyList(),
    val userRole: UserRole = UserRole.COLLABORATOR,
    val selectedStatus: ProjectStatus? = null,
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
        get() = if (selectedStatus != null)
            projects.filter { it.status == selectedStatus }
        else
            projects

    val isManager    get() = userRole == UserRole.MANAGER
    val canEdit      get() = isManager
    val isEditing    get() = editingId != null

    val inProgressCount get() = projects.count { it.status == ProjectStatus.IN_PROGRESS }
    val completedCount  get() = projects.count { it.status == ProjectStatus.COMPLETED }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ProjectsViewModel(private val sessionManager: SessionManager) : ViewModel() {

    var state by mutableStateOf(ProjectsUiState(projects = mockProjects()))
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
    }

    fun onStatusFilter(s: ProjectStatus?) { state = state.copy(selectedStatus = s) }

    fun showCreateForm() {
        state = state.copy(
            isFormVisible      = true,
            editingId          = null,
            formTitle          = "",
            formIdeaOrigin     = "",
            formResponsible    = "",
            formStatus         = ProjectStatus.PLANNING,
            formStage          = "",
            formInvestment     = "",
            formExpectedReturn = "",
            formActualReturn   = "",
            formProductivity   = "",
            formProgress       = 0f,
            formDeadline       = "",
            formError          = null
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

        if (state.editingId != null) {
            state = state.copy(
                projects = state.projects.map { p ->
                    if (p.id != state.editingId) p else p.copy(
                        title          = state.formTitle.trim(),
                        ideaOrigin     = state.formIdeaOrigin.trim(),
                        responsible    = state.formResponsible.trim(),
                        status         = state.formStatus,
                        stage          = state.formStage,
                        investment     = state.formInvestment.trim(),
                        expectedReturn = state.formExpectedReturn.trim(),
                        actualReturn   = state.formActualReturn.trim(),
                        productivity   = state.formProductivity.trim(),
                        progress       = state.formProgress.toInt(),
                        deadline       = state.formDeadline.trim()
                    )
                },
                isFormVisible = false,
                editingId     = null
            )
        } else {
            val newProject = ProjectItem(
                id             = UUID.randomUUID().toString(),
                title          = state.formTitle.trim(),
                ideaOrigin     = state.formIdeaOrigin.trim(),
                responsible    = state.formResponsible.trim(),
                status         = state.formStatus,
                stage          = state.formStage.ifBlank { PROJECT_STAGES.first() },
                investment     = state.formInvestment.trim(),
                expectedReturn = state.formExpectedReturn.trim(),
                actualReturn   = state.formActualReturn.trim(),
                productivity   = state.formProductivity.trim(),
                progress       = state.formProgress.toInt(),
                deadline       = state.formDeadline.trim(),
                createdAt      = "hoje"
            )
            state = state.copy(
                projects      = listOf(newProject) + state.projects,
                isFormVisible = false
            )
        }
    }

    private fun mockProjects(): List<ProjectItem> = listOf(
        ProjectItem(
            id             = "p1",
            title          = "Sistema de Monitoramento de Frota",
            ideaOrigin     = "Sistema de monitoramento de frota em tempo real",
            responsible    = "Gestor Regional",
            status         = ProjectStatus.IN_PROGRESS,
            stage          = "Desenvolvimento",
            investment     = "R$ 120.000",
            expectedReturn = "R$ 300.000",
            actualReturn   = "",
            productivity   = "72%",
            progress       = 65,
            deadline       = "30/09/2025",
            createdAt      = "01/03/2025"
        ),
        ProjectItem(
            id             = "p2",
            title          = "Painel de Feedback de Clientes",
            ideaOrigin     = "Painel de feedback de clientes por rota",
            responsible    = "Gestor Regional",
            status         = ProjectStatus.PLANNING,
            stage          = "Levantamento",
            investment     = "R$ 45.000",
            expectedReturn = "R$ 90.000",
            actualReturn   = "",
            productivity   = "",
            progress       = 10,
            deadline       = "31/12/2025",
            createdAt      = "15/04/2025"
        ),
        ProjectItem(
            id             = "p3",
            title          = "Programa de Carona Corporativa",
            ideaOrigin     = "Programa de carona corporativa",
            responsible    = "Gestor Regional",
            status         = ProjectStatus.ON_HOLD,
            stage          = "Planejamento",
            investment     = "R$ 30.000",
            expectedReturn = "R$ 60.000",
            actualReturn   = "",
            productivity   = "35%",
            progress       = 25,
            deadline       = "28/02/2026",
            createdAt      = "20/04/2025"
        ),
        ProjectItem(
            id             = "p4",
            title          = "Plataforma de Treinamento Digital",
            ideaOrigin     = "Treinamento gamificado para motoristas",
            responsible    = "Gestor Regional",
            status         = ProjectStatus.COMPLETED,
            stage          = "Encerramento",
            investment     = "R$ 80.000",
            expectedReturn = "R$ 150.000",
            actualReturn   = "R$ 178.000",
            productivity   = "92%",
            progress       = 100,
            deadline       = "30/04/2025",
            createdAt      = "01/01/2025"
        )
    )
}

// ── Factory ────────────────────────────────────────────────────────────────────

private class ProjectsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProjectsViewModel(SessionManager(context)) as T
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen() {
    val context    = LocalContext.current
    val vm: ProjectsViewModel = viewModel(factory = ProjectsViewModelFactory(context))
    val state      = vm.state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.isFormVisible) {
        ModalBottomSheet(
            onDismissRequest = vm::hideForm,
            sheetState       = sheetState,
            containerColor   = GabSurface
        ) {
            ProjectFormSheet(
                state                  = state,
                onTitleChange          = vm::onTitleChange,
                onIdeaOriginChange     = vm::onIdeaOriginChange,
                onResponsibleChange    = vm::onResponsibleChange,
                onStatusChange         = vm::onStatusChange,
                onStageChange          = vm::onStageChange,
                onInvestmentChange     = vm::onInvestmentChange,
                onExpectedReturnChange = vm::onExpectedReturnChange,
                onActualReturnChange   = vm::onActualReturnChange,
                onProductivityChange   = vm::onProductivityChange,
                onProgressChange       = vm::onProgressChange,
                onDeadlineChange       = vm::onDeadlineChange,
                onSave                 = vm::saveProject,
                onCancel               = vm::hideForm
            )
        }
    }

    ProjectsContent(
        state          = state,
        onStatusFilter = vm::onStatusFilter,
        onAddClick     = vm::showCreateForm,
        onEditClick    = vm::showEditForm
    )
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
internal fun ProjectsContent(
    state: ProjectsUiState,
    onStatusFilter: (ProjectStatus?) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (ProjectItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        state.isManager -> "Gestão de Projetos"
                        else            -> "Projetos"
                    },
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = GabTextDark
                )
                if (state.projects.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(GabBlue, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = "${state.visibleProjects.size}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }

            // Stats summary
            if (state.projects.isNotEmpty()) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "${state.projects.size}",
                        label    = "Total",
                        icon     = Icons.Filled.Work,
                        iconTint = GabBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "${state.inProgressCount}",
                        label    = "Em Andamento",
                        icon     = Icons.Filled.TrendingUp,
                        iconTint = GabGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "${state.completedCount}",
                        label    = "Concluídos",
                        icon     = Icons.Filled.CheckCircle,
                        iconTint = GabLightBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Status filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding        = PaddingValues(horizontal = 20.dp)
            ) {
                items(FILTER_PROJECT_STATUSES) { status ->
                    FilterChip(
                        selected = state.selectedStatus == status,
                        onClick  = { onStatusFilter(status) },
                        label    = { Text(statusLabel(status)) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GabBlue,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List or empty state
            if (state.visibleProjects.isEmpty()) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon    = Icons.Filled.Folder,
                        title   = "Nenhum projeto encontrado",
                        message = if (state.isManager)
                            "Crie um projeto a partir de uma ideia aprovada."
                        else
                            "Ainda não há projetos para exibir."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    items(state.visibleProjects, key = { it.id }) { project ->
                        ProjectCard(
                            project     = project,
                            canEdit     = state.canEdit,
                            onEditClick = { onEditClick(project) }
                        )
                    }
                }
            }
        }

        // FAB — Gestor only
        if (state.isManager) {
            ExtendedFloatingActionButton(
                text           = { Text("Novo Projeto") },
                icon           = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick        = onAddClick,
                containerColor = GabBlue,
                contentColor   = Color.White,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }
}

// ── ProjectCard ────────────────────────────────────────────────────────────────

@Composable
private fun ProjectCard(
    project: ProjectItem,
    canEdit: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        // Status color strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(statusStripColor(project.status))
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Status row + edit button
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    StatusChip(status = statusToGabStatus(project.status))
                    if (project.stage.isNotBlank()) {
                        StageBadge(label = project.stage)
                    }
                }
                if (canEdit) {
                    IconButton(
                        onClick  = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint               = GabLightBlue,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text       = project.title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = GabTextDark
            )

            // Idea of origin
            if (project.ideaOrigin.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text      = "Ideia: ${project.ideaOrigin}",
                    style     = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color     = GabOnSurfaceVariant,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Responsible + deadline
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (project.responsible.isNotBlank()) {
                    Text(
                        text  = "Resp.: ${project.responsible}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnSurfaceVariant
                    )
                }
                if (project.deadline.isNotBlank()) {
                    Text(
                        text  = "Prazo: ${project.deadline}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress   = { project.progress / 100f },
                modifier   = Modifier.fillMaxWidth(),
                color      = progressBarColor(project.progress),
                trackColor = GabSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Progresso",
                    style = MaterialTheme.typography.labelSmall,
                    color = GabOnSurfaceVariant
                )
                Text(
                    text       = "${project.progress}%",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = progressBarColor(project.progress)
                )
            }

            // Financial info
            val hasFinancials = project.investment.isNotBlank() ||
                    project.expectedReturn.isNotBlank() ||
                    project.actualReturn.isNotBlank()
            if (hasFinancials) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (project.investment.isNotBlank()) {
                        FinancePill(label = "Invest.", value = project.investment)
                    }
                    if (project.expectedReturn.isNotBlank()) {
                        FinancePill(label = "Ret. Esp.", value = project.expectedReturn)
                    }
                    if (project.actualReturn.isNotBlank()) {
                        FinancePill(label = "Ret. Real", value = project.actualReturn)
                    }
                }
            }

            // Productivity badge
            if (project.productivity.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text  = "Produtividade:",
                        style = MaterialTheme.typography.labelSmall,
                        color = GabOnSurfaceVariant
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(
                                productivityColor(project.productivity),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = project.productivity,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = productivityTextColor(project.productivity)
                        )
                    }
                }
            }
        }
    }
}

// ── Micro-composables ──────────────────────────────────────────────────────────

@Composable
private fun StageBadge(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .background(GabSurfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = GabOnSurfaceVariant
        )
    }
}

@Composable
private fun FinancePill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = GabOnSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = GabBlue
        )
    }
}

// ── Form Sheet ─────────────────────────────────────────────────────────────────

@Composable
private fun ProjectFormSheet(
    state: ProjectsUiState,
    onTitleChange: (String) -> Unit,
    onIdeaOriginChange: (String) -> Unit,
    onResponsibleChange: (String) -> Unit,
    onStatusChange: (ProjectStatus) -> Unit,
    onStageChange: (String) -> Unit,
    onInvestmentChange: (String) -> Unit,
    onExpectedReturnChange: (String) -> Unit,
    onActualReturnChange: (String) -> Unit,
    onProductivityChange: (String) -> Unit,
    onProgressChange: (Float) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GabBlue,
        focusedLabelColor  = GabBlue,
        cursorColor        = GabBlue
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text       = if (state.isEditing) "Editar Projeto" else "Novo Projeto",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = GabTextDark
        )
        Text(
            text  = if (state.isEditing)
                "Atualize os dados do projeto"
            else
                "Crie um projeto a partir de uma ideia aprovada",
            style = MaterialTheme.typography.bodySmall,
            color = GabOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        OutlinedTextField(
            value         = state.formTitle,
            onValueChange = onTitleChange,
            label         = { Text("Título *") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Idea of origin
        OutlinedTextField(
            value         = state.formIdeaOrigin,
            onValueChange = onIdeaOriginChange,
            label         = { Text("Ideia de Origem") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Responsible
        OutlinedTextField(
            value         = state.formResponsible,
            onValueChange = onResponsibleChange,
            label         = { Text("Responsável") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status
        FormSectionLabel("Status")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                ProjectStatus.PLANNING    to "Planejamento",
                ProjectStatus.IN_PROGRESS to "Em Andamento",
                ProjectStatus.ON_HOLD     to "Pausado",
                ProjectStatus.COMPLETED   to "Concluído",
                ProjectStatus.CANCELLED   to "Cancelado"
            ).forEach { (status, label) ->
                FilterChip(
                    selected = state.formStatus == status,
                    onClick  = { onStatusChange(status) },
                    label    = { Text(label) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = statusStripColor(status),
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stage
        FormSectionLabel("Etapa")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PROJECT_STAGES.forEach { stage ->
                FilterChip(
                    selected = state.formStage == stage,
                    onClick  = { onStageChange(stage) },
                    label    = { Text(stage) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GabBlue,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress slider
        FormSectionLabel("Progresso: ${state.formProgress.toInt()}%")
        Slider(
            value       = state.formProgress,
            onValueChange = onProgressChange,
            valueRange  = 0f..100f,
            modifier    = Modifier.fillMaxWidth(),
            colors      = SliderDefaults.colors(
                thumbColor       = progressBarColor(state.formProgress.toInt()),
                activeTrackColor = progressBarColor(state.formProgress.toInt())
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Prazo
        OutlinedTextField(
            value         = state.formDeadline,
            onValueChange = onDeadlineChange,
            label         = { Text("Prazo (ex: 30/12/2025)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Financial fields
        FormSectionLabel("Financeiro")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value            = state.formInvestment,
            onValueChange    = onInvestmentChange,
            label            = { Text("Investimento (ex: R$ 50.000)") },
            singleLine       = true,
            keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier         = Modifier.fillMaxWidth(),
            colors           = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value            = state.formExpectedReturn,
            onValueChange    = onExpectedReturnChange,
            label            = { Text("Retorno Esperado") },
            singleLine       = true,
            keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier         = Modifier.fillMaxWidth(),
            colors           = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value            = state.formActualReturn,
            onValueChange    = onActualReturnChange,
            label            = { Text("Retorno Real") },
            singleLine       = true,
            keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier         = Modifier.fillMaxWidth(),
            colors           = fieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.formProductivity,
            onValueChange = onProductivityChange,
            label         = { Text("Produtividade (ex: 85%)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors
        )

        // Error
        if (state.formError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = state.formError,
                style = MaterialTheme.typography.labelMedium,
                color = GabError
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
            Button(
                onClick  = onSave,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(containerColor = GabBlue)
            ) {
                Text(
                    text  = if (state.isEditing) "Salvar" else "Criar Projeto",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = GabTextDark
    )
}

// ── Previews ───────────────────────────────────────────────────────────────────

private fun mockProjectList() = listOf(
    ProjectItem(
        id = "p1", title = "Sistema de Monitoramento de Frota",
        ideaOrigin = "Sistema de monitoramento de frota em tempo real",
        responsible = "Gestor Regional", status = ProjectStatus.IN_PROGRESS,
        stage = "Desenvolvimento", investment = "R$ 120.000",
        expectedReturn = "R$ 300.000", actualReturn = "",
        productivity = "72%", progress = 65,
        deadline = "30/09/2025", createdAt = "01/03/2025"
    ),
    ProjectItem(
        id = "p2", title = "Plataforma de Treinamento Digital",
        ideaOrigin = "Treinamento gamificado para motoristas",
        responsible = "Gestor Regional", status = ProjectStatus.COMPLETED,
        stage = "Encerramento", investment = "R$ 80.000",
        expectedReturn = "R$ 150.000", actualReturn = "R$ 178.000",
        productivity = "92%", progress = 100,
        deadline = "30/04/2025", createdAt = "01/01/2025"
    ),
    ProjectItem(
        id = "p3", title = "Painel de Feedback de Clientes",
        ideaOrigin = "Painel de feedback de clientes por rota",
        responsible = "Gestor Regional", status = ProjectStatus.PLANNING,
        stage = "Levantamento", investment = "R$ 45.000",
        expectedReturn = "R$ 90.000", actualReturn = "",
        productivity = "", progress = 10,
        deadline = "31/12/2025", createdAt = "15/04/2025"
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "Projetos — Gestor")
@Composable
private fun ProjectsGestorPreview() {
    GabInovaTheme {
        ProjectsContent(
            state = ProjectsUiState(
                projects = mockProjectList(),
                userRole = UserRole.MANAGER
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onEditClick    = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Projetos — Liderança")
@Composable
private fun ProjectsLiderancaPreview() {
    GabInovaTheme {
        ProjectsContent(
            state = ProjectsUiState(
                projects = mockProjectList(),
                userRole = UserRole.ADMIN
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onEditClick    = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Projetos — Operador")
@Composable
private fun ProjectsOperadorPreview() {
    GabInovaTheme {
        ProjectsContent(
            state = ProjectsUiState(
                projects = mockProjectList(),
                userRole = UserRole.COLLABORATOR
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onEditClick    = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Projetos — Formulário")
@Composable
private fun ProjectFormPreview() {
    GabInovaTheme {
        ProjectFormSheet(
            state = ProjectsUiState(
                formTitle       = "Sistema de Monitoramento de Frota",
                formIdeaOrigin  = "Sistema de monitoramento de frota em tempo real",
                formResponsible = "Gestor Regional",
                formStatus      = ProjectStatus.IN_PROGRESS,
                formStage       = "Desenvolvimento",
                formProgress    = 65f,
                formInvestment  = "R$ 120.000",
                formDeadline    = "30/09/2025"
            ),
            onTitleChange          = {},
            onIdeaOriginChange     = {},
            onResponsibleChange    = {},
            onStatusChange         = {},
            onStageChange          = {},
            onInvestmentChange     = {},
            onExpectedReturnChange = {},
            onActualReturnChange   = {},
            onProductivityChange   = {},
            onProgressChange       = {},
            onDeadlineChange       = {},
            onSave                 = {},
            onCancel               = {}
        )
    }
}
