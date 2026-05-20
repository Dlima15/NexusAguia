package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.dto.DashboardDto
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.DashboardRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.launch

data class DashboardData(
    val totalIdeas: Int = 58,
    val approvedIdeas: Int = 31,
    val inReviewIdeas: Int = 17,
    val ideasThisMonth: Int = 12,

    val activeProjects: Int = 5,
    val completedProjects: Int = 14,
    val totalProjects: Int = 19,

    val totalInvestment: String = "R$ 370.000",
    val financialReturn: String = "R$ 815.000",
    val roi: Double = 120.3,
    val economyGenerated: String = "R$ 126.000",
    val productivityGain: String = "28%",

    val engagedCollaborators: Int = 96,
    val totalCollaborators: Int = 120,
    val engagementRate: Double = 80.0,

    val topGuideline: String = "Eficiência Operacional"
) {
    val approvalRate: Double
        get() = if (totalIdeas > 0) {
            approvedIdeas.toDouble() / totalIdeas.toDouble() * 100
        } else {
            0.0
        }

    val completionRate: Double
        get() = if (totalProjects > 0) {
            completedProjects.toDouble() / totalProjects.toDouble() * 100
        } else {
            0.0
        }

    val engagementFraction: Float
        get() = if (totalCollaborators > 0) {
            engagedCollaborators.toFloat() / totalCollaborators.toFloat()
        } else {
            0f
        }

    val rejectedIdeas: Int
        get() = (totalIdeas - approvedIdeas - inReviewIdeas).coerceAtLeast(0)

    val innovationSummary: String
        get() = "Inovação conectada à estratégia, execução e resultados mensuráveis."
}

data class DashboardUiState(
    val userRole: UserRole = UserRole.COLLABORATOR,
    val data: DashboardData = DashboardData(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isLeadership: Boolean
        get() = userRole == UserRole.ADMIN || userRole == UserRole.ANALYST

    val isManager: Boolean
        get() = userRole == UserRole.MANAGER
}

private fun DashboardDto.toDashboardData() = DashboardData(
    totalIdeas = totalIdeas,
    approvedIdeas = approvedIdeas,
    inReviewIdeas = inReviewIdeas,
    ideasThisMonth = ideasThisMonth,
    activeProjects = activeProjects,
    completedProjects = completedProjects,
    totalProjects = totalProjects,
    totalInvestment = totalInvestment,
    financialReturn = financialReturn,
    roi = roi,
    economyGenerated = economyGenerated,
    productivityGain = productivityGain,
    engagedCollaborators = engagedCollaborators,
    totalCollaborators = totalCollaborators,
    engagementRate = engagementRate,
    topGuideline = topGuideline
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    var state by mutableStateOf(DashboardUiState())
        private set

    init {
        observeUserRole()
        loadDashboard()
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
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                // Sprint 1:
                // Usamos dados locais simulados para demonstrar o fluxo completo.
                // Na Sprint 2, este ponto pode ser substituído por:
                // val dto = dashboardRepository.getDashboard()
                // val dashboardData = dto.toDashboardData()

                val dashboardData = DashboardData(
                    totalIdeas = 58,
                    approvedIdeas = 31,
                    inReviewIdeas = 17,
                    ideasThisMonth = 12,

                    activeProjects = 5,
                    completedProjects = 14,
                    totalProjects = 19,

                    totalInvestment = "R$ 370.000",
                    financialReturn = "R$ 815.000",
                    roi = 120.3,
                    economyGenerated = "R$ 126.000",
                    productivityGain = "28%",

                    engagedCollaborators = 96,
                    totalCollaborators = 120,
                    engagementRate = 80.0,

                    topGuideline = "Eficiência Operacional"
                )

                state = state.copy(
                    data = dashboardData,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "Não foi possível carregar o dashboard."
                )
            }
        }
    }
}

class DashboardViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(
            SessionManager(context),
            DashboardRepository(RetrofitClient.api)
        ) as T
}