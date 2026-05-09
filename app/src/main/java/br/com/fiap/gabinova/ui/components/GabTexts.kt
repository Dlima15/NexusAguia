package br.com.fiap.gabinova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GabTextDark
        )
        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = GabLightBlue
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = GabOnSurfaceVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = GabTextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = GabOnSurfaceVariant
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    GabInovaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title = "Minhas Ideias",   actionLabel = "Ver todas")
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(title = "Projetos Ativos")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateIdeiaPreview() {
    GabInovaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EmptyState(
                icon = Icons.Filled.Lightbulb,
                title = "Nenhuma ideia ainda",
                message = "Que tal compartilhar sua primeira ideia com o time?",
                action = {
                    PrimaryButton(
                        text = "Nova Ideia",
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateProjetosPreview() {
    GabInovaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EmptyState(
                icon = Icons.Filled.Folder,
                title = "Sem projetos ativos",
                message = "Você não possui nenhum projeto em andamento no momento."
            )
        }
    }
}
