package br.com.fiap.gabinova.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.gabinova.data.remote.service.RetrofitClient
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.repository.AuthRepository
import br.com.fiap.gabinova.session.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun login() {

        val email = uiState.email.trim().lowercase()
        val password = uiState.password.trim()

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Preencha e-mail e senha.")
            return
        }

        viewModelScope.launch {

            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            delay(500)

            when {

                email == "operador@gab.com" && password == "123456" -> {

                    sessionManager.saveSession(
                        token = "token_operador",
                        userId = "1",
                        userName = "Operador",
                        userRole = UserRole.COLLABORATOR.name,
                        email = email
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }

                email == "gestor@gab.com" && password == "123456" -> {

                    sessionManager.saveSession(
                        token = "token_gestor",
                        userId = "2",
                        userName = "Gestor",
                        userRole = UserRole.MANAGER.name,
                        email = email
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }

                email == "lideranca@gab.com" && password == "123456" -> {

                    sessionManager.saveSession(
                        token = "token_lideranca",
                        userId = "3",
                        userName = "Liderança",
                        userRole = UserRole.ADMIN.name,
                        email = email
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }

                else -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "E-mail ou senha inválidos."
                    )
                }
            }
        }
    }
}

class LoginViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val sessionManager = SessionManager(context)

        return LoginViewModel(
            AuthRepository(
                sessionManager,
                RetrofitClient.api
            ),
            sessionManager
        ) as T
    }
}