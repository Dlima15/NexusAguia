package br.com.fiap.gabinova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabPrimaryContainer
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant

enum class GabStatus {
    ATIVO, EM_ANALISE, APROVADO, REJEITADO, CONCLUIDO, PENDENTE
}

@Composable
fun StatusChip(
    status: GabStatus,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        GabStatus.ATIVO      -> Triple("Ativo",       Color(0xFFE8F5E9), GabGreen)
        GabStatus.EM_ANALISE -> Triple("Em Análise",  Color(0xFFFFF8E1), Color(0xFFF57F17))
        GabStatus.APROVADO   -> Triple("Aprovado",    Color(0xFFE8F5E9), GabGreen)
        GabStatus.REJEITADO  -> Triple("Rejeitado",   Color(0xFFFFEBEE), GabError)
        GabStatus.CONCLUIDO  -> Triple("Concluído",   GabPrimaryContainer, GabBlue)
        GabStatus.PENDENTE   -> Triple("Pendente",    GabSurfaceVariant,   GabOnSurfaceVariant)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = GabStatus.ATIVO)
                StatusChip(status = GabStatus.EM_ANALISE)
                StatusChip(status = GabStatus.APROVADO)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = GabStatus.REJEITADO)
                StatusChip(status = GabStatus.CONCLUIDO)
                StatusChip(status = GabStatus.PENDENTE)
            }
        }
    }
}
