package br.com.fiap.gabinova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor         = GabBlue,
            contentColor           = GabOnPrimary,
            disabledContainerColor = GabSurfaceVariant,
            disabledContentColor   = GabOnSurfaceVariant
        )
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GabOnPrimary,
                strokeWidth = 2.dp
            )
            else -> {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(text = "Enviar Ideia", onClick = {})
            PrimaryButton(text = "Nova Ideia", onClick = {}, icon = Icons.Filled.Add)
            PrimaryButton(text = "Carregando...", onClick = {}, isLoading = true)
            PrimaryButton(text = "Desabilitado", onClick = {}, enabled = false)
        }
    }
}
