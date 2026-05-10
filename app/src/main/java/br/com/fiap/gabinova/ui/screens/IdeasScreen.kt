package br.com.fiap.gabinova.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.IdeaDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.IdeaStatus
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.IdeasRepository
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.components.EmptyState
import br.com.fiap.gabinova.ui.components.GabStatus
import br.com.fiap.gabinova.ui.components.StatusChip
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ── Constants & helpers ────────────────────────────────────────────────────────

private val IDEA_SECTORS = listOf(
    "Operações", "Tecnologia", "RH", "Financeiro",
    "Comercial", "Logística", "Atendimento", "Jurídico"
)

private val IDEA_CATEGORIES = listOf(
    "Melhoria de Processo", "Redução de Custo", "Experiência do Cliente",
    "Inovação Tecnológica", "Engajamento", "Segurança"
)

private val FILTER_STATUSES: List<IdeaStatus?> = listOf(
    null,
    IdeaStatus.PENDING,
    IdeaStatus.IN_REVIEW,
    IdeaStatus.APPROVED,
    IdeaStatus.REJECTED,
    IdeaStatus.IMPLEMENTED
)

private fun urgencyLabel(urgency: Int) = when (urgency) {
    1    -> "Baixa"
    2    -> "Média"
    3    -> "Normal"
    4    -> "Alta"
    5    -> "Crítica"
    else -> ""
}

private fun urgencyColor(urgency: Int) = when (urgency) {
    1    -> GabGreen
    2    -> Color(0xFF8BC34A)
    3    -> GabYellow
    4    -> Color(0xFFFF9800)
    5    -> GabError
    else -> GabOnSurfaceVariant
}

private fun urgencyContentColor(urgency: Int) = when (urgency) {
    3    -> GabTextDark
    else -> Color.White
}

private fun statusToGabStatus(status: IdeaStatus) = when (status) {
    IdeaStatus.PENDING     -> GabStatus.PENDENTE
    IdeaStatus.IN_REVIEW   -> GabStatus.EM_ANALISE
    IdeaStatus.APPROVED    -> GabStatus.APROVADO
    IdeaStatus.REJECTED    -> GabStatus.REJEITADO
    IdeaStatus.IMPLEMENTED -> GabStatus.CONCLUIDO
}

private fun statusFilterLabel(status: IdeaStatus?) = when (status) {
    null                   -> "Todas"
    IdeaStatus.PENDING     -> "Pendente"
    IdeaStatus.IN_REVIEW   -> "Em Análise"
    IdeaStatus.APPROVED    -> "Aprovado"
    IdeaStatus.REJECTED    -> "Rejeitado"
    IdeaStatus.IMPLEMENTED -> "Implementado"
}

// ── Local model ────────────────────────────────────────────────────────────────

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

// ── UI State ───────────────────────────────────────────────────────────────────

data class IdeasUiState(
    val ideas: List<IdeaItem> = emptyList(),
    val userRole: UserRole = UserRole.COLLABORATOR,
    val currentUserId: String = "",
    val currentUserName: String = "",
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
            val base = if (userRole == UserRole.COLLABORATOR)
                ideas.filter { it.authorId == currentUserId }
            else
                ideas
            return if (selectedStatus != null) base.filter { it.status == selectedStatus } else base
        }

    val isCollaborator get() = userRole == UserRole.COLLABORATOR
    val isManager      get() = userRole == UserRole.MANAGER
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

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
            is ApiResult.Success -> state = state.copy(isLoading = false, ideas = result.data.map { it.toIdeaItem() })
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

    private fun mockIdeas(): List<IdeaItem> = listOf(
        IdeaItem(
            id             = "1",
            title          = "Sistema de monitoramento de frota em tempo real",
            description    = "Integrar GPS com telemetria para rastrear consumo de combustível e eficiência por rota.",
            sector         = "Operações",
            category       = "Inovação Tecnológica",
            expectedImpact = "Redução de 15% no custo de combustível",
            urgency        = 4,
            status         = IdeaStatus.APPROVED,
            authorId       = "operador",
            authorName     = "Operador Padrão",
            createdAt      = "02/04/2025",
            score          = 60
        ),
        IdeaItem(
            id             = "2",
            title          = "Programa de carona corporativa",
            description    = "Plataforma interna para funcionários que fazem o mesmo trajeto compartilharem transporte.",
            sector         = "RH",
            category       = "Engajamento",
            expectedImpact = "Economia nos deslocamentos e redução de emissões de CO₂",
            urgency        = 2,
            status         = IdeaStatus.IN_REVIEW,
            authorId       = "colaborador",
            authorName     = "Ana Lima",
            createdAt      = "10/04/2025",
            score          = 30
        ),
        IdeaItem(
            id             = "3",
            title          = "Checklist digital para embarque",
            description    = "Substituir papel por app mobile nas operações de embarque de passageiros.",
            sector         = "Operações",
            category       = "Melhoria de Processo",
            expectedImpact = "Reduzir erros e acelerar o embarque em 30%",
            urgency        = 3,
            status         = IdeaStatus.PENDING,
            authorId       = "operador",
            authorName     = "Operador Padrão",
            createdAt      = "18/04/2025",
            score          = 10
        ),
        IdeaItem(
            id             = "4",
            title          = "Painel de feedback de clientes por rota",
            description    = "Dashboard consolidado com NPS e reclamações organizados por linha.",
            sector         = "Atendimento",
            category       = "Experiência do Cliente",
            expectedImpact = "Melhora na satisfação e retenção de clientes",
            urgency        = 3,
            status         = IdeaStatus.PENDING,
            authorId       = "colaborador2",
            authorName     = "Mariana Costa",
            createdAt      = "22/04/2025",
            score          = 10
        ),
        IdeaItem(
            id             = "5",
            title          = "Treinamento gamificado para motoristas",
            description    = "Cursos com sistema de pontuação e rankings para engajar a equipe de condutores.",
            sector         = "RH",
            category       = "Engajamento",
            expectedImpact = "Redução de acidentes e aumento de produtividade",
            urgency        = 2,
            status         = IdeaStatus.REJECTED,
            authorId       = "operador",
            authorName     = "Operador Padrão",
            createdAt      = "01/05/2025",
            score          = 0
        )
    )
}

// ── Factory ────────────────────────────────────────────────────────────────────

private class IdeasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IdeasViewModel(SessionManager(context), IdeasRepository(RetrofitClient.api)) as T
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeasScreen() {
    val context    = LocalContext.current
    val vm: IdeasViewModel = viewModel(factory = IdeasViewModelFactory(context))
    val state      = vm.state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when {
        state.isLoading -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground),
            contentAlignment = Alignment.Center
        ) { androidx.compose.material3.CircularProgressIndicator(color = GabBlue) }

        state.error != null -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error!!, color = GabError)
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::retry,
                    colors = ButtonDefaults.buttonColors(containerColor = GabBlue)
                ) { Text("Tentar novamente", color = Color.White) }
            }
        }

        else -> {
            if (state.isFormVisible) {
                ModalBottomSheet(
                    onDismissRequest = vm::hideForm,
                    sheetState       = sheetState,
                    containerColor   = GabSurface
                ) {
                    IdeaFormSheet(
                        state                  = state,
                        onTitleChange          = vm::onTitleChange,
                        onDescriptionChange    = vm::onDescriptionChange,
                        onSectorChange         = vm::onSectorChange,
                        onCategoryChange       = vm::onCategoryChange,
                        onExpectedImpactChange = vm::onExpectedImpactChange,
                        onUrgencyChange        = vm::onUrgencyChange,
                        onSave                 = vm::saveIdea,
                        onCancel               = vm::hideForm
                    )
                }
            }

            IdeasContent(
                state          = state,
                onStatusFilter = vm::onStatusFilter,
                onAddClick     = vm::showCreateForm,
                onApprove      = vm::approveIdea,
                onReject       = vm::rejectIdea,
                onPrioritize   = vm::prioritizeIdea
            )
        }
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
internal fun IdeasContent(
    state: IdeasUiState,
    onStatusFilter: (IdeaStatus?) -> Unit,
    onAddClick: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onPrioritize: (String) -> Unit
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
                        state.isCollaborator -> "Minhas Ideias"
                        state.isManager      -> "Ideias Recebidas"
                        else                 -> "Consulta de Ideias"
                    },
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = GabTextDark
                )
                if (state.visibleIdeas.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(GabBlue, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = "${state.visibleIdeas.size}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }

            // Status filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding        = PaddingValues(horizontal = 20.dp)
            ) {
                items(FILTER_STATUSES) { status ->
                    FilterChip(
                        selected = state.selectedStatus == status,
                        onClick  = { onStatusFilter(status) },
                        label    = { Text(statusFilterLabel(status)) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GabBlue,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.visibleIdeas.isEmpty()) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon    = Icons.Filled.Lightbulb,
                        title   = "Nenhuma ideia encontrada",
                        message = if (state.isCollaborator)
                            "Toque em \"Nova Ideia\" para compartilhar sua primeira sugestão!"
                        else
                            "Não há ideias para o filtro selecionado."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    items(state.visibleIdeas, key = { it.id }) { idea ->
                        IdeaCard(
                            idea         = idea,
                            showActions  = state.isManager,
                            onApprove    = { onApprove(idea.id) },
                            onReject     = { onReject(idea.id) },
                            onPrioritize = { onPrioritize(idea.id) }
                        )
                    }
                }
            }
        }

        // FAB — Operador only
        if (state.isCollaborator) {
            ExtendedFloatingActionButton(
                text           = { Text("Nova Ideia") },
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

// ── IdeaCard ───────────────────────────────────────────────────────────────────

@Composable
private fun IdeaCard(
    idea: IdeaItem,
    showActions: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPrioritize: () -> Unit
) {
    val canAct = showActions && (idea.status == IdeaStatus.PENDING || idea.status == IdeaStatus.IN_REVIEW)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        // Urgency color strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(urgencyColor(idea.urgency))
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Status + score
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                StatusChip(status = statusToGabStatus(idea.status))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Star,
                        contentDescription = null,
                        tint               = GabYellow,
                        modifier           = Modifier.size(14.dp)
                    )
                    Text(
                        text       = "${idea.score} pts",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = GabTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = idea.title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = GabTextDark
            )

            if (idea.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = idea.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = GabOnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meta chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaChip(label = idea.sector)
                MetaChip(label = idea.category)
                UrgencyChip(urgency = idea.urgency)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Por ${idea.authorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GabOnSurfaceVariant
                )
                Text(
                    text  = idea.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = GabOnSurfaceVariant
                )
            }

            if (idea.expectedImpact.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text      = "Impacto: ${idea.expectedImpact}",
                    style     = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color     = GabBlue,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
            }

            // Gestor action buttons
            if (canAct) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = GabError),
                        border   = BorderStroke(1.dp, GabError)
                    ) {
                        Text("Recusar", style = MaterialTheme.typography.labelSmall)
                    }

                    if (idea.status == IdeaStatus.PENDING) {
                        OutlinedButton(
                            onClick  = onPrioritize,
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = GabBlue)
                        ) {
                            Text("Priorizar", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Button(
                        onClick  = onApprove,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = GabGreen)
                    ) {
                        Text("Aprovar", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Micro-composables ──────────────────────────────────────────────────────────

@Composable
private fun MetaChip(label: String) {
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
private fun UrgencyChip(urgency: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .background(urgencyColor(urgency), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text       = urgencyLabel(urgency),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = urgencyContentColor(urgency)
        )
    }
}

// ── Form Sheet ─────────────────────────────────────────────────────────────────

@Composable
private fun IdeaFormSheet(
    state: IdeasUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSectorChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onExpectedImpactChange: (String) -> Unit,
    onUrgencyChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text       = "Nova Ideia",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = GabTextDark
        )
        Text(
            text  = "Compartilhe sua sugestão com a equipe",
            style = MaterialTheme.typography.bodySmall,
            color = GabOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value         = state.formTitle,
            onValueChange = onTitleChange,
            label         = { Text("Título *") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GabBlue,
                focusedLabelColor  = GabBlue,
                cursorColor        = GabBlue
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.formDescription,
            onValueChange = onDescriptionChange,
            label         = { Text("Descrição") },
            minLines      = 3,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GabBlue,
                focusedLabelColor  = GabBlue,
                cursorColor        = GabBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sector selector
        Text(
            text       = "Setor *",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = GabTextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IDEA_SECTORS.forEach { sector ->
                FilterChip(
                    selected = state.formSector == sector,
                    onClick  = { onSectorChange(sector) },
                    label    = { Text(sector) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GabBlue,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category selector
        Text(
            text       = "Categoria *",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = GabTextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IDEA_CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = state.formCategory == cat,
                    onClick  = { onCategoryChange(cat) },
                    label    = { Text(cat) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GabBlue,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value         = state.formExpectedImpact,
            onValueChange = onExpectedImpactChange,
            label         = { Text("Impacto Esperado") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GabBlue,
                focusedLabelColor  = GabBlue,
                cursorColor        = GabBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Urgency selector
        Text(
            text       = "Urgência",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = GabTextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { level ->
                val selected = state.formUrgency == level
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(if (selected) urgencyColor(level) else GabSurfaceVariant)
                        .clickable { onUrgencyChange(level) }
                ) {
                    Text(
                        text       = "$level",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = if (selected) urgencyContentColor(level) else GabOnSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text  = "1 = Baixa  •  3 = Normal  •  5 = Crítica",
            style = MaterialTheme.typography.labelSmall,
            color = GabOnSurfaceVariant
        )

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
            ) { Text("Enviar Ideia", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────

private fun mockIdeasList() = listOf(
    IdeaItem(
        id = "1", title = "Sistema de monitoramento de frota",
        description = "Integrar GPS com telemetria para rastrear combustível.",
        sector = "Operações", category = "Inovação Tecnológica",
        expectedImpact = "Redução de 15% no custo de combustível",
        urgency = 4, status = IdeaStatus.APPROVED,
        authorId = "operador", authorName = "Operador Padrão",
        createdAt = "02/04/2025", score = 60
    ),
    IdeaItem(
        id = "2", title = "Checklist digital para embarque",
        description = "Substituir papel por app mobile no embarque.",
        sector = "Operações", category = "Melhoria de Processo",
        expectedImpact = "Reduzir erros em 30%",
        urgency = 3, status = IdeaStatus.PENDING,
        authorId = "operador", authorName = "Operador Padrão",
        createdAt = "18/04/2025", score = 10
    ),
    IdeaItem(
        id = "3", title = "Programa de carona corporativa",
        description = "Plataforma para funcionários compartilharem transporte.",
        sector = "RH", category = "Engajamento",
        expectedImpact = "Redução de emissões de CO₂",
        urgency = 2, status = IdeaStatus.IN_REVIEW,
        authorId = "colaborador", authorName = "Ana Lima",
        createdAt = "10/04/2025", score = 30
    )
)

@Preview(showBackground = true, showSystemUi = true, name = "Ideias — Operador")
@Composable
private fun IdeasOperadorPreview() {
    GabInovaTheme {
        IdeasContent(
            state = IdeasUiState(
                ideas          = mockIdeasList(),
                userRole       = UserRole.COLLABORATOR,
                currentUserId  = "operador",
                currentUserName = "Operador Padrão"
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onApprove      = {},
            onReject       = {},
            onPrioritize   = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Ideias — Gestor")
@Composable
private fun IdeasGestorPreview() {
    GabInovaTheme {
        IdeasContent(
            state = IdeasUiState(
                ideas    = mockIdeasList(),
                userRole = UserRole.MANAGER
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onApprove      = {},
            onReject       = {},
            onPrioritize   = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Ideias — Liderança")
@Composable
private fun IdeasLiderancaPreview() {
    GabInovaTheme {
        IdeasContent(
            state = IdeasUiState(
                ideas    = mockIdeasList(),
                userRole = UserRole.ADMIN
            ),
            onStatusFilter = {},
            onAddClick     = {},
            onApprove      = {},
            onReject       = {},
            onPrioritize   = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Ideias — Formulário")
@Composable
private fun IdeaFormPreview() {
    GabInovaTheme {
        IdeaFormSheet(
            state = IdeasUiState(
                formTitle   = "Minha nova ideia",
                formSector  = "Tecnologia",
                formUrgency = 4
            ),
            onTitleChange          = {},
            onDescriptionChange    = {},
            onSectorChange         = {},
            onCategoryChange       = {},
            onExpectedImpactChange = {},
            onUrgencyChange        = {},
            onSave                 = {},
            onCancel               = {}
        )
    }
}
