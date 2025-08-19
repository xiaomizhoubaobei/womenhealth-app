package top.mizhoubaobei.womenhealth.ui.quickadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import top.mizhoubaobei.womenhealth.R
import top.mizhoubaobei.womenhealth.databinding.DialogSymptomsBinding
import top.mizhoubaobei.womenhealth.data.MenstrualSymptom

/**
 * 症状记录对话框
 */
class SymptomsDialog : DialogFragment() {
    
    private var _binding: DialogSymptomsBinding? = null
    private val binding get() = _binding!!
    
    private var onSymptomsRecordedListener: ((String?) -> Unit)? = null
    
    companion object {
        fun newInstance(): SymptomsDialog {
            return SymptomsDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_WomenHealth_Dialog)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSymptomsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupClickListeners()
    }
    
    private fun setupViews() {
        // 设置症状选择
        MenstrualSymptom.values().forEach { symptom ->
            val chip = Chip(requireContext())
            chip.text = symptom.displayName
            chip.isCheckable = true
            chip.tag = symptom.name
            binding.chipGroupSymptoms.addView(chip)
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonSave.setOnClickListener {
            saveSymptoms()
        }
        
        binding.buttonClearAll.setOnClickListener {
            clearAllSymptoms()
        }
    }
    
    private fun saveSymptoms() {
        val selectedSymptoms = getSelectedSymptoms()
        onSymptomsRecordedListener?.invoke(selectedSymptoms)
        dismiss()
    }
    
    private fun clearAllSymptoms() {
        for (i in 0 until binding.chipGroupSymptoms.childCount) {
            val chip = binding.chipGroupSymptoms.getChildAt(i) as Chip
            chip.isChecked = false
        }
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
    
    fun setOnSymptomsRecordedListener(listener: (String?) -> Unit) {
        onSymptomsRecordedListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}