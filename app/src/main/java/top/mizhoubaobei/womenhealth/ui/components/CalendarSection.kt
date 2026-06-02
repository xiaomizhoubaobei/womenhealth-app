package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import top.mizhoubaobei.womenhealth.data.repository.CycleAnalysis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarSection(
    records: List<PeriodRecord>,
    analysis: CycleAnalysis,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Standard variables
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    var currentMonthState by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val daysInMonth = currentMonthState.lengthOfMonth()
    
    // Day of week offset for the 1st of the month (1 = Monday, 7 = Sunday)
    // We want Sunday as 0, Monday as 1, etc., up to Saturday as 6.
    val firstDayOfWeekVal = currentMonthState.dayOfWeek.value // 1 (Mon) - 7 (Sun)
    val startOffset = if (firstDayOfWeekVal == 7) 0 else firstDayOfWeekVal

    val monthName = currentMonthState.month.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    val yearName = currentMonthState.year

    // Color indicators
    val loggedColor = Color(0xFFF48FB1)      // Menstrual block
    val predictedColor = Color(0xFFFFCC80)   // Predicted block
    val ovulationColor = Color(0xFFCE93D8)   // Ovulation dot
    val selectedColor = MaterialTheme.colorScheme.primary

    var focusedDate by remember { mutableStateOf<LocalDate?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Selector HeaderRow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$yearName 年 $monthName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { currentMonthState = currentMonthState.minusMonths(1) },
                        modifier = Modifier.testTag("prev_month_btn")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                    }
                    IconButton(
                        onClick = { currentMonthState = currentMonthState.plusMonths(1) },
                        modifier = Modifier.testTag("next_month_btn")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days of the Week Title Header
            val daysOfWeekHeader = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeekHeader.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid Calculations
            val totalWeeksInGrid = ((daysInMonth + startOffset) / 7.0).let { kotlin.math.ceil(it).toInt() }
            
            Column {
                for (weekIndex in 0 until totalWeeksInGrid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayIndex in 0..6) {
                            val absoluteDayCounter = weekIndex * 7 + dayIndex - startOffset + 1
                            if (absoluteDayCounter in 1..daysInMonth) {
                                val cellDate = currentMonthState.withDayOfMonth(absoluteDayCounter)
                                val cellDateStr = cellDate.format(formatter)

                                 // 1. Check if dates fall in actual logged periods
                                var isLoggedPeriod = false
                                var activeLog: PeriodRecord? = null
                                for (record in records) {
                                    try {
                                        val start = LocalDate.parse(record.startDate, formatter)
                                        val end = if (!record.endDate.isNullOrBlank()) {
                                            LocalDate.parse(record.endDate, formatter)
                                        } else {
                                            start.plusDays(analysis.avgPeriodLength.toLong() - 1)
                                        }
                                        if (!cellDate.isBefore(start) && !cellDate.isAfter(end)) {
                                            isLoggedPeriod = true
                                            activeLog = record
                                            break
                                        }
                                    } catch (e: Exception) {
                                        // Ignore parsing error for corrupted entries
                                    }
                                }

                                // 2. Check if dates fall in predicted period window
                                val isPredictedPeriod = try {
                                    val predStart = LocalDate.parse(analysis.nextPeriodDate, formatter)
                                    val predEnd = predStart.plusDays(analysis.avgPeriodLength.toLong() - 1)
                                    !cellDate.isBefore(predStart) && !cellDate.isAfter(predEnd)
                                } catch (e: Exception) {
                                    false
                                }

                                // 3. Check if date is ovulation date
                                val isOvulation = cellDateStr == analysis.ovulationDate

                                // 4. Check if date falls in Fertile Window
                                val isFertile = try {
                                    val fStart = LocalDate.parse(analysis.fertileWindowStart, formatter)
                                    val fEnd = LocalDate.parse(analysis.fertileWindowEnd, formatter)
                                    !cellDate.isBefore(fStart) && !cellDate.isAfter(fEnd)
                                } catch (e: Exception) {
                                    false
                                }

                                // Layout representation
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = when {
                                                isLoggedPeriod -> loggedColor.copy(alpha = 0.85f)
                                                isPredictedPeriod -> predictedColor.copy(alpha = 0.25f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (focusedDate == cellDate) 2.dp else 0.dp,
                                            color = if (focusedDate == cellDate) selectedColor else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            focusedDate = cellDate
                                            onDateSelected(cellDate)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = absoluteDayCounter.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isLoggedPeriod || isPredictedPeriod || cellDate == LocalDate.now()) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isLoggedPeriod -> Color.White
                                                cellDate == LocalDate.now() -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        // Dots for Ovulation / Fertile states below the number
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.height(4.dp)
                                        ) {
                                            if (isOvulation) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(ovulationColor, CircleShape)
                                                )
                                            } else if (isFertile && !isLoggedPeriod) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(ovulationColor.copy(alpha = 0.6f), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Focused Day Log Info Card
            focusedDate?.let { date ->
                val dateStr = date.format(formatter)
                val logForDay = records.find { rec ->
                    try {
                        val start = LocalDate.parse(rec.startDate, formatter)
                        val end = if (!rec.endDate.isNullOrBlank()) {
                            LocalDate.parse(rec.endDate, formatter)
                        } else {
                            start.plusDays(analysis.avgPeriodLength.toLong() - 1)
                        }
                        !date.isBefore(start) && !date.isAfter(end)
                    } catch (e: Exception) {
                        false
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📅 ${date.year}年${date.monthValue}月${date.dayOfMonth}日",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (logForDay != null) {
                                Badge(containerColor = loggedColor) {
                                    Text("有经期记录", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (logForDay != null) {
                            Text("🩸 经量大小：${logForDay.flow}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            if (logForDay.symptoms.isNotBlank() && logForDay.symptoms != "无症状") {
                                Text("🌡️ 伴随症状：${logForDay.symptoms}", fontSize = 12.sp)
                            }
                            if (logForDay.mood.isNotBlank() && logForDay.mood != "平静") {
                                Text("💖 情绪状态：${logForDay.mood}", fontSize = 12.sp)
                            }
                            if (logForDay.notes.isNotBlank()) {
                                Text("📝 备注记事：${logForDay.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        } else {
                            // Check if predicted
                            val isPredictionDay = try {
                                val predStart = LocalDate.parse(analysis.nextPeriodDate, formatter)
                                val predEnd = predStart.plusDays(analysis.avgPeriodLength.toLong() - 1)
                                !date.isBefore(predStart) && !date.isAfter(predEnd)
                            } catch (e: Exception) {
                                false
                            }
                            val isOvday = dateStr == analysis.ovulationDate

                            when {
                                isPredictionDay -> {
                                    Text("🔮 预测：此天处于预测的生理行经期内部。建议随身备齐防护，注意保暖预防着凉。", fontSize = 12.sp, color = predictedColor.copy(red = 0.8f))
                                }
                                isOvday -> {
                                    Text("✨ 预测：此天为预测的排卵日。身体在此周期生育力最高，荷尔蒙丰沛。", fontSize = 12.sp, color = ovulationColor)
                                }
                                else -> {
                                    Text("🌿 暂无记录：点击页面底部【登记经期】即可在指定日期记录属于您的生理指标哟。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // Legend indicators
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Period Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(loggedColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("经期", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                // Predicted Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(predictedColor.copy(alpha = 0.4f), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("预测期", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                // Ovulation/Fertile Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(ovulationColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("排卵/易孕", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}
