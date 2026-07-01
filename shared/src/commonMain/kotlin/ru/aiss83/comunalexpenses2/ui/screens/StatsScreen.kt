package ru.aiss83.comunalexpenses2.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData

private val metricColors = listOf(
    Color(0xFF2196F3),
    Color(0xFFF44336),
    Color(0xFFFF9800),
    Color(0xFF9C27B0)
)

private data class MetricDef(
    val label: String,
    val extractor: (ResourceData) -> Long,
    val color: Color
)

private data class PeriodFilter(val label: String, val months: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    allResourceData: List<ResourceData>,
    onNavigateBack: () -> Unit
) {
    var selectedMetrics by remember { mutableStateOf(setOf(0)) }
    var deltaMode by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf<PeriodFilter?>(null) }

    val metrics = listOf(
        MetricDef(stringResource(Res.string.stats_cold_water), { it.coldWater }, metricColors[0]),
        MetricDef(stringResource(Res.string.stats_hot_water), { it.hotWater }, metricColors[1]),
        MetricDef(stringResource(Res.string.stats_day_electricity), { it.dayElectricity }, metricColors[2]),
        MetricDef(stringResource(Res.string.stats_night_electricity), { it.nightElectricity }, metricColors[3])
    )

    val now = Clock.System.now().toEpochMilliseconds()
    val periods = listOf(
        PeriodFilter(stringResource(Res.string.stats_period_3m), 3),
        PeriodFilter(stringResource(Res.string.stats_period_6m), 6),
        PeriodFilter(stringResource(Res.string.stats_period_year), 12),
    )

    val sortedData = allResourceData.sortedBy { it.date }
    val filteredData = if (selectedPeriod != null) {
        val cutoff = now - selectedPeriod!!.months * 30L * 24 * 3600 * 1000
        sortedData.filter { it.date >= cutoff }
    } else sortedData

    fun metricValues(extractor: (ResourceData) -> Long): List<Long> {
        val raw = filteredData.map(extractor)
        return if (deltaMode) {
            raw.mapIndexed { i, v -> if (i == 0) 0L else (v - raw[i - 1]).coerceAtLeast(0) }
        } else raw
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.settings_back_content_desc))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Metric chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                metrics.forEachIndexed { index, metric ->
                    FilterChip(
                        selected = index in selectedMetrics,
                        onClick = {
                            selectedMetrics = if (index in selectedMetrics) {
                                if (selectedMetrics.size > 1) selectedMetrics - index else selectedMetrics
                            } else selectedMetrics + index
                        },
                        label = { Text(metric.label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (index in selectedMetrics) {
                            { Box(Modifier.size(10.dp).background(metric.color, MaterialTheme.shapes.extraSmall)) }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Delta toggle + period filters
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = deltaMode,
                    onClick = { deltaMode = !deltaMode },
                    label = { Text(stringResource(Res.string.stats_delta_mode), style = MaterialTheme.typography.labelSmall) },
                )
                periods.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = if (selectedPeriod == period) null else period },
                        label = { Text(period.label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredData.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.stats_no_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                KoalaLineChart(
                    data = filteredData,
                    selectedMetrics = selectedMetrics,
                    metrics = metrics,
                    valueFn = { m, r -> metricValues(m.extractor).let { vals -> vals[filteredData.indexOf(r)] } },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Spacer(Modifier.height(8.dp))

                val primary = selectedMetrics.first()
                val values = metricValues(metrics[primary].extractor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(stringResource(Res.string.stats_min), (values.minOrNull() ?: 0).toString())
                    StatItem(stringResource(Res.string.stats_max), (values.maxOrNull() ?: 0).toString())
                    StatItem(stringResource(Res.string.stats_avg), values.average().toLong().toString())
                    StatItem(stringResource(Res.string.stats_total), values.sum().toString())
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun KoalaLineChart(
    data: List<ResourceData>,
    selectedMetrics: Set<Int>,
    metrics: List<MetricDef>,
    valueFn: (MetricDef, ResourceData) -> Long,
    modifier: Modifier = Modifier
) {
    val dateLabels = data.map { it.formatDate() }
    val allValues = selectedMetrics.flatMap { idx -> data.map { valueFn(metrics[idx], it) } }
    val minVal = (allValues.minOrNull() ?: 0L).toFloat()
    val maxVal = (allValues.maxOrNull() ?: 1L).toFloat().coerceAtLeast(minVal + 1f)

    ChartLayout(modifier = modifier) {
        XYGraph(
            xAxisModel = CategoryAxisModel(dateLabels),
            yAxisModel = FloatLinearAxisModel(minVal..maxVal),
            xAxisContent = AxisContent(
                labels = { Text(it, style = MaterialTheme.typography.labelSmall) },
                title = {},
                style = rememberAxisStyle(),
            ),
            yAxisContent = AxisContent(
                labels = { Text(formatAxisValue(it.toLong()), style = MaterialTheme.typography.labelSmall) },
                title = {},
                style = rememberAxisStyle(),
            ),
        ) {
            selectedMetrics.forEach { idx ->
                val points = data.map { record ->
                    DefaultPoint(record.formatDate(), valueFn(metrics[idx], record).toFloat())
                }
                LinePlot(
                    data = points,
                    lineStyle = LineStyle(brush = SolidColor(metrics[idx].color), strokeWidth = 2.dp),
                )
            }
        }
    }
}

private fun formatAxisValue(value: Long): String = when {
    value >= 1_000_000 -> "${value / 1_000_000}M"
    value >= 10_000 -> "${value / 1_000}K"
    else -> value.toString()
}
