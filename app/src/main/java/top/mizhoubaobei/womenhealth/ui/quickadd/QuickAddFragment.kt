package top.mizhoubaobei.womenhealth.ui.quickadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.databinding.FragmentQuickAddBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 快速添加月经记录界面
 */
class QuickAddFragment : Fragment() {

    private var _binding: FragmentQuickAddBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: QuickAddViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[QuickAddViewModel::class.java]
        
        setupObservers()
        setupClickListeners()
        
        // 加载最新记录信息
        viewModel.loadLatestRecord()
    }
    
    private fun setupObservers() {
        viewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            updateUI(record)
        }
        
        viewModel.predictedNextPeriod.observe(viewLifecycleOwner) { predictedDate ->
            updatePrediction(predictedDate)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 暂时移除进度条显示，因为布局中没有对应的视图
        }
    }
    
    private fun setupClickListeners() {
        binding.btnStartPeriod.setOnClickListener {
            startPeriod()
        }
        
        binding.btnRecordToday.setOnClickListener {
            recordToday()
        }
        
        binding.btnRecordYesterday.setOnClickListener {
            recordYesterday()
        }
        
        binding.btnRecordSymptoms.setOnClickListener {
            recordSymptoms()
        }
        
        binding.cardStatisticsPreview.setOnClickListener {
            // 点击统计预览卡片
        }
    }
    
    private fun updateUI(record: MenstrualRecord?) {
        if (record != null) {
            // 检查是否是当前进行中的经期
            val today = Date()
            val isCurrentPeriod = record.endDate == null && 
                ((today.time - record.startDate.time) / (1000 * 60 * 60 * 24)) <= 10
            
            if (isCurrentPeriod) {
                // 当前正在经期中
                val daysSinceStart = ((today.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                val startDateText = java.text.SimpleDateFormat("MM月dd日", Locale.getDefault()).format(record.startDate)
                binding.textViewCurrentPeriodStatus.text = "第${daysSinceStart}天 (开始于 $startDateText)"
            } else {
                // 没有进行中的经期
                val daysSinceLastPeriod = ((today.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                binding.textViewCurrentPeriodStatus.text = "上次经期：${daysSinceLastPeriod}天前"
            }
        } else {
            // 没有任何记录
            binding.textViewCurrentPeriodStatus.text = "暂无记录"
        }
    }
    
    private fun updatePrediction(predictedDate: Date?) {
        if (predictedDate != null) {
            val today = Date()
            val daysUntilPredicted = ((predictedDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            val dateText = java.text.SimpleDateFormat("MM月dd日", Locale.getDefault()).format(predictedDate)
            
            val predictionText = when {
                daysUntilPredicted < 0 -> "预测经期：${dateText} (已延迟${-daysUntilPredicted}天)"
                daysUntilPredicted == 0 -> "预测经期：${dateText} (今天)"
                else -> "预测经期：${dateText} (还有${daysUntilPredicted}天)"
            }
            
            binding.textViewNextPredicted.text = predictionText
        } else {
            binding.textViewNextPredicted.text = "预测经期：数据不足"
        }
    }
    
    private fun startPeriod() {
        val record = MenstrualRecord(
            startDate = Date(),
            flowLevel = 2 // 默认正常流量
        )
        
        viewModel.saveRecord(record) {
            Toast.makeText(context, "已记录经期开始", Toast.LENGTH_SHORT).show()
            viewModel.loadLatestRecord()
        }
    }
    
    private fun recordToday() {
        val record = MenstrualRecord(
            startDate = Date(),
            flowLevel = 2
        )
        
        viewModel.saveRecord(record) {
            Toast.makeText(context, "已记录今日经期", Toast.LENGTH_SHORT).show()
            viewModel.loadLatestRecord()
        }
    }
    
    private fun recordYesterday() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        
        val record = MenstrualRecord(
            startDate = yesterday.time,
            flowLevel = 2
        )
        
        viewModel.saveRecord(record) {
            Toast.makeText(context, "已记录昨日经期", Toast.LENGTH_SHORT).show()
            viewModel.loadLatestRecord()
        }
    }
    
    private fun recordSymptoms() {
        // 打开症状记录对话框
        val dialog = SymptomsDialog.newInstance()
        dialog.setOnSymptomsRecordedListener { symptoms ->
            // 更新当前记录的症状
            viewModel.latestRecord.value?.let { record ->
                val updatedRecord = record.copy(
                    symptoms = symptoms,
                    updatedAt = Date()
                )
                
                viewModel.updateRecord(updatedRecord) {
                    Toast.makeText(context, "症状已记录", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show(parentFragmentManager, "SymptomsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}