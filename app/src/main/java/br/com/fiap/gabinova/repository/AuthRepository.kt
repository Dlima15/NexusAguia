package br.com.fiap.gabinova.repository

import br.com.fiap.gabinova.model.User
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.session.SessionManager

data class LoginResult(
    val success: Boolean,
    val user: User? = null,
    val token: String? = null,
    val errorMessage: String? = null
)

class AuthRepository(private val sessionManager: SessionManager) {

    private data class Credential(
        val password: String,
        val role: UserRole,
        val name: String
    )

    private val credentials = mapOf(
        "operador@gab.com" to Credential("123456", UserRole.COLLABORATOR, "Operador Padrão"),
        "gestor@gab.com"   to Credential("123456", UserRole.MANAGER,      "Gestor Regional"),
        "lider@gab.com"    to Credential("123456", UserRole.ADMIN,        "Líder Estratégico")
    )

    suspend fun login(email: String, password: String): LoginResult {
        val credential = credentials[email.trim().lowercase()]
            ?: return LoginResult(success = false, errorMessage = "E-mail não cadastrado.")

        if (password != credential.password) {
            return LoginResult(success = false, errorMessage = "Senha incorreta.")
        }

        val token  = "mock-token-${System.currentTimeMillis()}"
        val userId = email.substringBefore("@")

        sessionManager.saveSession(
            token    = token,
            userId   = userId,
            userName = credential.name,
            userRole = credential.role.name,
            email    = email
        )

        return LoginResult(
            success = true,
            token   = token,
            user    = User(
                id         = userId,
                name       = credential.name,
                email      = email,
                role       = credential.role,
                department = "GAB Inova",
                createdAt  = "2024-01-01T00:00:00Z"
            )
        )
    }

    suspend fun logout() = sessionManager.clearSession()
}
