package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.data.repository.CycleAnalysis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun FuturePredictionsCard(
    analysis: CycleAnalysis,
    modifier: Modifier = Modifier
) {
    val avgCycle = if (analysis.avgCycleLength in 20..45) analysis.avgCycleLength else 28
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val userFriendlyFormatter = DateTimeFormatter.ofPattern("M月d日")

    // Try parsing the next period start date
    val baseNextPeriodDate = try {
        if (analysis.nextPeriodDate.isNotBlank() && analysis.nextPeriodDate != "等待更多记录") {
            LocalDate.parse(analysis.nextPeriodDate)
        } else {
            LocalDate.now().plusDays(18) // standard default if base is empty
        }
    } catch (e: Exception) {
        LocalDate.now().plusDays(18)
    }

    // Generate next 3 prediction nodes
    val predictions = remember(baseNextPeriodDate, avgCycle) {
        List(3) { index ->
            val pStart = baseNextPeriodDate.plusDays((index * avgCycle).toLong())
            val pEnd = pStart.plusDays((analysis.avgPeriodLength.coerceAtLeast(3).coerceAtMost(10)).toLong() - 1)
            
            // Fertility window: roughly 14 days before start is ovulation, and fertile is ovulation - 5 to ovulation + 4
            val ovulation = pStart.minusDays(14)
            val fertileStart = ovulation.minusDays(5)
            val fertileEnd = ovulation.plusDays(4)

            PredictionNode(
                monthLabel = "${pStart.monthValue}月经期预测",
                periodRange = "${pStart.format(userFriendlyFormatter)} ~ ${pEnd.format(userFriendlyFormatter)}",
                fertileRange = "${fertileStart.format(userFriendlyFormatter)} ~ ${fertileEnd.format(userFriendlyFormatter)}",
                daysCountdown = ChronoUnit.DAYS.between(LocalDate.now(), pStart).toInt()
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("future_predictions_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "未来三月生理时程流向预测",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "基于高敏算法预测您周期的动态演变与温养时钟",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Prediction Timeline
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                predictions.forEachIndexed { idx, pred ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress bullet/ordinal
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (idx == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M${idx + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == 0) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Prediction data details
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = pred.monthLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🩸 预估行经期：",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = pred.periodRange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🌸 易孕受孕期：",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = pred.fertileRange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF00796B)
                                )
                            }
                        }

                        // Right countdown badge
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (pred.daysCountdown < 0) "已进行" else "还有",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = if (pred.daysCountdown < 0) "N/A" else "${pred.daysCountdown}天",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (idx == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prediction warm advice line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "数据越丰富预测精度越高。请按时登记，让 LuminCore 更好呵护您哦！",
                        fontSize = 10.sp,
                        color = Color(0xFF004D40),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class PredictionNode(
    val monthLabel: String,
    val periodRange: String,
    val fertileRange: String,
    val daysCountdown: Int
)
