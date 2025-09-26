package top.mizhoubaobei.womenhealth.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import top.mizhoubaobei.womenhealth.databinding.FragmentCalendarBinding
import java.util.Calendar
import java.util.Date

/**
 * 日历式月经记录界面
 */
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendarAdapter: CalendarAdapter
    
    private val calendar = Calendar.getInstance()
    private var currentYear = calendar.get(Calendar.YEAR)
    private var currentMonth = calendar.get(Calendar.MONTH)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        
        setupCalendar()
        setupObservers()
        setupClickListeners()
        
        // 加载当前月份数据
        loadMonthData()
    }
    
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { date, dayInfo ->
            onDateClick(date, dayInfo)
        }
        
        binding.recyclerViewCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = calendarAdapter
        }
        
        updateMonthTitle()
    }
    
    private fun setupObservers() {
        viewModel.monthlyRecords.observe(viewLifecycleOwner) { records ->
            calendarAdapter.updateRecords(records)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.btnPreviousMonth.setOnClickListener {
            navigateMonth(-1)
        }
        
        binding.btnNextMonth.setOnClickListener {
            navigateMonth(1)
        }
        
        binding.btnToday.setOnClickListener {
            goToToday()
        }
    }
    
    private fun navigateMonth(direction: Int) {
        currentMonth += direction
        if (currentMonth < 0) {
            currentMonth = 11
            currentYear--
        } else if (currentMonth > 11) {
            currentMonth = 0
            currentYear++
        }
        
        updateMonthTitle()
        loadMonthData()
        generateCalendarDays()
    }
    
    private fun goToToday() {
        val today = Calendar.getInstance()
        currentYear = today.get(Calendar.YEAR)
        currentMonth = today.get(Calendar.MONTH)
        
        updateMonthTitle()
        loadMonthData()
        generateCalendarDays()
    }
    
    private fun updateMonthTitle() {
        val monthNames = arrayOf(
            "一月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "十二月"
        )
        binding.textViewMonthYear.text = "${currentYear}年 ${monthNames[currentMonth]}"
    }
    
    private fun loadMonthData() {
        viewModel.loadMonthlyRecords(currentYear, currentMonth)
        generateCalendarDays()
    }
    
    private fun generateCalendarDays() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        
        val days = mutableListOf<CalendarDay>()
        val today = Calendar.getInstance()
        
        // 添加上个月的日期（填充空白）
        val prevMonth = if (currentMonth == 0) 11 else currentMonth - 1
        val prevYear = if (currentMonth == 0) currentYear - 1 else currentYear
        calendar.set(prevYear, prevMonth, 1)
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (i in firstDayOfWeek - 1 downTo 0) {
            val day = daysInPrevMonth - i
            calendar.set(prevYear, prevMonth, day)
            days.add(CalendarDay(calendar.time, day, false, false))
        }
        
        // 添加当前月的日期
        for (day in 1..daysInMonth) {
            calendar.set(currentYear, currentMonth, day)
            val isToday = today.get(Calendar.YEAR) == currentYear &&
                         today.get(Calendar.MONTH) == currentMonth &&
                         today.get(Calendar.DAY_OF_MONTH) == day
            days.add(CalendarDay(calendar.time, day, true, isToday))
        }
        
        // 添加下个月的日期（填充剩余空白）
        val nextMonth = if (currentMonth == 11) 0 else currentMonth + 1
        val nextYear = if (currentMonth == 11) currentYear + 1 else currentYear
        val remainingDays = 42 - days.size // 6行 x 7列
        
        for (day in 1..remainingDays) {
            calendar.set(nextYear, nextMonth, day)
            days.add(CalendarDay(calendar.time, day, false, false))
        }
        
        calendarAdapter.updateDays(days)
    }
    
    private fun onDateClick(date: Date, dayInfo: CalendarDayInfo?) {
        // 显示日期详情对话框或跳转到详情页面
        showDateDetailDialog(date, dayInfo)
    }
    
    private fun showDateDetailDialog(date: Date, dayInfo: CalendarDayInfo?) {
        val dialog = DateDetailDialog.newInstance(date, dayInfo)
        dialog.setOnRecordSavedListener { record ->
            // 刷新当前月份数据
            loadMonthData()
            Toast.makeText(context, "记录已保存", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "DateDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}