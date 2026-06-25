package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData

private val metricColors = listOf(
    Color(0xFF2196F3), // blue - cold water
    Color(0xFFF44336), // red - hot water
    Color(0xFFFF9800), // orange - day electricity
    Color(0xFF9C27B0)  // purple - night electricity
)

private data class MetricDef(
    val label: String,
    val extractor: (ResourceData) -> Long,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    allResourceData: List<ResourceData>,
    onNavigateBack: () -> Unit
) {
    var selectedMetrics by remember { mutableStateOf(setOf(0)) }

    val metrics = listOf(
        MetricDef(stringResource(Res.string.stats_cold_water), { it.coldWater }, metricColors[0]),
        MetricDef(stringResource(Res.string.stats_hot_water), { it.hotWater }, metricColors[1]),
        MetricDef(stringResource(Res.string.stats_day_electricity), { it.dayElectricity }, metricColors[2]),
        MetricDef(stringResource(Res.string.stats_night_electricity), { it.nightElectricity }, metricColors[3])
    )

    val sortedData = allResourceData.sortedBy { it.date }
    val dateLabels = sortedData.map { it.formatDate() }

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
            // Multi-select metric chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                metrics.forEachIndexed { index, metric ->
                    FilterChip(
                        selected = index in selectedMetrics,
                        onClick = {
                            selectedMetrics = if (index in selectedMetrics) {
                                if (selectedMetrics.size > 1) selectedMetrics - index else selectedMetrics
                            } else {
                                selectedMetrics + index
                            }
                        },
                        label = { Text(metric.label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (index in selectedMetrics) {
                            {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(metric.color, MaterialTheme.shapes.extraSmall)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (sortedData.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.stats_no_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Chart
                MultiLineChart(
                    data = sortedData,
                    dateLabels = dateLabels,
                    selectedMetrics = selectedMetrics,
                    metrics = metrics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(8.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    selectedMetrics.forEachIndexed { i, idx ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .background(metrics[idx].color, MaterialTheme.shapes.extraSmall)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(metrics[idx].label, style = MaterialTheme.typography.labelSmall)
                        }
                        if (i < selectedMetrics.size - 1) {
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Stats summary for primary metric
                val primary = selectedMetrics.first()
                val values = sortedData.map { metrics[primary].extractor(it) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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

@Composable
private fun MultiLineChart(
    data: List<ResourceData>,
    dateLabels: List<String>,
    selectedMetrics: Set<Int>,
    metrics: List<MetricDef>,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            val axisLeft = 60f
            val axisBottom = 30f
            val chartW = size.width * scale - axisLeft
            val chartH = size.height * scale - axisBottom

            // Collect all values across selected metrics for Y-axis range
            val allValues = selectedMetrics.flatMap { idx ->
                data.map { metrics[idx].extractor(it) }
            }
            val minVal = allValues.minOrNull() ?: 0L
            val maxVal = allValues.maxOrNull() ?: 1L
            val range = (maxVal - minVal).coerceAtLeast(1)

            // --- Axes ---
            val axisColor = Color.Gray

            // Y-axis line
            drawLine(axisColor, Offset(offsetX + axisLeft, offsetY), Offset(offsetX + axisLeft, offsetY + chartH), 2f)
            // X-axis line
            drawLine(axisColor, Offset(offsetX + axisLeft, offsetY + chartH), Offset(offsetX + axisLeft + chartW, offsetY + chartH), 2f)

            // Y-axis labels + grid lines
            val yTicks = 5
            for (i in 0..yTicks) {
                val value = minVal + (range * i / yTicks)
                val y = offsetY + chartH - (chartH * i / yTicks)

                // Grid line
                drawLine(
                    Color.LightGray.copy(alpha = 0.3f),
                    Offset(offsetX + axisLeft, y),
                    Offset(offsetX + axisLeft + chartW, y),
                    1f
                )

                // Y label
                val label = if (value > 9999) "${value / 1000}k" else value.toString()
                val result = textMeasurer.measure(label, style = labelStyle)
                drawText(
                    result,
                    topLeft = Offset(offsetX + axisLeft - result.size.width - 6f, y - result.size.height / 2f)
                )
            }

            // X-axis labels (first + last)
            if (dateLabels.isNotEmpty()) {
                val firstResult = textMeasurer.measure(dateLabels.first(), style = labelStyle)
                drawText(firstResult, topLeft = Offset(offsetX + axisLeft, offsetY + chartH + 6f))

                val lastResult = textMeasurer.measure(dateLabels.last(), style = labelStyle)
                drawText(
                    lastResult,
                    topLeft = Offset(offsetX + axisLeft + chartW - lastResult.size.width, offsetY + chartH + 6f)
                )
            }

            if (data.size < 2) return@Canvas

            val stepX = chartW / (data.size - 1).coerceAtLeast(1)

            // Draw lines for each selected metric
            selectedMetrics.forEach { metricIdx ->
                val color = metrics[metricIdx].color
                val points = data.mapIndexed { i, record ->
                    val value = metrics[metricIdx].extractor(record)
                    val x = offsetX + axisLeft + stepX * i
                    val y = offsetY + chartH - ((value - minVal).toFloat() / range * chartH)
                    Offset(x, y)
                }

                // Draw line segments
                for (i in 0 until points.size - 1) {
                    drawLine(color, points[i], points[i + 1], 3f)
                }

                // Draw data points
                points.forEach { point ->
                    drawCircle(color, 5f, point)
                }
            }
        }
    }
}
