package ru.aiss83.comunalexpenses2.domain

import ru.aiss83.comunalexpenses2.data.ResourceData

/**
 * A single point of a metric series: the record date and the value to plot.
 * In cumulative mode [value] is the raw meter reading, in consumption mode it is the
 * difference to the previous record.
 */
data class MetricPoint(val date: Long, val value: Long)

/**
 * Summary of the values currently plotted under the chart.
 */
data class StatsSummary(
    val min: Long,
    val max: Long,
    val average: Long,
    val total: Long,
    val last: Long
)

/**
 * Builds a metric series over [records], which are expected to be ordered by date ascending.
 *
 * In [deltaMode] the value is the consumption since the previous record. The oldest record has
 * no predecessor and yields 0. Negative differences (a meter change or a correction) are clamped
 * to 0 so they cannot distort the chart scale.
 */
fun metricSeries(
    records: List<ResourceData>,
    extractor: (ResourceData) -> Long,
    deltaMode: Boolean
): List<MetricPoint> = records.mapIndexed { index, record ->
    val raw = extractor(record)
    val value = if (!deltaMode) {
        raw
    } else if (index == 0) {
        0L
    } else {
        (raw - extractor(records[index - 1])).coerceAtLeast(0L)
    }
    MetricPoint(date = record.date, value = value)
}

/**
 * Computes min / max / average / total / last over the plotted values.
 * Returns `null` for an empty series.
 */
fun summarize(values: List<Long>): StatsSummary? {
    if (values.isEmpty()) return null

    var min = values.first()
    var max = values.first()
    var total = 0L
    for (value in values) {
        if (value < min) min = value
        if (value > max) max = value
        total += value
    }
    return StatsSummary(
        min = min,
        max = max,
        average = total / values.size,
        total = total,
        last = values.last()
    )
}

/**
 * Keeps at most [maxLabels] axis labels readable: every n-th label is kept, the rest become
 * empty strings, which is much cheaper than drawing hundreds of overlapping dates.
 *
 * The returned list always has the same size as [labels] so it can be mapped by index.
 */
fun downsampleLabels(labels: List<String>, maxLabels: Int): List<String> {
    if (maxLabels <= 0 || labels.size <= maxLabels) return labels

    val step = ((labels.size + maxLabels - 1) / maxLabels).coerceAtLeast(1)
    return labels.mapIndexed { index, label ->
        if (index % step == 0 || index == labels.lastIndex) label else ""
    }
}
