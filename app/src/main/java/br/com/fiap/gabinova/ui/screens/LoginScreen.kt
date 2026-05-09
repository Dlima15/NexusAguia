package br.com.fiap.gabinova.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.repository.AuthRepository
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.components.PrimaryButton
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun login() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(error = "Preencha e-mail e senha.")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = authRepository.login(uiState.email.trim(), uiState.password)
            uiState = if (result.success) {
                uiState.copy(isLoading = false, loginSuccess = true)
            } else {
                uiState.copy(isLoading = false, error = result.errorMessage)
            }
        }
    }
}

// ── Factory ──────────────────────────────────────────────────────────────────

private class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(AuthRepository(SessionManager(context))) as T
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val vm: LoginViewModel = viewModel(factory = LoginViewModelFactory(context))

    LaunchedEffect(vm.uiState.loginSuccess) {
        if (vm.uiState.loginSuccess) onLoginSuccess()
    }

    LoginScreenContent(
        state            = vm.uiState,
        onEmailChange    = vm::onEmailChange,
        onPasswordChange = vm::onPasswordChange,
        onLoginClick     = vm::login
    )
}

// ── Content (separado para @Preview) ─────────────────────────────────────────

@Composable
internal fun LoginScreenContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header corporativo ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GabBlue)
                    .padding(top = 72.dp, bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(GabYellow)
                    ) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GabTextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Nexus Águia",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GabOnPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grupo Águia Branca · Inovação",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnPrimary.copy(alpha = 0.75f)
                    )
                }
            }

            // ── Card de login ─────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-32).dp),
                colors    = CardDefaults.cardColors(containerColor = GabSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape     = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "Bem-vindo!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GabTextDark
                    )
                    Text(
                        text = "Acesse sua conta para continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // E-mail
                    OutlinedTextField(
                        value         = state.email,
                        onValueChange = onEmailChange,
                        label         = { Text("E-mail") },
                        leadingIcon   = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = GabBlue)
                        },
                        singleLine      = true,
                        isError         = state.error != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GabBlue,
                            focusedLabelColor  = GabBlue,
                            cursorColor        = GabBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Senha
                    OutlinedTextField(
                        value         = state.password,
                        onValueChange = onPasswordChange,
                        label         = { Text("Senha") },
                        leadingIcon   = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = GabBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible)
                                        "Ocultar senha" else "Mostrar senha",
                                    tint = GabOnSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine      = true,
                        isError         = state.error != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus(); onLoginClick() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GabBlue,
                            focusedLabelColor  = GabBlue,
                            cursorColor        = GabBlue
                        )
                    )

                    // Mensagem de erro
                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector   = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint          = GabError,
                                modifier      = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text  = state.error,
                                style = MaterialTheme.typography.labelMedium,
                                color = GabError
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text      = if (state.isLoading) "Entrando..." else "Entrar",
                        onClick   = onLoginClick,
                        isLoading = state.isLoading
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Login — vazio")
@Composable
private fun LoginScreenPreview() {
    GabInovaTheme {
        LoginScreenContent(
            state            = LoginUiState(),
            onEmailChange    = {},
            onPasswordChange = {},
            onLoginClick     = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — erro")
@Composable
private fun LoginScreenErrorPreview() {
    GabInovaTheme {
        LoginScreenContent(
            state = LoginUiState(
                email    = "operador@gab.com",
                password = "senhaerrada",
                error    = "Senha incorreta."
            ),
            onEmailChange    = {},
            onPasswordChange = {},
            onLoginClick     = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login — carregando")
@Composable
private fun LoginScreenLoadingPreview() {
    GabInovaTheme {
        LoginScreenContent(
            state = LoginUiState(
                email     = "gestor@gab.com",
                password  = "123456",
                isLoading = true
            ),
            onEmailChange    = {},
            onPasswordChange = {},
            onLoginClick     = {}
        )
    }
}
