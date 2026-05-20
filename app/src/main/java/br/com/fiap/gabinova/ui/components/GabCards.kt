package br.com.fiap.gabinova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabPrimaryContainer
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import androidx.compose.foundation.shape.RoundedCornerShape
import br.com.fiap.gabinova.ui.theme.GabGreen

@Composable
fun GabCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors    = CardDefaults.cardColors(containerColor = GabSurface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val shape     = MaterialTheme.shapes.medium
    val inner: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colors, elevation = elevation, shape = shape, content = inner)
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colors, elevation = elevation, shape = shape, content = inner)
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = GabBlue
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GabBlue
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = GabOnSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressCard(
    title: String,
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    description: String = ""
) {
    val progress = if (total > 0) {
        (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val percent = (progress * 100).toInt()
    val remaining = (total - current).coerceAtLeast(0)

    GabCard(modifier = modifier) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GabTextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description.ifBlank {
                        if (remaining > 0) {
                            "Faltam $remaining para atingir a meta"
                        } else {
                            "Meta atingida"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GabOnSurfaceVariant
                )
            }

            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GabBlue
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = if (progress >= 1f) GabGreen else GabBlue,
            trackColor = GabSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Atual: $current",
                style = MaterialTheme.typography.labelMedium,
                color = GabOnSurfaceVariant
            )

            Text(
                text = "Meta: $total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GabBlue
            )
        }
    }
}

@Composable
fun BadgeCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    earned: Boolean = false
) {

    val containerColor =
        if (earned) Color(0xFFFFF8E1)
        else GabSurface

    val borderColor =
        if (earned) GabYellow
        else Color.LightGray.copy(alpha = 0.3f)

    val iconBg =
        if (earned) GabYellow
        else GabSurfaceVariant

    val iconTint =
        if (earned) GabTextDark
        else GabOnSurfaceVariant

    val titleColor =
        if (earned) GabTextDark
        else GabOnSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),

        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = if (earned) 6.dp else 1.dp
        ),

        shape = RoundedCornerShape(20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                contentAlignment = Alignment.Center,

                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(iconBg)
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GabOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(50),

                    color =
                        if (earned)
                            GabGreen.copy(alpha = 0.15f)
                        else
                            Color.Gray.copy(alpha = 0.15f)
                ) {

                    Text(
                        text =
                            if (earned)
                                "✓ Desbloqueado"
                            else
                                "🔒 Em progresso",

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),

                        style = MaterialTheme.typography.labelMedium,

                        color =
                            if (earned)
                                GabGreen
                            else
                                Color.Gray,

                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RankingCard(
    position: Int,
    name: String,
    score: Int,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false
) {
    val containerColor = when {
        isCurrentUser -> GabPrimaryContainer
        position == 1 -> Color(0xFFFFF9C4)
        position == 2 -> Color(0xFFF5F5F5)
        position == 3 -> Color(0xFFFBE9E7)
        else          -> GabSurface
    }
    val positionColor = when (position) {
        1    -> Color(0xFFFFB300)
        2    -> Color(0xFF9E9E9E)
        3    -> Color(0xFFBF360C)
        else -> GabOnSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = positionColor,
                modifier = Modifier.width(44.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentUser) GabBlue else GabSurfaceVariant)
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) GabOnPrimary else GabOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                color = GabTextDark,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GabBlue
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun GabCardPreview() {
    GabInovaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GabCard {
                Text("Título do card", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Conteúdo genérico reutilizável.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    GabInovaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(value = "42", label = "Ideias",   icon = Icons.Filled.Lightbulb, modifier = Modifier.weight(1f))
            StatCard(value = "7",  label = "Projetos", icon = Icons.Filled.Work,      modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressCardPreview() {
    GabInovaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ProgressCard(title = "Metas do Mês", current = 6, total = 10, description = "Faltam 4 metas")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeCardPreview() {
    GabInovaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgeCard(title = "Inovador",   description = "10 ideias enviadas", icon = Icons.Filled.EmojiEvents, earned = true,  modifier = Modifier.weight(1f))
            BadgeCard(title = "Explorador", description = "5 projetos abertos", icon = Icons.Filled.Explore,     earned = false, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingCardPreview() {
    GabInovaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RankingCard(position = 1, name = "Ana Paula",   score = 980)
            RankingCard(position = 2, name = "Danilo Cruz", score = 850, isCurrentUser = true)
            RankingCard(position = 3, name = "Carlos Lima", score = 720)
            RankingCard(position = 4, name = "Maria Souza", score = 610)
        }
    }
}
