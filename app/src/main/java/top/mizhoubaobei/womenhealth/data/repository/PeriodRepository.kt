package top.mizhoubaobei.womenhealth.data.repository

import top.mizhoubaobei.womenhealth.data.database.PeriodDao
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class CycleAnalysis(
    val currentCycleDay: Int,            // Current day in the cycle, e.g., Day 8
    val currentPhase: String,            // "月经期", "卵泡期", "排卵期" (or "易孕期"), "黄体期"
    val phaseDescription: String,        // Human readable phase advice
    val daysUntilNextPeriod: Int,        // Days left until next period
    val nextPeriodDate: String,          // "yyyy-MM-dd"
    val ovulationDate: String,           // "yyyy-MM-dd"
    val fertileWindowStart: String,      // "yyyy-MM-dd"
    val fertileWindowEnd: String,        // "yyyy-MM-dd"
    val avgCycleLength: Int,             // computed average, default 28
    val avgPeriodLength: Int,            // computed average, default 5
    val isCurrentlyInPeriod: Boolean     // Whether the user is currently bleeding
)

class PeriodRepository(private val periodDao: PeriodDao) {

    val allRecords: Flow<List<PeriodRecord>> = periodDao.getAllRecordsFlow()

    suspend fun getAllRecordsDirect(): List<PeriodRecord> = periodDao.getAllRecordsDirect()

    suspend fun insert(record: PeriodRecord) = periodDao.insert(record)

    suspend fun update(record: PeriodRecord) = periodDao.update(record)

    suspend fun delete(record: PeriodRecord) = periodDao.delete(record)

    fun calculateCycleAnalysis(records: List<PeriodRecord>, today: LocalDate = LocalDate.now()): CycleAnalysis {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // 1. If NO records, provide default state
        if (records.isEmpty()) {
            val defaultNextPeriod = today.plusDays(14) // default 14 days later
            val defaultOvulation = defaultNextPeriod.minusDays(14)
            return CycleAnalysis(
                currentCycleDay = 1,
                currentPhase = "卵泡期",
                phaseDescription = "开始新周期的旅程，适合规划和学习。",
                daysUntilNextPeriod = 14,
                nextPeriodDate = defaultNextPeriod.format(formatter),
                ovulationDate = defaultOvulation.format(formatter),
                fertileWindowStart = defaultOvulation.minusDays(5).format(formatter),
                fertileWindowEnd = defaultOvulation.plusDays(1).format(formatter),
                avgCycleLength = 28,
                avgPeriodLength = 5,
                isCurrentlyInPeriod = false
            )
        }

        // Records are sorted DESC by startDate (records[0] is latest)
        val sortedRecords = records.sortedByDescending { LocalDate.parse(it.startDate, formatter) }
        val latestRecord = sortedRecords[0]
        val latestStart = LocalDate.parse(latestRecord.startDate, formatter)

        // 2. Compute Average Period Length (Duration of bleeding)
        var totalPeriodDays = 0L
        var validPeriodCount = 0
        for (record in sortedRecords) {
            if (record.endDate != null) {
                val start = LocalDate.parse(record.startDate, formatter)
                val end = LocalDate.parse(record.endDate, formatter)
                if (!end.isBefore(start)) {
                    val duration = ChronoUnit.DAYS.between(start, end) + 1
                    totalPeriodDays += duration
                    validPeriodCount++
                }
            }
        }
        val avgPeriodLength = if (validPeriodCount > 0) {
            (totalPeriodDays.toFloat() / validPeriodCount).toInt().coerceIn(3, 10)
        } else {
            5
        }

        // 3. Compute Average Cycle Length (Interval between period starts)
        var totalCycleDays = 0L
        var validCycleCount = 0
        for (i in 0 until sortedRecords.size - 1) {
            val currentStart = LocalDate.parse(sortedRecords[i].startDate, formatter)
            val nextStart = LocalDate.parse(sortedRecords[i + 1].startDate, formatter)
            val diff = ChronoUnit.DAYS.between(nextStart, currentStart)
            if (diff in 15..50) { // filter out physiological anomalies
                totalCycleDays += diff
                validCycleCount++
            }
        }
        val avgCycleLength = if (validCycleCount > 0) {
            (totalCycleDays.toFloat() / validCycleCount).toInt().coerceIn(21, 45)
        } else {
            28
        }

        // 4. Predict Next Period Date
        val predictedNextPeriod = latestStart.plusDays(avgCycleLength.toLong())
        val daysUntilNextPeriod = ChronoUnit.DAYS.between(today, predictedNextPeriod).toInt()

        // 5. Predict Ovulation and Fertile Window (14 days before next period start)
        val predictedOvulation = predictedNextPeriod.minusDays(14)
        val fertileStart = predictedOvulation.minusDays(5)
        val fertileEnd = predictedOvulation.plusDays(1)

        // 6. Current Cycle Day representation
        val currentCycleDay = (ChronoUnit.DAYS.between(latestStart, today).toInt() + 1).coerceAtLeast(1)

        // 7. Determine state and phase
        // Is user currently bleeding (in period)?
        val isCurrentlyInPeriod = if (latestRecord.endDate == null) {
            // No end date logged, count in period if today is within average period range from start
            currentCycleDay <= avgPeriodLength
        } else {
            // End date logged, check if today is between start and end date
            val endDate = LocalDate.parse(latestRecord.endDate, formatter)
            !today.isBefore(latestStart) && !today.isAfter(endDate)
        }

        val currentPhase: String
        val phaseDescription: String
        
        when {
            isCurrentlyInPeriod -> {
                currentPhase = "月经期"
                phaseDescription = "卵巢处于平淡期。建议注意腹部保暖，避免剧烈运动和偏冷辛辣食物，好好呵护自己。"
            }
            !today.isBefore(fertileStart) && !today.isAfter(fertileEnd) -> {
                currentPhase = "易孕期"
                phaseDescription = "卵巢正处于排卵期，雌孕激素高涨。此时精力最旺盛、皮肤红润。如无生育计划请做好避孕防护措施。"
            }
            today.isAfter(predictedOvulation.plusDays(1)) -> {
                currentPhase = "黄体期"
                phaseDescription = "排卵后体内孕激素主导。可能出现心烦气躁、皮肤油脂多、容易疲倦或经前综合症（PMS）。适合冥想放松。"
            }
            else -> {
                currentPhase = "卵泡期"
                phaseDescription = "月经刚结束。雌激素逐渐上升，新陈代谢旺盛。是减肥和制定生活计划的黄金时期。"
            }
        }

        return CycleAnalysis(
            currentCycleDay = currentCycleDay,
            currentPhase = currentPhase,
            phaseDescription = phaseDescription,
            daysUntilNextPeriod = daysUntilNextPeriod,
            nextPeriodDate = predictedNextPeriod.format(formatter),
            ovulationDate = predictedOvulation.format(formatter),
            fertileWindowStart = fertileStart.format(formatter),
            fertileWindowEnd = fertileEnd.format(formatter),
            avgCycleLength = avgCycleLength,
            avgPeriodLength = avgPeriodLength,
            isCurrentlyInPeriod = isCurrentlyInPeriod
        )
    }
}
