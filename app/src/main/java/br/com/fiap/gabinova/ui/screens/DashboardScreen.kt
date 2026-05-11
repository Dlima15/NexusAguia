package br.com.fiap.gabinova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.gabinova.model.UserRole
import br.com.fiap.gabinova.ui.components.SectionTitle
import br.com.fiap.gabinova.ui.components.StatCard
import br.com.fiap.gabinova.ui.theme.GabBackground
import br.com.fiap.gabinova.ui.theme.GabBlue
import br.com.fiap.gabinova.ui.theme.GabError
import br.com.fiap.gabinova.ui.theme.GabGreen
import br.com.fiap.gabinova.ui.theme.GabInovaTheme
import br.com.fiap.gabinova.ui.theme.GabLightBlue
import br.com.fiap.gabinova.ui.theme.GabOnPrimary
import br.com.fiap.gabinova.ui.theme.GabOnSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabSurface
import br.com.fiap.gabinova.ui.theme.GabSurfaceVariant
import br.com.fiap.gabinova.ui.theme.GabTextDark
import br.com.fiap.gabinova.ui.theme.GabYellow
import br.com.fiap.gabinova.ui.viewmodel.DashboardData
import br.com.fiap.gabinova.ui.viewmodel.DashboardUiState
import br.com.fiap.gabinova.ui.viewmodel.DashboardViewModel
import br.com.fiap.gabinova.ui.viewmodel.DashboardViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun currentPeriod(): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
    return sdf.format(Date()).replaceFirstChar { it.uppercase() }
}

private fun roiColor(roi: Double) = when {
    roi >= 100.0 -> GabGreen
    roi >= 50.0  -> GabYellow
    else         -> GabError
}

private fun roiLabel(roi: Double) = when {
    roi >= 100.0 -> "Excelente"
    roi >= 50.0  -> "Atingido"
    else         -> "Atenção"
}

private fun roiTextColor(roi: Double) = if (roi in 50.0..99.9) GabTextDark else Color.White

private fun engagementColor(rate: Double) = when {
    rate >= 60.0 -> GabGreen
    rate >= 40.0 -> GabYellow
    else         -> GabError
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(context))

    when {
        vm.state.isLoading -> Box(
            modifier            = Modifier.fillMaxSize().background(GabBackground),
            contentAlignment    = Alignment.Center
        ) { CircularProgressIndicator(color = GabBlue) }

        vm.state.error != null -> Box(
            modifier         = Modifier.fillMaxSize().background(GabBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(vm.state.error!!, color = GabError, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = vm::retry,
                    colors  = ButtonDefaults.buttonColors(containerColor = GabBlue)
                ) { Text("Tentar novamente", color = Color.White) }
            }
        }

        else -> DashboardContent(state = vm.state)
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
internal fun DashboardContent(state: DashboardUiState) {
    val data = state.data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GabBackground)
            .verticalScroll(rememberScrollState())
    ) {
        ExecutiveHeader(data = data, userRole = state.userRole)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Quick overview tiles
            QuickStatsGrid(data = data)

            // ── Ideias ─────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Ideias & Inovação")
            Spacer(modifier = Modifier.height(10.dp))
            IdeaInsightsCard(data = data)

            // ── Projetos ───────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Projetos")
            Spacer(modifier = Modifier.height(10.dp))
            ProjectsOverviewSection(data = data)

            // ── Financeiro (Liderança only) ────────────────────────────────
            if (state.isLeadership) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(title = "Resultados Financeiros")
                Spacer(modifier = Modifier.height(10.dp))
                FinancialSection(data = data)
            }

            // ── Engajamento ────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(title = "Engajamento")
            Spacer(modifier = Modifier.height(10.dp))
            EngagementSection(data = data, showDetails = state.isLeadership)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Executive header ───────────────────────────────────────────────────────────

@Composable
private fun ExecutiveHeader(data: DashboardData, userRole: UserRole) {
    val isLeadership = userRole == UserRole.ADMIN || userRole == UserRole.ANALYST
    val title        = if (isLeadership) "Painel Executivo" else "Resumo Operacional"
    val subtitle     = if (isLeadership) "Visão estratégica consolidada" else "Indicadores do período"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GabBlue)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = GabOnPrimary
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text  = currentPeriod(),
                        style = MaterialTheme.typography.labelMedium,
                        color = GabOnPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "Nexus Águia · Grupo Águia Branca",
                style = MaterialTheme.typography.bodySmall,
                color = GabOnPrimary.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GabOnPrimary.copy(alpha = 0.55f)
            )
        }
    }
}

// ── Quick stats grid (4 tiles) ─────────────────────────────────────────────────

@Composable
private fun QuickStatsGrid(data: DashboardData) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickTile(
            value    = "${data.totalIdeas}",
            label    = "Ideias",
            color    = GabBlue,
            modifier = Modifier.weight(1f)
        )
        QuickTile(
            value    = "${data.approvedIdeas}",
            label    = "Aprovadas",
            color    = GabGreen,
            modifier = Modifier.weight(1f)
        )
        QuickTile(
            value    = "${data.activeProjects}",
            label    = "Ativos",
            color    = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        QuickTile(
            value    = "${data.completedProjects}",
            label    = "Concluídos",
            color    = GabLightBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickTile(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(color)
        )
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = color,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = GabOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Idea insights card ─────────────────────────────────────────────────────────

@Composable
private fun IdeaInsightsCard(data: DashboardData) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Approval rate
            MetricProgressRow(
                label    = "Taxa de Aprovação",
                valueStr = "${String.format("%.1f", data.approvalRate)}%",
                progress = (data.approvalRate / 100.0).toFloat().coerceIn(0f, 1f),
                barColor = GabGreen
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Mini metric row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniMetric(
                    value = "${data.ideasThisMonth}",
                    label = "Ideias/mês"
                )
                ColumnDivider()
                MiniMetric(
                    value = "${data.totalIdeas - data.approvedIdeas - data.inReviewIdeas}",
                    label = "Rejeitadas"
                )
                ColumnDivider()
                MiniMetric(
                    value = "${data.inReviewIdeas}",
                    label = "Em Análise"
                )
                ColumnDivider()
                MiniMetric(
                    value = "${data.approvedIdeas}",
                    label = "Aprovadas"
                )
            }
        }
    }
}

// ── Projects overview section ──────────────────────────────────────────────────

@Composable
private fun ProjectsOverviewSection(data: DashboardData) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(
            value    = "${data.activeProjects}",
            label    = "Em Andamento",
            icon     = Icons.AutoMirrored.Filled.TrendingUp,
            iconTint = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value    = "${data.completedProjects}",
            label    = "Concluídos",
            icon     = Icons.Filled.CheckCircle,
            iconTint = GabGreen,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricProgressRow(
                label    = "Taxa de Conclusão",
                valueStr = "${String.format("%.0f", data.completionRate)}%",
                progress = (data.completionRate / 100.0).toFloat().coerceIn(0f, 1f),
                barColor = GabBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = "${data.completedProjects} de ${data.totalProjects} projetos concluídos",
                style = MaterialTheme.typography.bodySmall,
                color = GabOnSurfaceVariant
            )
        }
    }
}

// ── Financial section (Liderança only) ────────────────────────────────────────

@Composable
private fun FinancialSection(data: DashboardData) {
    // Investment + Return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            label    = "Investimento Total",
            value    = data.totalInvestment,
            caption  = "Recursos alocados",
            color    = GabBlue,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label    = "Retorno Financeiro",
            value    = data.financialReturn,
            caption  = "Resultado gerado",
            color    = GabGreen,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // ROI — prominent full-width card
    RoiCard(roi = data.roi)

    Spacer(modifier = Modifier.height(8.dp))

    // Economy + Productivity
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            label    = "Economia Gerada",
            value    = data.economyGenerated,
            caption  = "Custos evitados",
            color    = GabLightBlue,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label    = "Ganho Produtividade",
            value    = data.productivityGain,
            caption  = "Média dos projetos",
            color    = GabGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Engagement section ─────────────────────────────────────────────────────────

@Composable
private fun EngagementSection(data: DashboardData, showDetails: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(
            value    = "${data.engagedCollaborators}",
            label    = "Engajados",
            icon     = Icons.Filled.Groups,
            iconTint = GabBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value    = "${data.totalCollaborators - data.engagedCollaborators}",
            label    = "Sem Registro",
            icon     = Icons.Filled.Work,
            iconTint = GabOnSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Collaborator progress bar
            MetricProgressRow(
                label    = "Colaboradores Engajados",
                valueStr = "${data.engagedCollaborators} / ${data.totalCollaborators}",
                progress = data.engagementFraction,
                barColor = engagementColor(data.engagementRate)
            )

            if (showDetails) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Engagement rate badge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .background(
                                    engagementColor(data.engagementRate),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text       = "${String.format("%.1f", data.engagementRate)}%",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Taxa de Engajamento",
                            style = MaterialTheme.typography.labelSmall,
                            color = GabOnSurfaceVariant
                        )
                    }

                    ColumnDivider()

                    // Top guideline
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = data.topGuideline,
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = GabBlue,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text      = "Diretriz Principal",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = GabOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Gestor — simplified rate
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Taxa de Engajamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = GabOnSurfaceVariant
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(
                                engagementColor(data.engagementRate),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text       = "${String.format("%.1f", data.engagementRate)}%",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── ROI card ───────────────────────────────────────────────────────────────────

@Composable
private fun RoiCard(roi: Double) {
    val color    = roiColor(roi)
    val label    = roiLabel(roi)
    val txtColor = roiTextColor(roi)
    val progress = (roi / 200.0).toFloat().coerceIn(0f, 1f)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(color)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Retorno sobre Investimento (ROI)",
                style = MaterialTheme.typography.labelSmall,
                color = GabOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "${String.format("%.1f", roi)}%",
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = color
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .background(color, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text       = label,
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = txtColor
                        )
                    }
                    if (roi >= 100.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "+${String.format("%.1f", roi - 100.0)}% acima da meta",
                            style = MaterialTheme.typography.labelSmall,
                            color = GabGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0%", style = MaterialTheme.typography.labelSmall, color = GabOnSurfaceVariant)
                Text("Meta 100%", style = MaterialTheme.typography.labelSmall, color = GabOnSurfaceVariant)
                Text("200%", style = MaterialTheme.typography.labelSmall, color = GabOnSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth(),
                color      = color,
                trackColor = GabSurfaceVariant
            )
        }
    }
}

// ── KPI card ───────────────────────────────────────────────────────────────────

@Composable
private fun KpiCard(
    label: String,
    value: String,
    caption: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = GabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(color)
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = GabOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = caption,
                style = MaterialTheme.typography.labelSmall,
                color = GabOnSurfaceVariant
            )
        }
    }
}

// ── Shared micro-composables ───────────────────────────────────────────────────

@Composable
private fun MetricProgressRow(
    label: String,
    valueStr: String,
    progress: Float,
    barColor: Color
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = GabTextDark
        )
        Text(
            text       = valueStr,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = barColor
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    LinearProgressIndicator(
        progress   = { progress },
        modifier   = Modifier.fillMaxWidth(),
        color      = barColor,
        trackColor = GabSurfaceVariant
    )
}

@Composable
private fun MiniMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color      = GabBlue
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = GabOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColumnDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(GabSurfaceVariant)
    )
}

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Dashboard — Liderança")
@Composable
private fun DashboardLiderancaPreview() {
    GabInovaTheme {
        DashboardContent(
            state = DashboardUiState(userRole = UserRole.ADMIN)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Dashboard — Gestor")
@Composable
private fun DashboardGestorPreview() {
    GabInovaTheme {
        DashboardContent(
            state = DashboardUiState(userRole = UserRole.MANAGER)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Dashboard — ROI Card")
@Composable
private fun RoiCardPreview() {
    GabInovaTheme {
        Column(
            modifier  = Modifier
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoiCard(roi = 111.3)
            RoiCard(roi = 67.5)
            RoiCard(roi = 32.1)
        }
    }
}

@Preview(showBackground = true, name = "Dashboard — KPI Cards")
@Composable
private fun KpiCardsPreview() {
    GabInovaTheme {
        Column(
            modifier  = Modifier
                .background(GabBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Investimento Total", "R\$ 345.000", "Recursos alocados", GabBlue, Modifier.weight(1f))
                KpiCard("Retorno Financeiro", "R\$ 728.000", "Resultado gerado", GabGreen, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Economia Gerada", "R\$ 92.000", "Custos evitados", GabLightBlue, Modifier.weight(1f))
                KpiCard("Ganho Produtividade", "18%", "Média dos projetos", GabGreen, Modifier.weight(1f))
            }
        }
    }
}
