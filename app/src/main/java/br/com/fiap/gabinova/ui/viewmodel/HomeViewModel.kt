package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String   = "",
    val userRole: UserRole = UserRole.COLLABORATOR,
    val myIdeasCount:      Int    = 5,
    val points:            Int    = 320,
    val ranking:           Int    = 12,
    val level:             Int    = 3,
    val levelName:         String = "Colaborador",
    val levelProgress:     Float  = 0.64f,
    val pendingReviews:    Int    = 8,
    val activeProjects:    Int    = 4,
    val approvedThisMonth: Int    = 12,
    val approvalGoal:      Int    = 20,
    val totalIdeas:        Int    = 147,
    val completedProjects: Int    = 23,
    val engagementRate:    Int    = 78,
    val quarterlyProgress: Float  = 0.78f
)

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            combine(
                sessionManager.userNameFlow,
                sessionManager.userRoleFlow
            ) { name, roleStr ->
                val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.COLLABORATOR)
                mockState(name, role)
            }.collect { state -> uiState = state }
        }
    }

    private fun mockState(name: String, role: UserRole) = when (role) {
        UserRole.COLLABORATOR -> HomeUiState(
            userName = name, userRole = role,
            myIdeasCount = 5, points = 320, ranking = 12,
            level = 3, levelName = "Colaborador", levelProgress = 0.64f
        )
        UserRole.MANAGER -> HomeUiState(
            userName = name, userRole = role,
            pendingReviews = 8, activeProjects = 4,
            approvedThisMonth = 12, approvalGoal = 20
        )
        else -> HomeUiState(
            userName = name, userRole = role,
            totalIdeas = 147, completedProjects = 23,
            engagementRate = 78, quarterlyProgress = 0.78f
        )
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(SessionManager(context)) as T
}
