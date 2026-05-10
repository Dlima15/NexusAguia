package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.GuidelineDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.StrategicGuideline
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.GuidelineRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

data class GuidelinesUiState(
    val guidelines:       List<StrategicGuideline> = emptyList(),
    val userRole:         UserRole = UserRole.COLLABORATOR,
    val selectedCategory: String   = "Todas",
    val isFormVisible:    Boolean  = false,
    val editingGuideline: StrategicGuideline? = null,
    val formTitle:        String   = "",
    val formDescription:  String   = "",
    val formCategory:     String   = "Inovação",
    val formPriority:     Int      = 3,
    val formError:        String?  = null,
    val isLoading:        Boolean  = false,
    val error:            String?  = null
) {
    val filteredGuidelines get() =
        if (selectedCategory == "Todas") guidelines
        else guidelines.filter { it.category == selectedCategory }

    val isAdmin get() = userRole == UserRole.ADMIN
}

private fun GuidelineDto.toGuideline() = StrategicGuideline(
    id          = id,
    title       = title,
    description = description,
    category    = category,
    priority    = priority.toIntOrNull() ?: 3,
    active      = status.equals("ACTIVE", ignoreCase = true),
    createdAt   = createdAt
)

class GuidelinesViewModel(
    private val sessionManager: SessionManager,
    private val guidelineRepository: GuidelineRepository
) : ViewModel() {

    var uiState by mutableStateOf(GuidelinesUiState())
        private set

    init {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { roleStr ->
                val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.COLLABORATOR)
                uiState = uiState.copy(userRole = role)
            }
        }
        viewModelScope.launch { loadGuidelines() }
    }

    fun retry() { viewModelScope.launch { loadGuidelines() } }

    private suspend fun loadGuidelines() {
        uiState = uiState.copy(isLoading = true, error = null)
        when (val result = guidelineRepository.getGuidelines()) {
            is ApiResult.Success -> uiState = uiState.copy(
                isLoading  = false,
                guidelines = result.data.map { it.toGuideline() }
            )
            is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, error = result.message)
        }
    }

    fun onCategoryFilter(category: String) { uiState = uiState.copy(selectedCategory = category) }

    fun showCreateForm() {
        uiState = uiState.copy(
            isFormVisible = true, editingGuideline = null,
            formTitle = "", formDescription = "", formCategory = "Inovação",
            formPriority = 3, formError = null
        )
    }

    fun showEditForm(guideline: StrategicGuideline) {
        uiState = uiState.copy(
            isFormVisible    = true,
            editingGuideline = guideline,
            formTitle        = guideline.title,
            formDescription  = guideline.description,
            formCategory     = guideline.category,
            formPriority     = guideline.priority,
            formError        = null
        )
    }

    fun hideForm() { uiState = uiState.copy(isFormVisible = false, editingGuideline = null) }

    fun onFormTitleChange(v: String)       { uiState = uiState.copy(formTitle = v, formError = null) }
    fun onFormDescriptionChange(v: String) { uiState = uiState.copy(formDescription = v) }
    fun onFormCategoryChange(v: String)    { uiState = uiState.copy(formCategory = v) }
    fun onFormPriorityChange(v: Int)       { uiState = uiState.copy(formPriority = v) }

    fun saveGuideline() {
        if (uiState.formTitle.isBlank()) {
            uiState = uiState.copy(formError = "O título é obrigatório.")
            return
        }
        val s = uiState
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, formError = null)
            val result = if (s.editingGuideline != null) {
                guidelineRepository.updateGuideline(
                    s.editingGuideline.id, s.formTitle.trim(),
                    s.formDescription.trim(), s.formCategory, s.formPriority.toString()
                )
            } else {
                guidelineRepository.createGuideline(
                    s.formTitle.trim(), s.formDescription.trim(),
                    s.formCategory, s.formPriority.toString()
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    val updated = result.data.toGuideline()
                    val list    = if (s.editingGuideline != null)
                        s.guidelines.map { if (it.id == updated.id) updated else it }
                    else
                        listOf(updated) + s.guidelines
                    uiState = uiState.copy(
                        isLoading        = false,
                        guidelines       = list,
                        isFormVisible    = false,
                        editingGuideline = null
                    )
                }
                is ApiResult.Error -> uiState = uiState.copy(isLoading = false, formError = result.message)
            }
        }
    }

    fun deleteGuideline(id: String) {
        viewModelScope.launch {
            guidelineRepository.deleteGuideline(id)
            uiState = uiState.copy(guidelines = uiState.guidelines.filter { it.id != id })
        }
    }
}

class GuidelinesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GuidelinesViewModel(SessionManager(context), GuidelineRepository(RetrofitClient.api)) as T
}
