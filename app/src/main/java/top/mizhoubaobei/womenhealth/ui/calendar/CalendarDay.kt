package top.mizhoubaobei.womenhealth.ui.calendar

import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import java.util.Date

/**
 * 日历日期数据模型
 */
data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val dayInfo: CalendarDayInfo? = null
)

/**
 * 日历日期信息
 */
data class CalendarDayInfo(
    val isPeriodDay: Boolean = false,
    val isPredictedPeriod: Boolean = false,
    val isOvulationDay: Boolean = false,
    val flowLevel: Int? = null,
    val hasSymptoms: Boolean = false,
    val notes: String? = null,
    val record: MenstrualRecord? = null
)
