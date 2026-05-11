package br.com.fiap.gabinova.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.model.StrategicGuideline
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.ui.components.EmptyState
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import br.com.fiap.gabinova.ui.viewmodel.GuidelinesUiState
import br.com.fiap.gabinova.ui.viewmodel.GuidelinesViewModel
import br.com.fiap.gabinova.ui.viewmodel.GuidelinesViewModelFactory

// ── Constants ─────────────────────────────────────────────────────────────────

private val ALL_CATEGORIES    = listOf("Inovação", "Tecnologia", "Pessoas", "Operações", "Sustentabilidade", "Finanças")
private val FILTER_CATEGORIES = listOf("Todas") + ALL_CATEGORIES

private fun priorityLabel(p: Int) = when (p) {
    1    -> "Crítica"
    2    -> "Alta"
    3    -> "Média"
    4    -> "Baixa"
    else -> "Informativa"
}

private fun priorityColor(p: Int): Color = when (p) {
    1    -> GabError
    2    -> Color(0xFFF57C00)
    3    -> GabYellow
    4    -> GabGreen
    else -> GabLightBlue
}

private fun priorityContentColor(p: Int): Color = if (p == 3) GabTextDark else Color.White

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategicGuidelinesScreen() {
    val context    = LocalContext.current
    val vm: GuidelinesViewModel = viewModel(factory = GuidelinesViewModelFactory(context))
    val state      = vm.uiState
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var deleteCandidate by remember { mutableStateOf<StrategicGuideline?>(null) }

    when {
        state.isLoading -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = GabBlue) }

        state.error != null -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error!!, color = GabError)
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::retry, colors = ButtonDefaults.buttonColors(containerColor = GabBlue)) {
                    Text("Tentar novamente", color = GabOnPrimary)
                }
            }
        }

        else -> GuidelinesContent(
            state            = state,
            onCategoryFilter = vm::onCategoryFilter,
            onEditClick      = vm::showEditForm,
            onDeleteClick    = { deleteCandidate = it },
            onFabClick       = vm::showCreateForm
        )
    }

    // ── Form — ModalBottomSheet ────────────────────────────────────────────────
    if (state.isFormVisible) {
        ModalBottomSheet(
            onDismissRequest = vm::hideForm,
            sheetState       = sheetState,
            containerColor   = GabSurface
        ) {
            GuidelineFormSheet(
                state               = state,
                onTitleChange       = vm::onFormTitleChange,
                onDescriptionChange = vm::onFormDescriptionChange,
                onCategoryChange    = vm::onFormCategoryChange,
                onPriorityChange    = vm::onFormPriorityChange,
                onSave              = vm::saveGuideline,
                onCancel            = vm::hideForm
            )
        }
    }

    // ── Delete Confirmation ────────────────────────────────────────────────────
    deleteCandidate?.let { guideline ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title   = { Text("Excluir orientação?", fontWeight = FontWeight.Bold) },
            text    = { Text("\"${guideline.title}\" será removida permanentemente.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteGuideline(guideline.id); deleteCandidate = null }) {
                    Text("Excluir", color = GabError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Cancelar") }
            }
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
internal fun GuidelinesContent(
    state:            GuidelinesUiState,
    onCategoryFilter: (String) -> Unit            = {},
    onEditClick:      (StrategicGuideline) -> Unit = {},
    onDeleteClick:    (StrategicGuideline) -> Unit = {},
    onFabClick:       () -> Unit                  = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Filtro de categorias ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTER_CATEGORIES.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick  = { onCategoryFilter(category) },
                        label    = { Text(category, style = MaterialTheme.typography.labelMedium) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GabBlue,
                            selectedLabelColor     = GabOnPrimary
                        )
                    )
                }
            }

            HorizontalDivider(color = GabSurfaceVariant)

            // ── Lista de orientações ─────────────────────────────────────
            if (state.filteredGuidelines.isEmpty()) {
                EmptyState(
                    icon    = Icons.AutoMirrored.Filled.TrendingUp,
                    title   = "Nenhuma orientação encontrada",
                    message = if (state.isAdmin)
                        "Clique em + para criar a primeira orientação estratégica."
                    else
                        "Não há orientações disponíveis para esta categoria."
                )
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    items(state.filteredGuidelines, key = { it.id }) { guideline ->
                        GuidelineCard(
                            guideline     = guideline,
                            isAdmin       = state.isAdmin,
                            onEditClick   = { onEditClick(guideline) },
                            onDeleteClick = { onDeleteClick(guideline) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // ── FAB — exclusivo para Liderança ───────────────────────────────
        if (state.isAdmin) {
            ExtendedFloatingActionButton(
                text           = { Text("Nova Orientação", fontWeight = FontWeight.SemiBold) },
                icon           = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick        = onFabClick,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = GabBlue,
                contentColor   = GabOnPrimary
            )
        }
    }
}

// ── Guideline Card ────────────────────────────────────────────────────────────

@Composable
private fun GuidelineCard(
    guideline:    StrategicGuideline,
    isAdmin:      Boolean,
    onEditClick:  () -> Unit,
    onDeleteClick:() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        // Faixa colorida de prioridade no topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(priorityColor(guideline.priority))
        )
        Column(modifier = Modifier.padding(16.dp)) {

            // Badges + Ações
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = guideline.priority)
                Spacer(modifier = Modifier.width(8.dp))
                CategoryBadge(category = guideline.category)
                Spacer(modifier = Modifier.weight(1f))
                if (isAdmin) {
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
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick  = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Delete,
                            contentDescription = "Excluir",
                            tint               = GabError,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = guideline.title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = GabTextDark
            )

            if (guideline.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = guideline.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = GabOnSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

// ── Priority & Category Badges ────────────────────────────────────────────────

@Composable
private fun PriorityBadge(priority: Int) {
    Box(
        modifier = Modifier
            .background(priorityColor(priority), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text       = priorityLabel(priority),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = priorityContentColor(priority)
        )
    }
}

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .background(GabSurfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text  = category,
            style = MaterialTheme.typography.labelSmall,
            color = GabOnSurfaceVariant
        )
    }
}

// ── Form BottomSheet ──────────────────────────────────────────────────────────

@Composable
private fun GuidelineFormSheet(
    state:               GuidelinesUiState,
    onTitleChange:       (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange:    (String) -> Unit,
    onPriorityChange:    (Int) -> Unit,
    onSave:              () -> Unit,
    onCancel:            () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text       = if (state.editingGuideline != null) "Editar Orientação" else "Nova Orientação",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = GabTextDark
        )

        // Título
        OutlinedTextField(
            value         = state.formTitle,
            onValueChange = onTitleChange,
            label         = { Text("Título *") },
            singleLine    = true,
            isError       = state.formError != null,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GabBlue,
                focusedLabelColor  = GabBlue,
                cursorColor        = GabBlue
            )
        )
        if (state.formError != null) {
            Text(
                text  = state.formError,
                style = MaterialTheme.typography.labelSmall,
                color = GabError
            )
        }

        // Descrição
        OutlinedTextField(
            value         = state.formDescription,
            onValueChange = onDescriptionChange,
            label         = { Text("Descrição") },
            minLines      = 3,
            maxLines      = 5,
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GabBlue,
                focusedLabelColor  = GabBlue,
                cursorColor        = GabBlue
            )
        )

        // Categoria
        Text(
            text       = "Categoria",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color      = GabTextDark
        )
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ALL_CATEGORIES.forEach { category ->
                FilterChip(
                    selected = state.formCategory == category,
                    onClick  = { onCategoryChange(category) },
                    label    = { Text(category, style = MaterialTheme.typography.labelSmall) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GabBlue,
                        selectedLabelColor     = GabOnPrimary
                    )
                )
            }
        }

        // Prioridade
        Text(
            text       = "Prioridade",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color      = GabTextDark
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { p ->
                val selected = state.formPriority == p
                Surface(
                    onClick  = { onPriorityChange(p) },
                    color    = if (selected) priorityColor(p) else GabSurfaceVariant,
                    shape    = MaterialTheme.shapes.small,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text       = "$p",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (selected) priorityContentColor(p) else GabOnSurfaceVariant
                        )
                    }
                }
            }
        }
        Text(
            text  = "1 = Crítica  ·  2 = Alta  ·  3 = Média  ·  4 = Baixa  ·  5 = Informativa",
            style = MaterialTheme.typography.labelSmall,
            color = GabOnSurfaceVariant
        )

        HorizontalDivider(color = GabSurfaceVariant)

        // Botões
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick  = onSave,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(containerColor = GabBlue)
            ) {
                Text("Salvar", color = GabOnPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewGuidelines = listOf(
    StrategicGuideline("1", "Transformação Digital",  "Acelerar a digitalização dos processos internos.", "Tecnologia", 1, true, "2025-01-01T00:00:00Z"),
    StrategicGuideline("2", "Cultura de Inovação",   "Fomentar o intraempreendedorismo nos colaboradores.", "Inovação", 2, true, "2025-01-01T00:00:00Z"),
    StrategicGuideline("3", "Desenvolvimento Humano","Investir em capacitação e crescimento profissional.", "Pessoas", 3, true, "2025-01-01T00:00:00Z"),
)

@Preview(showBackground = true, showSystemUi = true, name = "Guidelines — Operador (somente leitura)")
@Composable
private fun GuidelinesOperadorPreview() {
    GabInovaTheme {
        GuidelinesContent(
            state = GuidelinesUiState(guidelines = previewGuidelines, userRole = UserRole.COLLABORATOR)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Guidelines — Liderança (CRUD)")
@Composable
private fun GuidelinesAdminPreview() {
    GabInovaTheme {
        GuidelinesContent(
            state = GuidelinesUiState(guidelines = previewGuidelines, userRole = UserRole.ADMIN)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Guidelines — Lista vazia")
@Composable
private fun GuidelinesEmptyPreview() {
    GabInovaTheme {
        GuidelinesContent(
            state = GuidelinesUiState(guidelines = emptyList(), userRole = UserRole.ADMIN)
        )
    }
}

@Preview(showBackground = true, name = "Form — Nova orientação")
@Composable
private fun GuidelineFormPreview() {
    GabInovaTheme {
        Surface(color = GabSurface) {
            GuidelineFormSheet(
                state               = GuidelinesUiState(formCategory = "Inovação", formPriority = 2),
                onTitleChange       = {},
                onDescriptionChange = {},
                onCategoryChange    = {},
                onPriorityChange    = {},
                onSave              = {},
                onCancel            = {}
            )
        }
    }
}
