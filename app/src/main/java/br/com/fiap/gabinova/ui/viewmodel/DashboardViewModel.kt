package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.dto.DashboardDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.DashboardRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

data class DashboardData(
    val totalIdeas: Int           = 47,
    val approvedIdeas: Int        = 23,
    val inReviewIdeas: Int        = 15,
    val ideasThisMonth: Int       = 8,
    val activeProjects: Int       = 4,
    val completedProjects: Int    = 12,
    val totalProjects: Int        = 16,
    val totalInvestment: String   = "R\$ 345.000",
    val financialReturn: String   = "R\$ 728.000",
    val roi: Double               = 111.3,
    val economyGenerated: String  = "R\$ 92.000",
    val productivityGain: String  = "18%",
    val engagedCollaborators: Int = 89,
    val totalCollaborators: Int   = 120,
    val engagementRate: Double    = 74.2,
    val topGuideline: String      = "Eficiência Operacional"
) {
    val approvalRate: Double
        get() = if (totalIdeas > 0) approvedIdeas.toDouble() / totalIdeas.toDouble() * 100 else 0.0

    val completionRate: Double
        get() = if (totalProjects > 0) completedProjects.toDouble() / totalProjects.toDouble() * 100 else 0.0

    val engagementFraction: Float
        get() = if (totalCollaborators > 0)
            engagedCollaborators.toFloat() / totalCollaborators.toFloat()
        else 0f
}

data class DashboardUiState(
    val userRole:  UserRole      = UserRole.COLLABORATOR,
    val data:      DashboardData = DashboardData(),
    val isLoading: Boolean       = false,
    val error:     String?       = null
) {
    val isLeadership get() = userRole == UserRole.ADMIN || userRole == UserRole.ANALYST
    val isManager    get() = userRole == UserRole.MANAGER
}

private fun DashboardDto.toDashboardData() = DashboardData(
    totalIdeas           = totalIdeas,
    approvedIdeas        = approvedIdeas,
    inReviewIdeas        = inReviewIdeas,
    ideasThisMonth       = ideasThisMonth,
    activeProjects       = activeProjects,
    completedProjects    = completedProjects,
    totalProjects        = totalProjects,
    totalInvestment      = totalInvestment,
    financialReturn      = financialReturn,
    roi                  = roi,
    economyGenerated     = economyGenerated,
    productivityGain     = productivityGain,
    engagedCollaborators = engagedCollaborators,
    totalCollaborators   = totalCollaborators,
    engagementRate       = engagementRate,
    topGuideline         = topGuideline
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    var state by mutableStateOf(DashboardUiState())
        private set

    init {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { roleStr ->
                val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.COLLABORATOR)
                state = state.copy(userRole = role)
            }
        }
        viewModelScope.launch { loadDashboard() }
    }

    fun retry() { viewModelScope.launch { loadDashboard() } }

    private suspend fun loadDashboard() {
        state = state.copy(isLoading = true, error = null)
        when (val result = dashboardRepository.getDashboard()) {
            is ApiResult.Success -> state = state.copy(isLoading = false, data = result.data.toDashboardData())
            is ApiResult.Error   -> state = state.copy(isLoading = false, error = result.message)
        }
    }
}

class DashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(SessionManager(context), DashboardRepository(RetrofitClient.api)) as T
}
