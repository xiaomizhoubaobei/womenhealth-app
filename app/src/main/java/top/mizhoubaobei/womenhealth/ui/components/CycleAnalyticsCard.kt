package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CycleAnalyticsCard(
    records: List<PeriodRecord>,
    modifier: Modifier = Modifier
) {
    // 1. Calculate stats from real record listings
    val totalRecordsCount = records.size
    val averagePeriodDuration = if (totalRecordsCount > 0) {
        val calculated = records.mapNotNull { record ->
            if (record.endDate != null) {
                try {
                    val s = LocalDate.parse(record.startDate)
                    val e = LocalDate.parse(record.endDate)
                    ChronoUnit.DAYS.between(s, e).toInt() + 1
                } catch (ex: Exception) {
                    null
                }
            } else null
        }
        if (calculated.isNotEmpty()) calculated.average() else 5.2
    } else 5.2

    // Calculate Cycle intervals (Diff between successive period start dates)
    val cycleLengthSamples = mutableListOf<Long>()
    if (records.size >= 2) {
        val sortedRecords = records.sortedBy { it.startDate } // oldest first
        for (i in 0 until sortedRecords.size - 1) {
            try {
                val currentStart = LocalDate.parse(sortedRecords[i].startDate)
                val nextStart = LocalDate.parse(sortedRecords[i + 1].startDate)
                cycleLengthSamples.add(ChronoUnit.DAYS.between(currentStart, nextStart))
            } catch (ex: Exception) {
                // ignore format issue
            }
        }
    }
    val averageCycleDuration = if (cycleLengthSamples.isNotEmpty()) {
        cycleLengthSamples.average()
    } else 28.0

    // Stability Meter Rating
    val (stabilityScore, stabilityLabel, stabilityColor) = when {
        totalRecordsCount < 2 -> Triple(100, "记录累积中", Color(0xFF94A3B8))
        cycleLengthSamples.isEmpty() -> Triple(100, "首期数据规律", Color(0xFF64748B))
        else -> {
            val mean = averageCycleDuration
            val variance = cycleLengthSamples.map { (it - mean) * (it - mean) }.sum() / cycleLengthSamples.size
            val sd = kotlin.math.sqrt(variance)
            if (sd <= 1.5) {
                Triple(98, "钟摆般极度规律 ✨", Color(0xFF10B981))
            } else if (sd <= 3.8) {
                Triple(85, "正常平稳波动 🌟", Color(0xFF3B82F6))
            } else {
                Triple(55, "波动偏大/注意作息 ⚠️", Color(0xFFF59E0B))
            }
        }
    }

    // 2. Extract and Aggregate Symptoms Frequency
    val symptomTally = remember(records) {
        val tally = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.symptoms.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "无症状" }
                .forEach { s ->
                    tally[s] = tally.getOrDefault(s, 0) + 1
                }
        }
        tally.entries.sortedByDescending { it.value }.take(3)
    }

    // 3. Extract and Aggregate Mood Frequency
    val moodTally = remember(records) {
        val tally = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.mood.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "平静" }
                .forEach { m ->
                    tally[m] = tally.getOrDefault(m, 0) + 1
                }
        }
        tally.entries.sortedByDescending { it.value }.take(3)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cycle_analytics_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Widget Title Header Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LuminCore 黄金生命周期统计",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Grid Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total tracked card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text("累计记录数", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "${totalRecordsCount} 次",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "掌握身体节奏",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // Average period length
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text("平均行经天数", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = String.format("%.1f 天", averagePeriodDuration),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "安全合规范围: 3-7天",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                // Average cycle length
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text("平均周期跨度", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = String.format("%.1f 天", averageCycleDuration),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "教科书值: 28.0天",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cycle Stability rating indicator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = stabilityColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚕️ 生理周期规律度评分",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stabilityLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = stabilityColor
                    )
                }
            }

            // 4. Symptoms frequency rank
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Symptoms
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🌡️ 频发不适排位",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (symptomTally.isEmpty()) {
                        Text(
                            text = "无显著症状偏向 ✨",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            symptomTally.forEachIndexed { sIndex, entry ->
                                val percent = if (totalRecordsCount > 0) (entry.value.toFloat() / totalRecordsCount) else 0f
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.key, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text("${entry.value}次", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = percent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFFF48FB1),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Top Mood fluctuation
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎭 波动情绪偏好",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (moodTally.isEmpty()) {
                        Text(
                            text = "情绪非常宁静平静 🧘",
                            fontSize = 11.sp,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            moodTally.forEach { entry ->
                                val percent = if (totalRecordsCount > 0) (entry.value.toFloat() / totalRecordsCount) else 0f
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.key, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text("${entry.value}次", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = percent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFF818CF8),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
