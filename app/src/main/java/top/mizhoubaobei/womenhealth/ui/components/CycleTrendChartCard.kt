package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

data class CyclePoint(
    val label: String,
    val cycleDays: Int,
    val startDate: LocalDate
)

@Composable
fun CycleTrendChartCard(
    records: List<PeriodRecord>,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val textMeasurer = rememberTextMeasurer()

    // 1. Calculate historical cycle intervals
    val cycleTrendData = remember(records) {
        val sorted = records.sortedBy {
            try {
                LocalDate.parse(it.startDate, formatter)
            } catch (e: Exception) {
                LocalDate.of(1970, 1, 1)
            }
        }
        val points = mutableListOf<CyclePoint>()
        for (i in 0 until sorted.size - 1) {
            try {
                val start1 = LocalDate.parse(sorted[i].startDate, formatter)
                val start2 = LocalDate.parse(sorted[i + 1].startDate, formatter)
                val days = ChronoUnit.DAYS.between(start1, start2).toInt()
                if (days in 15..90) { // Keep medically plausible cycle range
                    val label = "${start2.monthValue}/${start2.dayOfMonth}"
                    points.add(CyclePoint(label, days, start2))
                }
            } catch (e: Exception) {
                // Ignore parsing errors for corrupted historical data
            }
        }
        // Take last 6 cycles (covering roughly the past six months)
        points.takeLast(6)
    }

    // 2. Compute metrics for insights below the chart
    val averageCycle = if (cycleTrendData.isNotEmpty()) {
        cycleTrendData.map { it.cycleDays }.average().toInt()
    } else {
        28
    }

    val maxCycle = if (cycleTrendData.isNotEmpty()) cycleTrendData.maxOf { it.cycleDays } else 28
    val minCycle = if (cycleTrendData.isNotEmpty()) cycleTrendData.minOf { it.cycleDays } else 28
    val fluctuation = if (cycleTrendData.size >= 2) {
        val mean = averageCycle.toDouble()
        val variance = cycleTrendData.map { (it.cycleDays - mean) * (it.cycleDays - mean) }.sum() / cycleTrendData.size
        sqrt(variance)
    } else {
        0.0
    }

    val regularityMessage = when {
        cycleTrendData.size < 2 -> "积累至少两次记录自动生成波动趋势 🔍"
        fluctuation <= 1.5 -> "钟摆式极度规律（波动 ±${String.format("%.1f", fluctuation)}天），体质超赞！"
        fluctuation <= 3.0 -> "处于常规稳态区间（波动 ±${String.format("%.1f", fluctuation)}天），非常健康。"
        else -> "波动性稍大（波动 ±${String.format("%.1f", fluctuation)}天），请注意调理作息，规律饮食。"
    }

    // Standard styling values
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val neutralLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val textStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )

    // Animation progress for line rendering
    var triggerAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = cycleTrendData) {
        triggerAnimation = true
    }
    val animatedProgress = animateFloatAsState(
        targetValue = if (triggerAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "chart_line_animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cycle_length_trends_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "六个月生理周期规律性趋势",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "追踪两次行经相隔天数，辨析健康规律度",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Graph visualization area (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("cycle_trends_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    // Margins & responsive bounds
                    val leftPadding = 32.dp.toPx()
                    val rightPadding = 16.dp.toPx()
                    val topPadding = 20.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val chartWidth = width - leftPadding - rightPadding
                    val chartHeight = height - topPadding - bottomPadding

                    // Fix values boundaries for standard women's cycle length (15 to 45 days)
                    val minY = 15f
                    val maxY = 45f
                    val daysRange = maxY - minY

                    // Helper to translate cycle days to visual Y coordinate
                    fun getPixelY(days: Float): Float {
                        val percentage = (days - minY) / daysRange
                        return topPadding + chartHeight * (1f - percentage)
                    }

                    // 1. Draw Normal Area Shade (21 to 35 days represents healthy cycle bounds)
                    val normalYStart = getPixelY(35f)
                    val normalYEnd = getPixelY(21f)
                    drawRect(
                        color = Color(0xFFE6F4EA).copy(alpha = 0.45f), // Medical green tint
                        topLeft = Offset(leftPadding, normalYStart),
                        size = Size(chartWidth, normalYEnd - normalYStart)
                    )

                    // Write Normal Area Tag Label on background
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "健康规律带 (21~35 天)",
                        style = textStyle.copy(color = Color(0xFF137333), fontSize = 8.sp),
                        topLeft = Offset(leftPadding + 8.dp.toPx(), normalYStart + 4.dp.toPx())
                    )

                    // 2. Draw Horizontal Grid lines and axis labels
                    val gridDays = listOf(15f, 21f, 28f, 35f, 45f)
                    gridDays.forEach { days ->
                        val py = getPixelY(days)
                        // Grid line (reference line)
                        drawLine(
                            color = if (days == 28f) primaryColor.copy(alpha = 0.35f) else Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(leftPadding, py),
                            end = Offset(width - rightPadding, py),
                            strokeWidth = if (days == 28f) 1.5.dp.toPx() else 1.dp.toPx(),
                            pathEffect = if (days == 28f) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                        )

                        // Y-axis label text (Day number)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "${days.toInt()}d",
                            style = textStyle,
                            topLeft = Offset(4.dp.toPx(), py - 6.dp.toPx())
                        )
                    }

                    // 3. Draw standard cycle line highlight annotation at 28 days
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "经典标准 28 天",
                        style = textStyle.copy(color = primaryColor, fontWeight = FontWeight.SemiBold, fontSize = 9.sp),
                        topLeft = Offset(width - 92.dp.toPx(), getPixelY(28f) - 14.dp.toPx())
                    )

                    // 4. Draw actual cycle trends line or demo baseline if insufficient data
                    if (cycleTrendData.isEmpty()) {
                        // Demo mode/No records state dashed curve representing a standard cycle
                        val mockPoints = listOf(28f, 29f, 28f, 27f, 28f, 28f)
                        val pointsCount = mockPoints.size
                        val strokePath = Path()
                        val fillPath = Path()

                        for (i in 0 until pointsCount) {
                            val px = leftPadding + i * (chartWidth / (pointsCount - 1))
                            val py = getPixelY(mockPoints[i])
                            if (i == 0) {
                                strokePath.moveTo(px, py)
                                fillPath.moveTo(px, py)
                            } else {
                                val prevPx = leftPadding + (i - 1) * (chartWidth / (pointsCount - 1))
                                val prevPy = getPixelY(mockPoints[i - 1])
                                // Cubic bezier control points for beautiful layout curve
                                strokePath.cubicTo(
                                    prevPx + (px - prevPx) / 2f, prevPy,
                                    prevPx + (px - prevPx) / 2f, py,
                                    px, py
                                )
                                fillPath.cubicTo(
                                    prevPx + (px - prevPx) / 2f, prevPy,
                                    prevPx + (px - prevPx) / 2f, py,
                                    px, py
                                )
                            }
                            if (i == pointsCount - 1) {
                                fillPath.lineTo(px, height - bottomPadding)
                                fillPath.lineTo(leftPadding, height - bottomPadding)
                                fillPath.close()
                            }
                        }

                        // Demo line rendering
                        drawPath(
                            path = strokePath,
                            color = primaryColor.copy(alpha = 0.2f),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )

                        // Draw mock standard label
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "趋势范例图 (首度记录起算)",
                            style = textStyle.copy(color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            topLeft = Offset(leftPadding + chartWidth / 3f, getPixelY(23f))
                        )

                        // Draw X Axis Demo Labels
                        for (i in 0 until pointsCount) {
                            val px = leftPadding + i * (chartWidth / (pointsCount - 1))
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "期-${pointsCount - i}",
                                style = textStyle,
                                topLeft = Offset(px - 10.dp.toPx(), height - 16.dp.toPx())
                            )
                        }

                    } else {
                        // Real records cycle trajectory
                        val count = cycleTrendData.size
                        val baseSegmentWidth = if (count > 1) chartWidth / (count - 1) else chartWidth

                        val actualPoints = cycleTrendData.mapIndexed { idx, pt ->
                            val px = leftPadding + idx * baseSegmentWidth
                            val py = getPixelY(pt.cycleDays.toFloat())
                            Offset(px, py)
                        }

                        val strokePath = Path()
                        val fillPath = Path()

                        actualPoints.forEachIndexed { idx, point ->
                            // Scale down line path drawing according to animation slider
                            val animatedY = getPixelY(15f) + (point.y - getPixelY(15f)) * animatedProgress.value
                            val currentPos = Offset(point.x, animatedY)

                            if (idx == 0) {
                                strokePath.moveTo(currentPos.x, currentPos.y)
                                fillPath.moveTo(currentPos.x, currentPos.y)
                            } else {
                                val prevPoint = actualPoints[idx - 1]
                                val animatedPrevY = getPixelY(15f) + (prevPoint.y - getPixelY(15f)) * animatedProgress.value
                                val controlX1 = prevPoint.x + (currentPos.x - prevPoint.x) / 3f
                                val controlX2 = prevPoint.x + 2f * (currentPos.x - prevPoint.x) / 3f

                                strokePath.cubicTo(
                                    controlX1, animatedPrevY,
                                    controlX2, currentPos.y,
                                    currentPos.x, currentPos.y
                                )
                                fillPath.cubicTo(
                                    controlX1, animatedPrevY,
                                    controlX2, currentPos.y,
                                    currentPos.x, currentPos.y
                                )
                            }

                            if (idx == count - 1) {
                                fillPath.lineTo(currentPos.x, height - bottomPadding)
                                fillPath.lineTo(actualPoints[0].x, height - bottomPadding)
                                fillPath.close()
                            }
                        }

                        // 1. Draw glowing background gradient below the actual line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )

                        // 2. Draw actual trend line
                        drawPath(
                            path = strokePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            ),
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 3. Draw interactive nodes and values labels
                        actualPoints.forEachIndexed { idx, pt ->
                            val value = cycleTrendData[idx].cycleDays
                            val animatedY = getPixelY(15f) + (pt.y - getPixelY(15f)) * animatedProgress.value

                            // Draw circle outline
                            drawCircle(
                                color = surfaceColor,
                                radius = 7.dp.toPx(),
                                center = Offset(pt.x, animatedY)
                            )
                            drawCircle(
                                color = if (value in 21..35) primaryColor else Color(0xFFEF4444),
                                radius = 4.5.dp.toPx(),
                                center = Offset(pt.x, animatedY)
                            )

                            // Draw Value Text above node
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${value}天",
                                style = textStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (value in 21..35) primaryColor else Color(0xFFEF4444)
                                ),
                                topLeft = Offset(pt.x - 12.dp.toPx(), animatedY - 18.dp.toPx())
                            )

                            // Draw X-axis Labels (StartDate months)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = cycleTrendData[idx].label,
                                style = textStyle.copy(fontWeight = FontWeight.Bold),
                                topLeft = Offset(pt.x - 14.dp.toPx(), height - 16.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Legended info area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFE6F4EA), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("规律舒张带 (21~35 天)", fontSize = 9.sp, color = neutralLabelColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(primaryColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("记录周期节点", fontSize = 9.sp, color = neutralLabelColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Regularity diagnostic feedback component
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (cycleTrendData.size >= 2) primaryColor.copy(alpha = 0.05f)
                        else Color(0xFFFFFBEB)
                    )
                    .border(
                        1.dp,
                        if (cycleTrendData.size >= 2) primaryColor.copy(alpha = 0.15f)
                        else Color(0xFFFDE8E1),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (cycleTrendData.size >= 2) Icons.Default.Info else Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = if (cycleTrendData.size >= 2) primaryColor else Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = regularityMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (cycleTrendData.size >= 2) MaterialTheme.colorScheme.onSurface else Color(0xFF92400E)
                )
            }

            // Summary Analytics boxes if we have more than 2 records
            if (cycleTrendData.size >= 2) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("波动区段", fontSize = 9.sp, color = neutralLabelColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$minCycle - $maxCycle 天",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("平均极差", fontSize = 9.sp, color = neutralLabelColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${maxCycle - minCycle} 天",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (maxCycle - minCycle <= 3) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("规律系数 (CV)", fontSize = 9.sp, color = neutralLabelColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        val cv = (fluctuation / averageCycle) * 100
                        Text(
                            text = String.format("%.1f%%", cv),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cv <= 5.0) Color(0xFF10B981) else Color(0xFF3B82F6)
                        )
                    }
                }
            }
        }
    }
}
