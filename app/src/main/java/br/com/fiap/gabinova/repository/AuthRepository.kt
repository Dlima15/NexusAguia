package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.data.remote.ApiResult
import br.com.fiap.gabinova.data.remote.api.ApiService
import br.com.fiap.gabinova.data.remote.dto.LoginRequest
import br.com.fiap.gabinova.data.remote.safeApiCall
import br.com.fiap.gabinova.data.remote.service.TokenProvider
import br.com.fiap.gabinova.model.User
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.session.SessionManager

data class LoginResult(
    val success: Boolean,
    val user: User? = null,
    val token: String? = null,
    val errorMessage: String? = null
)

class AuthRepository(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {

    suspend fun login(email: String, password: String): LoginResult {
        val result = safeApiCall {
            apiService.login(LoginRequest(email = email.trim(), password = password))
        }

        return when (result) {
            is ApiResult.Success -> {
                val data = result.data

                TokenProvider.token = data.token

                val role = runCatching { UserRole.valueOf(data.userRole) }
                    .getOrDefault(UserRole.COLLABORATOR)

                sessionManager.saveSession(
                    token    = data.token,
                    userId   = data.userId,
                    userName = data.userName,
                    userRole = role.name,
                    email    = data.email
                )

                LoginResult(
                    success = true,
                    token   = data.token,
                    user    = User(
                        id         = data.userId,
                        name       = data.userName,
                        email      = data.email,
                        role       = role,
                        department = "GAB Inova",
                        createdAt  = ""
                    )
                )
            }

            is ApiResult.Error -> LoginResult(
                success      = false,
                errorMessage = result.message
            )
        }
    }

    suspend fun logout() {
        TokenProvider.token = ""
        sessionManager.clearSession()
    }
}
