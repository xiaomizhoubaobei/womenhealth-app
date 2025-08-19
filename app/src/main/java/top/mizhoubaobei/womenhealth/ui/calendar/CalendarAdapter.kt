package top.mizhoubaobei.womenhealth.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.mizhoubaobei.womenhealth.R
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.databinding.ItemCalendarDayBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * 日历适配器
 */
class CalendarAdapter(
    private val onDateClick: (Date, CalendarDayInfo?) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    
    private var days = listOf<CalendarDay>()
    private var records = listOf<MenstrualRecord>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
    
    fun updateRecords(newRecords: List<MenstrualRecord>) {
        records = newRecords
        // 重新计算每天的信息
        updateDaysWithRecords()
        notifyDataSetChanged()
    }
    
    private fun updateDaysWithRecords() {
        val updatedDays = days.map { day ->
            val dayInfo = calculateDayInfo(day.date)
            day.copy(
                dayInfo = dayInfo,
                isToday = isToday(day.date)
            )
        }
        days = updatedDays
    }
    
    private fun calculateDayInfo(date: Date): CalendarDayInfo? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayStart = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dayEnd = calendar.time
        
        // 查找当天的记录
        val dayRecord = records.find { record ->
            record.startDate >= dayStart && record.startDate < dayEnd ||
            (record.endDate != null && record.endDate >= dayStart && record.startDate < dayEnd)
        }
        
        if (dayRecord != null) {
            // 判断是否在经期内
            val isPeriodDay = if (dayRecord.endDate != null) {
                date >= dayRecord.startDate && date <= dayRecord.endDate
            } else {
                // 如果没有结束日期，只有开始日期当天算经期
                dateFormat.format(date) == dateFormat.format(dayRecord.startDate)
            }
            
            return CalendarDayInfo(
                isPeriodDay = isPeriodDay,
                flowLevel = if (isPeriodDay) dayRecord.flowLevel else null,
                hasSymptoms = !dayRecord.symptoms.isNullOrEmpty(),
                record = dayRecord
            )
        }
        
        // 预测逻辑（简单实现）
        val isPredicted = predictPeriodDay(date)
        val isOvulation = predictOvulationDay(date)
        
        return if (isPredicted || isOvulation) {
            CalendarDayInfo(
                isPredictedPeriod = isPredicted,
                isOvulationDay = isOvulation
            )
        } else null
    }
    
    private fun predictPeriodDay(date: Date): Boolean {
        // 简单的预测逻辑：基于最近的记录预测
        if (records.isEmpty()) return false
        
        val latestRecord = records.maxByOrNull { it.startDate }
        latestRecord?.let { record ->
            val daysSinceLastPeriod = ((date.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            // 假设平均周期28天，预测下次经期
            return daysSinceLastPeriod in 26..30
        }
        return false
    }
    
    private fun predictOvulationDay(date: Date): Boolean {
        // 简单的排卵期预测：经期开始后14天左右
        if (records.isEmpty()) return false
        
        val latestRecord = records.maxByOrNull { it.startDate }
        latestRecord?.let { record ->
            val daysSinceLastPeriod = ((date.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            // 排卵期通常在经期开始后12-16天
            return daysSinceLastPeriod in 12..16
        }
        return false
    }
    
    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance()
        checkDate.time = date
        
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }
    
    override fun getItemCount(): Int = days.size
    
    inner class CalendarViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(day: CalendarDay) {
            binding.textViewDay.text = day.dayOfMonth.toString()
            
            // 设置文字颜色
            when {
                !day.isCurrentMonth -> {
                    binding.textViewDay.setTextColor(Color.GRAY)
                }
                day.isToday -> {
                    binding.textViewDay.setTextColor(Color.WHITE)
                }
                else -> {
                    binding.textViewDay.setTextColor(Color.BLACK)
                }
            }
            
            // 设置背景和指示器
            setupDayBackground(day)
            setupIndicators(day.dayInfo)
            
            // 设置点击事件
            binding.root.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDateClick(day.date, day.dayInfo)
                }
            }
        }
        
        private fun setupDayBackground(day: CalendarDay) {
            val context = binding.root.context
            
            when {
                day.isToday -> {
                    binding.root.setBackgroundResource(R.drawable.bg_calendar_today)
                }
                day.dayInfo?.isPeriodDay == true -> {
                    binding.root.setBackgroundResource(R.drawable.bg_calendar_period)
                }
                day.dayInfo?.isPredictedPeriod == true -> {
                    binding.root.setBackgroundResource(R.drawable.bg_calendar_predicted)
                }
                day.dayInfo?.isOvulationDay == true -> {
                    binding.root.setBackgroundResource(R.drawable.bg_calendar_ovulation)
                }
                else -> {
                    binding.root.background = null
                }
            }
        }
        
        private fun setupIndicators(dayInfo: CalendarDayInfo?) {
            // 流量指示器
            binding.viewFlowIndicator.visibility = if (dayInfo?.flowLevel != null) {
                val color = when (dayInfo.flowLevel) {
                    1 -> Color.parseColor("#FFE0E0") // 轻微 - 浅红
                    2 -> Color.parseColor("#FF9999") // 正常 - 中红
                    3 -> Color.parseColor("#FF6666") // 较多 - 深红
                    4 -> Color.parseColor("#FF3333") // 很多 - 很深红
                    else -> Color.TRANSPARENT
                }
                binding.viewFlowIndicator.setBackgroundColor(color)
                View.VISIBLE
            } else {
                View.GONE
            }
            
            // 症状指示器
            binding.viewSymptomIndicator.visibility = if (dayInfo?.hasSymptoms == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}