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
import androidx.compose.material.icons.automirrored.rounded.ShowChart
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import io.github.koalaplot.core.xygraph.AxisContent
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.domain.MetricPoint
import ru.aiss83.comunalexpenses2.domain.downsampleLabels
import ru.aiss83.comunalexpenses2.domain.metricSeries
import ru.aiss83.comunalexpenses2.domain.summarize
import ru.aiss83.comunalexpenses2.ui.theme.ResourceBlue
import ru.aiss83.comunalexpenses2.ui.theme.ResourceOrange
import ru.aiss83.comunalexpenses2.ui.theme.ResourcePurple
import ru.aiss83.comunalexpenses2.ui.theme.ResourceRed
import ru.aiss83.comunalexpenses2.utils.MILLIS_PER_DAY
import ru.aiss83.comunalexpenses2.utils.PlatformBackHandler
import ru.aiss83.comunalexpenses2.utils.formatDayKey
import ru.aiss83.comunalexpenses2.utils.toDayKey

/** Rough month length used by the period chips. */
private const val MONTH_MILLIS = 30L * MILLIS_PER_DAY

/** How many date labels stay readable on the chart. */
private const val MAX_AXIS_LABELS = 6

private enum class Metric(
    val label: StringResource,
    val color: Color,
    val extractor: (ResourceData) -> Long
) {
    COLD_WATER(Res.string.stats_cold_water, ResourceBlue, { it.coldWater }),
    HOT_WATER(Res.string.stats_hot_water, ResourceRed, { it.hotWater }),
    DAY_ELECTRICITY(Res.string.stats_day_electricity, ResourceOrange, { it.dayElectricity }),
    NIGHT_ELECTRICITY(Res.string.stats_night_electricity, ResourcePurple, { it.nightElectricity });

    /** Bit of the saveable selection mask. */
    val bit: Int get() = 1 shl ordinal
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    allResourceData: List<ResourceData>,
    onNavigateBack: () -> Unit
) {
    // A bitmask keeps the selection in rememberSaveable-friendly state (Set<Int> is not saveable).
    var selectedMask by rememberSaveable { mutableStateOf(Metric.COLD_WATER.bit) }
    var deltaMode by rememberSaveable { mutableStateOf(false) }
    var periodMonths by rememberSaveable { mutableStateOf(0) } // 0 = all time

    PlatformBackHandler(enabled = true, onBack = onNavigateBack)

    val sortedData = remember(allResourceData) { allResourceData.sortedBy { it.date } }
    val cutoff = remember(periodMonths) {
        if (periodMonths == 0) {
            null
        } else {
            // Cut-off is a day key, matching record dates.
            Clock.System.now().toEpochMilliseconds().toDayKey() - periodMonths.toLong() * MONTH_MILLIS
        }
    }

    // Each metric is computed exactly once per data/filter change instead of once per plotted
    // point (the previous implementation was O(n^2) and re-allocated lists inside composition).
    val seriesByMetric = remember(sortedData, deltaMode, cutoff) {
        Metric.entries.associateWith { metric ->
            val points = metricSeries(sortedData, metric.extractor, deltaMode)
            if (cutoff == null) points else points.filter { it.date >= cutoff }
        }
    }

    val visibleMetrics = remember(selectedMask) {
        // Guard against a corrupted saved mask: at least one metric must stay visible.
        Metric.entries.filter { selectedMask and it.bit != 0 }.ifEmpty { listOf(Metric.COLD_WATER) }
    }
    val primarySeries = seriesByMetric[visibleMetrics.first()].orEmpty()
    val summary = remember(primarySeries) { summarize(primarySeries.map { it.value }) }
    val chartSeries = remember(seriesByMetric, visibleMetrics) {
        visibleMetrics.map { metric -> metric to seriesByMetric[metric].orEmpty() }
    }
    val labels = remember(chartSeries) {
        chartSeries.firstOrNull()?.second?.map { it.date.formatDayKey() }.orEmpty()
    }

    val hasData = allResourceData.isNotEmpty()

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
            // Filters — hidden when no data
            if (hasData) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Metric.entries.forEach { metric ->
                        val selected = selectedMask and metric.bit != 0
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedMask = if (selected) {
                                    // Never let the user deselect the last metric.
                                    if (selectedMask.countOneBits() > 1) selectedMask and metric.bit.inv() else selectedMask
                                } else {
                                    selectedMask or metric.bit
                                }
                            },
                            label = { Text(stringResource(metric.label), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (selected) {
                                { Box(Modifier.size(10.dp).background(metric.color, MaterialTheme.shapes.extraSmall)) }
                            } else null
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = deltaMode,
                        onClick = { deltaMode = !deltaMode },
                        label = { Text(stringResource(Res.string.stats_delta_mode), style = MaterialTheme.typography.labelSmall) },
                    )
                    periodChips().forEach { (months, labelRes) ->
                        FilterChip(
                            selected = periodMonths == months,
                            onClick = { periodMonths = if (periodMonths == months) 0 else months },
                            label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (!hasData) {
                // Empty state: no records at all
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ShowChart,
                            stringResource(Res.string.stats_empty_icon_desc),
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(Res.string.stats_no_data),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(Res.string.stats_no_data_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else if (labels.size < 2) {
                // Need at least 2 points for a chart
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.stats_need_more),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                KoalaLineChart(
                    labels = labels,
                    series = chartSeries,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Spacer(Modifier.height(8.dp))

                if (summary != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(stringResource(Res.string.stats_min), summary.min.toString())
                        StatItem(stringResource(Res.string.stats_max), summary.max.toString())
                        StatItem(stringResource(Res.string.stats_avg), summary.average.toString())
                        if (deltaMode) {
                            // Sum of per-period consumption is meaningful.
                            StatItem(stringResource(Res.string.stats_total), summary.total.toString())
                        } else {
                            // Sum of absolute meter readings is not, so show the latest reading.
                            StatItem(stringResource(Res.string.stats_last), summary.last.toString())
                        }
                    }
                }
            }
        }
    }
}

private fun periodChips(): List<Pair<Int, StringResource>> = listOf(
    3 to Res.string.stats_period_3m,
    6 to Res.string.stats_period_6m,
    12 to Res.string.stats_period_year
)

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
    labels: List<String>,
    series: List<Pair<Metric, List<MetricPoint>>>,
    modifier: Modifier = Modifier
) {
    // The category is the point index, not the formatted date: two readings taken on the same
    // day would otherwise map to one category and collapse onto a single x position.
    val categories = remember(labels) { labels.indices.toList() }
    // Only a handful of dates are drawn to keep the axis readable.
    val shownLabels = remember(labels) { downsampleLabels(labels, MAX_AXIS_LABELS) }
    val labelIndicesToDraw = remember(shownLabels) {
        shownLabels.indices.filter { shownLabels[it].isNotEmpty() }.toSet()
    }

    val allValues = remember(series) { series.flatMap { (_, points) -> points.map { it.value } } }
    val minVal = (allValues.minOrNull() ?: 0L).toFloat()
    // Ensure range is at least 1 to avoid zero-range axis crash
    val maxVal = (allValues.maxOrNull() ?: 1L).toFloat().coerceAtLeast(minVal + 1f)

    ChartLayout(modifier = modifier) {
        XYGraph(
            xAxisModel = CategoryAxisModel(categories),
            yAxisModel = FloatLinearAxisModel(minVal..maxVal),
            xAxisContent = AxisContent(
                labels = { index ->
                    val text = if (index in labelIndicesToDraw) labels.getOrElse(index) { "" } else ""
                    Text(text = text, style = MaterialTheme.typography.labelSmall)
                },
                title = {},
                style = rememberAxisStyle(),
            ),
            yAxisContent = AxisContent(
                labels = { value -> Text(formatAxisValue(value.toLong()), style = MaterialTheme.typography.labelSmall) },
                title = {},
                style = rememberAxisStyle(),
            ),
        ) {
            series.forEach { (metric, points) ->
                val data = points.mapIndexed { index, point ->
                    DefaultPoint(index, point.value.toFloat())
                }
                @Suppress("DEPRECATION")
                LinePlot(
                    data = data,
                    lineStyle = LineStyle(brush = SolidColor(metric.color), strokeWidth = 2.dp),
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
