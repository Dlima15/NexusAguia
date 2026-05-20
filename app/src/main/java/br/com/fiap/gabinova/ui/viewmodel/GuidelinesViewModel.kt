package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.StrategicGuideline
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.GuidelineRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

data class GuidelinesUiState(
    val guidelines: List<StrategicGuideline> = emptyList(),
    val userRole: UserRole = UserRole.COLLABORATOR,
    val selectedCategory: String = "Todas",
    val isFormVisible: Boolean = false,
    val editingGuideline: StrategicGuideline? = null,
    val formTitle: String = "",
    val formDescription: String = "",
    val formCategory: String = "Inovação",
    val formPriority: Int = 3,
    val formError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredGuidelines: List<StrategicGuideline>
        get() = if (selectedCategory == "Todas") {
            guidelines
        } else {
            guidelines.filter { it.category == selectedCategory }
        }

    val isAdmin: Boolean
        get() = userRole == UserRole.ADMIN
}

class GuidelinesViewModel(
    private val sessionManager: SessionManager,
    private val guidelineRepository: GuidelineRepository
) : ViewModel() {

    var uiState by mutableStateOf(GuidelinesUiState())
        private set

    init {
        observeUserRole()
        loadMockGuidelines()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { roleStr ->
                val role = runCatching {
                    UserRole.valueOf(roleStr)
                }.getOrDefault(UserRole.COLLABORATOR)

                uiState = uiState.copy(userRole = role)
            }
        }
    }

    fun retry() {
        loadMockGuidelines()
    }

    private fun loadMockGuidelines() {
        val mockGuidelines = listOf(
            StrategicGuideline(
                id = "1",
                title = "Eficiência Operacional",
                description = "Priorizar ideias que reduzam custos, otimizem processos e aumentem a produtividade nas operações do Grupo Águia Branca.",
                category = "Operações",
                priority = 1,
                active = true,
                createdAt = "01/05/2026"
            ),
            StrategicGuideline(
                id = "2",
                title = "Experiência do Cliente",
                description = "Estimular soluções que melhorem a jornada dos passageiros, o atendimento e a qualidade dos serviços prestados.",
                category = "Inovação",
                priority = 2,
                active = true,
                createdAt = "03/05/2026"
            ),
            StrategicGuideline(
                id = "3",
                title = "Transformação Digital",
                description = "Incentivar projetos com uso de tecnologia, dados e automação para modernizar áreas internas e processos de negócio.",
                category = "Tecnologia",
                priority = 2,
                active = true,
                createdAt = "05/05/2026"
            )
        )

        uiState = uiState.copy(
            isLoading = false,
            error = null,
            guidelines = mockGuidelines
        )
    }

    fun onCategoryFilter(category: String) {
        uiState = uiState.copy(selectedCategory = category)
    }

    fun showCreateForm() {
        if (!uiState.isAdmin) return

        uiState = uiState.copy(
            isFormVisible = true,
            editingGuideline = null,
            formTitle = "",
            formDescription = "",
            formCategory = "Inovação",
            formPriority = 3,
            formError = null
        )
    }

    fun showEditForm(guideline: StrategicGuideline) {
        if (!uiState.isAdmin) return

        uiState = uiState.copy(
            isFormVisible = true,
            editingGuideline = guideline,
            formTitle = guideline.title,
            formDescription = guideline.description,
            formCategory = guideline.category,
            formPriority = guideline.priority,
            formError = null
        )
    }

    fun hideForm() {
        uiState = uiState.copy(
            isFormVisible = false,
            editingGuideline = null,
            formError = null
        )
    }

    fun onFormTitleChange(v: String) {
        uiState = uiState.copy(formTitle = v, formError = null)
    }

    fun onFormDescriptionChange(v: String) {
        uiState = uiState.copy(formDescription = v)
    }

    fun onFormCategoryChange(v: String) {
        uiState = uiState.copy(formCategory = v)
    }

    fun onFormPriorityChange(v: Int) {
        uiState = uiState.copy(formPriority = v)
    }

    fun saveGuideline() {
        if (!uiState.isAdmin) return

        if (uiState.formTitle.isBlank()) {
            uiState = uiState.copy(formError = "O título é obrigatório.")
            return
        }

        val editing = uiState.editingGuideline

        if (editing != null) {
            val updated = editing.copy(
                title = uiState.formTitle.trim(),
                description = uiState.formDescription.trim(),
                category = uiState.formCategory,
                priority = uiState.formPriority
            )

            uiState = uiState.copy(
                guidelines = uiState.guidelines.map {
                    if (it.id == updated.id) updated else it
                },
                isFormVisible = false,
                editingGuideline = null,
                formError = null
            )
        } else {
            val newGuideline = StrategicGuideline(
                id = System.currentTimeMillis().toString(),
                title = uiState.formTitle.trim(),
                description = uiState.formDescription.trim(),
                category = uiState.formCategory,
                priority = uiState.formPriority,
                active = true,
                createdAt = "13/05/2026"
            )

            uiState = uiState.copy(
                guidelines = listOf(newGuideline) + uiState.guidelines,
                isFormVisible = false,
                editingGuideline = null,
                formError = null
            )
        }
    }

    fun deleteGuideline(id: String) {
        if (!uiState.isAdmin) return

        uiState = uiState.copy(
            guidelines = uiState.guidelines.filter { it.id != id }
        )
    }
}

class GuidelinesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GuidelinesViewModel(
            SessionManager(context),
            GuidelineRepository(RetrofitClient.api)
        ) as T
}