package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import top.mizhoubaobei.womenhealth.data.repository.CycleAnalysis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun NextPeriodPredictionCard(
    records: List<PeriodRecord>,
    analysis: CycleAnalysis,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val nextPeriodDateText = analysis.nextPeriodDate

    // Parse predicted next period start date
    val nextPeriodLocalDate = try {
        if (nextPeriodDateText.isNotBlank() && nextPeriodDateText != "等待更多记录") {
            LocalDate.parse(nextPeriodDateText, formatter)
        } else {
            LocalDate.now().plusDays(analysis.daysUntilNextPeriod.toLong())
        }
    } catch (e: Exception) {
        LocalDate.now().plusDays(18)
    }

    val displayMonthDay = nextPeriodLocalDate.format(DateTimeFormatter.ofPattern("M月d日"))
    val displayDayOfWeek = nextPeriodLocalDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
    val displayYear = nextPeriodLocalDate.year

    val daysRemaining = analysis.daysUntilNextPeriod

    // Evaluate confidence level based on number of records
    val recordCount = records.size
    val (confidenceLabel, confidenceColor, confidenceDesc) = when {
        recordCount == 0 -> Triple("常规预估", Color(0xFFF59E0B), "建议登记首笔经期以激活高精确推算")
        recordCount < 3 -> Triple("初步推测", Color(0xFF3B82F6), "数据少于3次。记录越多预测越精准哦")
        else -> Triple("高精确推算", Color(0xFF10B981), "基于历史 ${recordCount} 次周期规律智能深度演化")
    }

    // Suggestions list for preparation
    val preparations = remember {
        listOf(
            "备好卫生棉 & 温敷贴" to "🩸 经期储备",
            "减少生冷，温水足浴暖足" to "🪵 温养习惯",
            "关注身体微变（胸胀/情绪）" to "💡 身心觉察"
        )
    }

    var isReminderEnabled by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("next_period_prediction_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Header Row: Title & Confidence Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "下期经期预测核心看板",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "预测精准度分析与趋势追踪",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Confidence Tag Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(confidenceColor.copy(alpha = 0.12f))
                        .border(1.dp, confidenceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confidenceLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Display: Big Countdown Box with Asymmetry design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "预计来潮日",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Big Date Text
                    Text(
                        text = displayMonthDay,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${displayYear}年 ($displayDayOfWeek) 开始",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Countdown Column Highlight
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (daysRemaining <= 0) "预测生理日" else "还有",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (daysRemaining <= 0) "就在今日 🩸" else "${daysRemaining}天",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info bar showing algorithm confidence details description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = confidenceDesc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sub stats indicators (Dual blocks layout pattern)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avg Cycle length
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "平均生理周期",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${analysis.avgCycleLength} 天",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Avg Period length
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "平均行经天数",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${analysis.avgPeriodLength} 天",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // A visual timeline dotted progress mapping (Transition of Cycle phase to upcoming Menstruation)
            Text(
                text = "📊 周期转段进度图",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            CycleTimelineProgressBar(
                currentPhase = analysis.currentPhase,
                daysRemaining = daysRemaining,
                avgCycle = analysis.avgCycleLength
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic warm checklist recommendations before next period
            Text(
                text = "📝 贴心备忘与暖阳指南",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                preparations.forEach { (text, tag) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Actions inside predicted card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Feature A: Enable prediction alarms toggle
                Button(
                    onClick = {
                        isReminderEnabled = !isReminderEnabled
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("enable_prediction_reminders_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isReminderEnabled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isReminderEnabled) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isReminderEnabled) "来潮提醒已开" else "开启来温预警",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Feature B: Export period prediction to calendar simulator
                OutlinedButton(
                    onClick = {
                        showExportToast = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("export_prediction_to_cal_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "同步日历",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = showExportToast,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = "🎉 已成功将下期经期预测序列 ($displayMonthDay ~ ${nextPeriodLocalDate.plusDays(analysis.avgPeriodLength.toLong() - 1).format(DateTimeFormatter.ofPattern("M月d日"))}) 模拟同步至您本地账户日历！",
                    color = Color(0xFF00796B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2F1))
                        .padding(10.dp),
                    textAlign = TextAlign.Center
                )

                // auto dismissal of mock toast
                LaunchedEffect(showExportToast) {
                    kotlinx.coroutines.delay(3500)
                    showExportToast = false
                }
            }
        }
    }
}

@Composable
fun CycleTimelineProgressBar(
    currentPhase: String,
    daysRemaining: Int,
    avgCycle: Int
) {
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    // Calculate normalized progress percentage to upcoming period.
    // 0 is start of cycle, 1 is the upcoming prediction day
    val progress = remember(daysRemaining, avgCycle) {
        val calculatedPercent = (avgCycle - daysRemaining.coerceAtLeast(0)).toFloat() / avgCycle.coerceAtLeast(21)
        calculatedPercent.coerceIn(0.05f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前: $currentPhase",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
            )
            Text(
                text = if (daysRemaining <= 0) "即将到达临界点 ⏳" else "距月经期还剩 ${daysRemaining} 天",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Canvas Timeline line with node markers and dashed paths
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val width = size.width
            val centerY = size.height / 2f

            // 1. Draw base trajectory path
            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 6.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )

            // 2. Draw elapsed progress path
            drawLine(
                color = progressColor,
                start = Offset(0f, centerY),
                end = Offset(width * progress, centerY),
                strokeWidth = 6.dp.toPx()
            )

            // 3. Draw phase milestone dots: Start of Cycle (Day 1), Ovulation/Midpoint, Next Period (End)
            // Node A: Day 1
            drawCircle(
                color = if (progress >= 0.05f) progressColor else trackColor,
                radius = 5.dp.toPx(),
                center = Offset(0f + 5.dp.toPx(), centerY)
            )

            // Node B: Ovulation Day (Roughly 50% through the line)
            drawCircle(
                color = if (progress >= 0.5f) progressColor else trackColor,
                radius = 5.dp.toPx(),
                center = Offset(width / 2f, centerY)
            )

            // Node C: Predicted Period Start (100% boundary)
            drawCircle(
                color = if (progress >= 0.95f) progressColor else trackColor,
                radius = 6.dp.toPx(),
                center = Offset(width - 6.dp.toPx(), centerY)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day 1 (月初潮)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("推演排卵期", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("预估下期行经", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}
