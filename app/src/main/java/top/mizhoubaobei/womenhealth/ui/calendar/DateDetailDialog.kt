package top.mizhoubaobei.womenhealth.ui.calendar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import top.mizhoubaobei.womenhealth.R
import top.mizhoubaobei.womenhealth.data.FlowLevel
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.data.MenstrualSymptom
import top.mizhoubaobei.womenhealth.databinding.DialogDateDetailBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期详情对话框
 */
class DateDetailDialog : DialogFragment() {
    
    private var _binding: DialogDateDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CalendarViewModel
    private var selectedDate: Date? = null
    private var existingRecord: MenstrualRecord? = null
    private var onRecordSavedListener: ((MenstrualRecord) -> Unit)? = null
    
    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    companion object {
        private const val ARG_DATE = "date"
        private const val ARG_DAY_INFO = "day_info"
        
        fun newInstance(date: Date, dayInfo: CalendarDayInfo?): DateDetailDialog {
            val dialog = DateDetailDialog()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            // dayInfo 可以为空，这里不传递复杂对象
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_WomenHealth_Dialog)
        
        arguments?.let {
            selectedDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_DATE, Date::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_DATE) as? Date
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDateDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[CalendarViewModel::class.java]
        
        setupViews()
        loadExistingRecord()
        setupClickListeners()
    }
    
    private fun setupViews() {
        selectedDate?.let { date ->
            binding.textViewSelectedDate.text = dateFormat.format(date)
        }
        
        // 设置流量等级选择
        setupFlowLevelSelection()
        
        // 设置症状选择
        setupSymptomsSelection()
    }
    
    private fun setupFlowLevelSelection() {
        FlowLevel.values().forEach { flowLevel ->
            val chip = Chip(requireContext())
            chip.text = flowLevel.displayName
            chip.isCheckable = true
            chip.tag = flowLevel.level
            binding.chipGroupFlowLevel.addView(chip)
        }
    }
    
    private fun setupSymptomsSelection() {
        MenstrualSymptom.values().forEach { symptom ->
            val chip = Chip(requireContext())
            chip.text = symptom.displayName
            chip.isCheckable = true
            chip.tag = symptom.name
            binding.chipGroupSymptoms.addView(chip)
        }
    }
    
    private fun loadExistingRecord() {
        selectedDate?.let { date ->
            viewModel.getRecordByDate(date) { record ->
                existingRecord = record
                record?.let {
                    populateFields(it)
                }
            }
        }
    }
    
    private fun populateFields(record: MenstrualRecord) {
        // 设置开始日期
        binding.textViewStartDate.text = dateFormat.format(record.startDate)
        
        // 设置结束日期
        record.endDate?.let {
            binding.textViewEndDate.text = dateFormat.format(it)
            binding.switchHasEndDate.isChecked = true
            binding.layoutEndDate.visibility = View.VISIBLE
        }
        
        // 设置流量等级
        for (i in 0 until binding.chipGroupFlowLevel.childCount) {
            val chip = binding.chipGroupFlowLevel.getChildAt(i) as Chip
            if (chip.tag == record.flowLevel) {
                chip.isChecked = true
                break
            }
        }
        
        // 设置症状
        record.symptoms?.split(",")?.forEach { symptomName ->
            for (i in 0 until binding.chipGroupSymptoms.childCount) {
                val chip = binding.chipGroupSymptoms.getChildAt(i) as Chip
                if (chip.tag == symptomName.trim()) {
                    chip.isChecked = true
                }
            }
        }
        
        // 设置备注
        binding.editTextNotes.setText(record.notes)
    }
    
    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonSave.setOnClickListener {
            saveRecord()
        }
        
        binding.buttonDelete.setOnClickListener {
            deleteRecord()
        }
        
        binding.switchHasEndDate.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutEndDate.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        binding.textViewStartDate.setOnClickListener {
            showDatePicker(true)
        }
        
        binding.textViewEndDate.setOnClickListener {
            showDatePicker(false)
        }
        
        // 显示/隐藏删除按钮
        binding.buttonDelete.visibility = if (existingRecord != null) View.VISIBLE else View.GONE
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.time = it }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDateText = dateFormat.format(calendar.time)
                
                if (isStartDate) {
                    binding.textViewStartDate.text = selectedDateText
                    selectedDate = calendar.time
                } else {
                    binding.textViewEndDate.text = selectedDateText
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun saveRecord() {
        val startDate = selectedDate ?: return
        
        val endDate = if (binding.switchHasEndDate.isChecked) {
            // 解析结束日期
            try {
                dateFormat.parse(binding.textViewEndDate.text.toString())
            } catch (e: Exception) {
                null
            }
        } else null
        
        // 获取选中的流量等级
        val flowLevel = getSelectedFlowLevel()
        
        // 获取选中的症状
        val symptoms = getSelectedSymptoms()
        
        // 获取备注
        val notes = binding.editTextNotes.text.toString().trim()
        
        val record = if (existingRecord != null) {
            existingRecord!!.copy(
                startDate = startDate,
                endDate = endDate,
                flowLevel = flowLevel,
                symptoms = symptoms,
                notes = notes.ifEmpty { null },
                updatedAt = Date()
            )
        } else {
            MenstrualRecord(
                startDate = startDate,
                endDate = endDate,
                flowLevel = flowLevel,
                symptoms = symptoms,
                notes = notes.ifEmpty { null }
            )
        }
        
        viewModel.saveRecord(
            record,
            onSuccess = {
                onRecordSavedListener?.invoke(record)
                dismiss()
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun deleteRecord() {
        existingRecord?.let { record ->
            viewModel.deleteRecord(
                record,
                onSuccess = {
                    onRecordSavedListener?.invoke(record)
                    dismiss()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    private fun getSelectedFlowLevel(): Int {
        for (i in 0 until binding.chipGroupFlowLevel.childCount) {
            val chip = binding.chipGroupFlowLevel.getChildAt(i) as Chip
            if (chip.isChecked) {
                return chip.tag as Int
            }
        }
        return 2 // 默认正常流量
    }
    
    private fun getSelectedSymptoms(): String? {
        val selectedSymptoms = mutableListOf<String>()
        for (i in 0 until binding.chipGroupSymptoms.childCount) {
            val chip = binding.chipGroupSymptoms.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedSymptoms.add(chip.tag as String)
            }
        }
        return if (selectedSymptoms.isNotEmpty()) {
            selectedSymptoms.joinToString(",")
        } else null
    }
    
    fun setOnRecordSavedListener(listener: (MenstrualRecord) -> Unit) {
        onRecordSavedListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}