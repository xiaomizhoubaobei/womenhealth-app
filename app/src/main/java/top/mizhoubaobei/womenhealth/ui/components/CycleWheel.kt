package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.data.repository.CycleAnalysis
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CycleWheel(
    analysis: CycleAnalysis,
    modifier: Modifier = Modifier
) {
    val totalDays = analysis.avgCycleLength.toFloat()
    val currentDay = analysis.currentCycleDay.toFloat().coerceAtMost(totalDays)
    val periodLength = analysis.avgPeriodLength.toFloat()

    // Animating the indicator bead or arcs
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Parse Ovulation & Fertile days to cycle day indexes
    // Ovulation is usually 14 days before next start date.
    // In our repository: predictedOvulation = nextPeriodDate - 14.
    // In cycle scale, ovulation is approximately at index: (avgCycleLength - 14) + 1
    val ovulationDayIdx = (analysis.avgCycleLength - 14).toFloat().coerceAtLeast(1f)
    val fertileStartIdx = (ovulationDayIdx - 5f).coerceAtLeast(1f)
    val fertileEndIdx = (ovulationDayIdx + 1f).coerceAtMost(totalDays)

    // Color definitions for segments
    val menstrualColor = Color(0xFFF48FB1)  // Soft Rose
    val follicularColor = Color(0xFFCE93D8) // Soft Purple
    val fertileColor = Color(0xFF80CBC4)    // Soft Mint
    val lutealColor = Color(0xFFFFCC80)     // Soft Peach
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(12.dp)
            .testTag("cycle_wheel_container"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Glow Circle Background matching current phase colors
        val activeBgBrush = when (analysis.currentPhase) {
            "月经期" -> Brush.radialGradient(listOf(menstrualColor.copy(alpha = 0.15f), Color.Transparent))
            "易孕期", "排卵期" -> Brush.radialGradient(listOf(fertileColor.copy(alpha = 0.15f), Color.Transparent))
            "黄体期" -> Brush.radialGradient(listOf(lutealColor.copy(alpha = 0.15f), Color.Transparent))
            else -> Brush.radialGradient(listOf(follicularColor.copy(alpha = 0.15f), Color.Transparent))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(activeBgBrush)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val rect = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            // 1. Draw base track background ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokeWidth)
            )

            // Calculations for segment degrees
            // Every segment: 1 day = (360 / totalDays) degrees
            val degPerDay = 360f / totalDays

            // Segment 1: Menstrual Phase (Day 1 to periodLength)
            val menstrualSweep = periodLength * degPerDay
            drawArc(
                color = menstrualColor,
                startAngle = -90f,
                sweepAngle = menstrualSweep,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 2: Follicular Phase (from end of period to fertile start)
            val follicularSweep = (fertileStartIdx - periodLength).coerceAtLeast(0f) * degPerDay
            if (follicularSweep > 0f) {
                drawArc(
                    color = follicularColor,
                    startAngle = -90f + menstrualSweep,
                    sweepAngle = follicularSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = rect,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Segment 3: Fertile Window (fertile start index to fertile end index)
            val fertileStartAngle = -90f + (fertileStartIdx * degPerDay)
            val fertileSweep = (fertileEndIdx - fertileStartIdx) * degPerDay
            drawArc(
                color = fertileColor,
                startAngle = fertileStartAngle,
                sweepAngle = fertileSweep,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Segment 4: Luteal Phase (fertile end to cycle end)
            val lutealStartAngle = -90f + (fertileEndIdx * degPerDay)
            val lutealSweep = (totalDays - fertileEndIdx) * degPerDay
            if (lutealSweep > 0f) {
                drawArc(
                    color = lutealColor,
                    startAngle = lutealStartAngle,
                    sweepAngle = lutealSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = rect,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 2. Draw Current Day Progress Bead (Indicator Dot)
            val progressAngleRad = Math.toRadians(((currentDay / totalDays) * 360f - 90f).toDouble())
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val dotX = center.x + radius * cos(progressAngleRad).toFloat()
            val dotY = center.y + radius * sin(progressAngleRad).toFloat()

            val activeColor = when (analysis.currentPhase) {
                "月经期" -> menstrualColor
                "易孕期", "排卵期" -> fertileColor
                "黄体期" -> lutealColor
                else -> follicularColor
            }

            // Glow backing
            drawCircle(
                color = activeColor.copy(alpha = pulseAlpha),
                radius = 16.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            // Solid center bead
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = activeColor,
                radius = 5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }

        // 3. Central HUD Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "第 ${analysis.currentCycleDay} 天",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = when (analysis.currentPhase) {
                    "月经期" -> menstrualColor
                    "易孕期", "排卵期" -> fertileColor.copy(green = 0.6f)
                    "黄体期" -> lutealColor.copy(red = 0.9f)
                    else -> follicularColor
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = when (analysis.currentPhase) {
                            "月经期" -> menstrualColor.copy(alpha = 0.15f)
                            "易孕期", "排卵期" -> fertileColor.copy(alpha = 0.15f)
                            "黄体期" -> lutealColor.copy(alpha = 0.15f)
                            else -> follicularColor.copy(alpha = 0.15f)
                        },
                        shape = CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = analysis.currentPhase,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (analysis.currentPhase) {
                        "月经期" -> menstrualColor
                        "易孕期", "排卵期" -> fertileColor.copy(green = 0.7f)
                        "黄体期" -> lutealColor.copy(red = 0.8f)
                        else -> follicularColor
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (analysis.daysUntilNextPeriod > 0) {
                    "距离下次经期\n约 ${analysis.daysUntilNextPeriod} 天"
                } else if (analysis.daysUntilNextPeriod == 0) {
                    "今天可能迎来生理期"
                } else {
                    "生理期延迟\n${-analysis.daysUntilNextPeriod} 天"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
