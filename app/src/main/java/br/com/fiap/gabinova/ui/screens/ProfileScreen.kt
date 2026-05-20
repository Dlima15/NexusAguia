package br.com.fiap.gabinova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.session.SessionManager
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import androidx.compose.ui.tooling.preview.Preview
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ProfileScreen() {

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val userName by sessionManager.userNameFlow.collectAsState(initial = "")
    val email by sessionManager.emailFlow.collectAsState(initial = "")
    val roleString by sessionManager.userRoleFlow.collectAsState(initial = "")

    val userRole = runCatching {
        UserRole.valueOf(roleString)
    }.getOrDefault(UserRole.COLLABORATOR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(GabBackground)
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier
                .clip(CircleShape),

            color = GabYellow
        ) {

            Text(
                text = userName.take(1).uppercase(),
                modifier = Modifier.padding(32.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GabTextDark
            )
        }

        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium
        )

        RoleCard(userRole)
    }
}

@Composable
private fun RoleCard(userRole: UserRole) {

    val title: String
    val description: String

    when (userRole) {

        UserRole.COLLABORATOR -> {

            title = "Perfil Operador"

            description =
                "Responsável por registrar ideias, acompanhar sugestões e participar da gamificação corporativa."
        }

        UserRole.MANAGER -> {

            title = "Perfil Gestor"

            description =
                "Responsável por avaliar ideias, acompanhar projetos e priorizar iniciativas estratégicas."
        }

        else -> {

            title = "Perfil Liderança"

            description =
                "Responsável por acompanhar indicadores estratégicos, ROI, produtividade e resultados organizacionais."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = GabSurface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GabBlue
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (userRole) {

                UserRole.COLLABORATOR -> {

                    ProfileInfo("Pontos", "320")
                    ProfileInfo("Ideias enviadas", "5")
                    ProfileInfo("Ranking", "#12")
                }

                UserRole.MANAGER -> {

                    ProfileInfo("Projetos ativos", "4")
                    ProfileInfo("Ideias avaliadas", "18")
                    ProfileInfo("Taxa de aprovação", "74%")
                }

                else -> {

                    ProfileInfo("ROI acumulado", "111%")
                    ProfileInfo("Economia gerada", "R$ 92.000")
                    ProfileInfo("Projetos estratégicos", "12")
                }
            }
        }
    }
}

@Composable
private fun ProfileInfo(
    label: String,
    value: String
) {

    Column {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GabBlue
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
@Preview(showBackground = true, showSystemUi = true, name = "Perfil — Operador")
@Composable
private fun ProfileOperadorPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(CircleShape),
                color = GabYellow
            ) {
                Text(
                    text = "O",
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GabTextDark
                )
            }

            Text(
                text = "Operador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "operador@gab.com",
                style = MaterialTheme.typography.bodyMedium
            )

            RoleCard(UserRole.COLLABORATOR)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Perfil — Gestor")
@Composable
private fun ProfileGestorPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(CircleShape),
                color = GabYellow
            ) {
                Text(
                    text = "G",
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GabTextDark
                )
            }

            Text(
                text = "Gestor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "gestor@gab.com",
                style = MaterialTheme.typography.bodyMedium
            )

            RoleCard(UserRole.MANAGER)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Perfil — Liderança")
@Composable
private fun ProfileLiderancaPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(CircleShape),
                color = GabYellow
            ) {
                Text(
                    text = "L",
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GabTextDark
                )
            }

            Text(
                text = "Liderança",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "lideranca@gab.com",
                style = MaterialTheme.typography.bodyMedium
            )

            RoleCard(UserRole.ADMIN)
        }
    }
}