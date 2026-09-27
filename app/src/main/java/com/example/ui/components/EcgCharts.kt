package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaultEntity
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgStatusInProgress
import com.example.ui.theme.EcgStatusPending
import com.example.ui.theme.EcgStatusResolved
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow
import kotlin.math.max

@Composable
fun StatCardsGrid(
    faults: List<FaultEntity>,
    modifier: Modifier = Modifier
) {
    val total = faults.size
    val pending = faults.count { it.status.equals("Pending", ignoreCase = true) }
    val inProgress = faults.count { it.status.equals("In Progress", ignoreCase = true) }
    val resolved = faults.count { it.status.equals("Resolved", ignoreCase = true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Total Faults",
                count = total,
                icon = Icons.Default.Warning,
                accentColor = EcgYellow,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_card_total")
            )
            StatCard(
                title = "Pending Action",
                count = pending,
                icon = Icons.Default.HourglassTop,
                accentColor = EcgStatusPending,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_card_pending")
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "In Progress",
                count = inProgress,
                icon = Icons.Default.Build,
                accentColor = EcgStatusInProgress,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_card_in_progress")
            )
            StatCard(
                title = "Resolved",
                count = resolved,
                icon = Icons.Default.CheckCircle,
                accentColor = EcgStatusResolved,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_card_resolved")
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Subtle left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = EcgTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EcgWhite
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BarChartFaultsByType(
    faults: List<FaultEntity>,
    modifier: Modifier = Modifier
) {
    val grouped = faults.groupingBy { it.faultType }.eachCount()
    val defaultTypes = listOf(
        "Transformer Outage",
        "Line Drop",
        "Low Voltage",
        "Burnt Pole",
        "Meter Fault",
        "Phase Failure"
    )

    val data = defaultTypes.map { type ->
        val count = grouped.entries.firstOrNull { it.key.contains(type.split(" ")[0], ignoreCase = true) }?.value ?: 0
        type to count
    }

    val maxVal = max(data.maxOfOrNull { it.second } ?: 1, 1)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(faults) {
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bar_chart_card"),
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Faults by Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Text(
                    text = "Bar Distribution",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcgTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("bar_chart_canvas")
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = data.size
                val spacing = 16.dp.toPx()
                val totalSpacing = spacing * (barCount + 1)
                val barWidth = (canvasWidth - totalSpacing) / barCount
                val chartBottom = canvasHeight - 28.dp.toPx()

                // Draw horizontal baseline
                drawLine(
                    color = Color(0xFF383838),
                    start = Offset(0f, chartBottom),
                    end = Offset(canvasWidth, chartBottom),
                    strokeWidth = 2.dp.toPx()
                )

                data.forEachIndexed { index, (label, count) ->
                    val barHeight = if (maxVal > 0) {
                        (count.toFloat() / maxVal) * (chartBottom - 30.dp.toPx()) * animProgress.value
                    } else 0f

                    val x = spacing + index * (barWidth + spacing)
                    val y = chartBottom - barHeight

                    // Draw bar
                    drawRoundRect(
                        color = EcgYellow,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    // Draw count number above bar using nativeCanvas
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        count.toString(),
                        x + barWidth / 2,
                        y - 6.dp.toPx(),
                        paint
                    )

                    // Draw short label below
                    val shortLabel = when {
                        label.contains("Transformer", ignoreCase = true) -> "Txmr"
                        label.contains("Line", ignoreCase = true) -> "Line"
                        label.contains("Voltage", ignoreCase = true) -> "Volt"
                        label.contains("Pole", ignoreCase = true) -> "Pole"
                        label.contains("Meter", ignoreCase = true) -> "Mtr"
                        label.contains("Phase", ignoreCase = true) -> "Phase"
                        else -> label.take(4)
                    }

                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#A0A0A0")
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        shortLabel,
                        x + barWidth / 2,
                        canvasHeight - 6.dp.toPx(),
                        labelPaint
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoughnutChartFaultsByRegion(
    faults: List<FaultEntity>,
    modifier: Modifier = Modifier
) {
    val regionColors = listOf(
        Color(0xFFFFCC00), // Accra East (ECG Yellow)
        Color(0xFF29B6F6), // Accra West (Cyan)
        Color(0xFFAB47BC), // Ashanti (Purple)
        Color(0xFF00E676), // Tema (Green)
        Color(0xFFFF7043), // Eastern (Orange)
        Color(0xFF26A69A), // Western (Teal)
        Color(0xFFEC407A)  // Central (Pink)
    )

    val grouped = faults.groupingBy { it.region }.eachCount()
    val total = faults.size

    val data = if (grouped.isEmpty()) {
        listOf("Accra East" to 1)
    } else {
        grouped.toList().sortedByDescending { it.second }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(faults) {
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("doughnut_chart_card"),
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Faults by Region",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Text(
                    text = "Doughnut Breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcgTextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Doughnut Canvas with center total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .testTag("doughnut_canvas")
                ) {
                    val strokeWidth = 32.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    var startAngle = -90f
                    val divisor = max(total, 1).toFloat()

                    data.forEachIndexed { index, (_, count) ->
                        val sweepAngle = (count.toFloat() / divisor) * 360f * animProgress.value
                        val color = regionColors[index % regionColors.size]

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center text in doughnut
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = EcgWhite
                    )
                    Text(
                        text = "Faults",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcgTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.forEachIndexed { index, (region, count) ->
                    val color = regionColors[index % regionColors.size]
                    val pct = if (total > 0) (count * 100 / total) else 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color, CircleShape)
                        )
                        Text(
                            text = "$region: $count ($pct%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = EcgTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
