package top.mizhoubaobei.womenhealth.ui.list

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
import top.mizhoubaobei.womenhealth.databinding.DialogAddRecordBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 添加/编辑记录对话框
 */
class AddRecordDialog : DialogFragment() {
    
    private var _binding: DialogAddRecordBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ListViewModel
    private var editingRecord: MenstrualRecord? = null
    private var onRecordSavedListener: ((MenstrualRecord) -> Unit)? = null
    
    private var selectedStartDate: Date = Date()
    private var selectedEndDate: Date? = null
    
    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    companion object {
        private const val ARG_RECORD = "record"
        
        fun newInstance(record: MenstrualRecord? = null): AddRecordDialog {
            val dialog = AddRecordDialog()
            val args = Bundle()
            record?.let {
                args.putSerializable(ARG_RECORD, it)
            }
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_WomenHealth_Dialog)
        
        arguments?.let {
            editingRecord = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_RECORD, MenstrualRecord::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_RECORD) as? MenstrualRecord
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddRecordBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        
        setupViews()
        setupClickListeners()
        
        // 如果是编辑模式，填充现有数据
        editingRecord?.let { populateFields(it) }
    }
    
    private fun setupViews() {
        // 设置标题
        binding.textViewTitle.text = if (editingRecord != null) getString(R.string.dialog_edit_record) else getString(R.string.dialog_add_record)
        
        // 设置默认开始日期为今天
        binding.textViewStartDate.text = dateFormat.format(selectedStartDate)
        
        // 设置流量等级选择
        setupFlowLevelSelection()
        
        // 设置症状选择
        setupSymptomsSelection()
    }
    
    private fun setupFlowLevelSelection() {
        FlowLevel.entries.forEach { flowLevel ->
            val chip = Chip(requireContext())
            chip.text = flowLevel.displayName
            chip.isCheckable = true
            chip.tag = flowLevel.level
            binding.chipGroupFlowLevel.addView(chip)
        }
        
        // 默认选择正常流量
        val normalChip = binding.chipGroupFlowLevel.findViewWithTag<Chip>(2)
        normalChip?.isChecked = true
    }
    
    private fun setupSymptomsSelection() {
        MenstrualSymptom.entries.forEach { symptom ->
            val chip = Chip(requireContext())
            chip.text = symptom.displayName
            chip.isCheckable = true
            chip.tag = symptom.name
            binding.chipGroupSymptoms.addView(chip)
        }
    }
    
    private fun populateFields(record: MenstrualRecord) {
        selectedStartDate = record.startDate
        selectedEndDate = record.endDate
        
        // 设置开始日期
        binding.textViewStartDate.text = dateFormat.format(record.startDate)
        
        // 设置结束日期
        record.endDate?.let { endDate ->
            binding.textViewEndDate.text = dateFormat.format(endDate)
            binding.switchHasEndDate.isChecked = true
            binding.layoutEndDate.visibility = View.VISIBLE
        }
        
        // 设置流量等级
        val flowChip = binding.chipGroupFlowLevel.findViewWithTag<Chip>(record.flowLevel)
        flowChip?.isChecked = true
        
        // 设置症状
        record.symptoms?.split(",")?.forEach { symptomName ->
            val symptomChip = binding.chipGroupSymptoms.findViewWithTag<Chip>(symptomName.trim())
            symptomChip?.isChecked = true
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
        
        binding.switchHasEndDate.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutEndDate.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                selectedEndDate = null
                binding.textViewEndDate.text = "选择结束日期"
            }
        }
        
        binding.textViewStartDate.setOnClickListener {
            showDatePicker(true)
        }
        
        binding.textViewEndDate.setOnClickListener {
            showDatePicker(false)
        }
        
        binding.buttonQuickToday.setOnClickListener {
            selectedStartDate = Date()
            binding.textViewStartDate.text = dateFormat.format(selectedStartDate)
        }
        
        binding.buttonQuickYesterday.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            selectedStartDate = calendar.time
            binding.textViewStartDate.text = dateFormat.format(selectedStartDate)
        }
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val currentDate = if (isStartDate) selectedStartDate else (selectedEndDate ?: selectedStartDate)
        calendar.time = currentDate
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time
                
                if (isStartDate) {
                    selectedStartDate = selectedDate
                    binding.textViewStartDate.text = dateFormat.format(selectedDate)
                } else {
                    selectedEndDate = selectedDate
                    binding.textViewEndDate.text = dateFormat.format(selectedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun saveRecord() {
        // 验证数据
        if (binding.switchHasEndDate.isChecked && selectedEndDate == null) {
            Toast.makeText(context, "请选择结束日期", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedEndDate != null && selectedEndDate!! < selectedStartDate) {
            Toast.makeText(context, "结束日期不能早于开始日期", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取选中的流量等级
        val flowLevel = getSelectedFlowLevel()
        
        // 获取选中的症状
        val symptoms = getSelectedSymptoms()
        
        // 获取备注
        val notes = binding.editTextNotes.text.toString().trim()
        
        // 计算周期和经期长度
        val periodLength = selectedEndDate?.let { endDate ->
            ((endDate.time - selectedStartDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        }
        
        val record = if (editingRecord != null) {
            editingRecord!!.copy(
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                flowLevel = flowLevel,
                periodLength = periodLength,
                symptoms = symptoms,
                notes = notes.ifEmpty { null },
                updatedAt = Date()
            )
        } else {
            MenstrualRecord(
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                flowLevel = flowLevel,
                periodLength = periodLength,
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