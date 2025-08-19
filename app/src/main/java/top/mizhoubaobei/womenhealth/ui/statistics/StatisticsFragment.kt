package top.mizhoubaobei.womenhealth.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import top.mizhoubaobei.womenhealth.R
import top.mizhoubaobei.womenhealth.databinding.FragmentStatisticsBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * 统计分析界面
 */
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var symptomAdapter: SymptomStatAdapter
    private val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        
        // 加载统计数据
        viewModel.loadStatistics()
    }
    
    private fun setupRecyclerView() {
        symptomAdapter = SymptomStatAdapter()
        binding.recyclerViewSymptoms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = symptomAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.statistics.observe(viewLifecycleOwner) { stats ->
            updateStatisticsUI(stats)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showError(error)
            } else {
                hideError()
            }
        }
    }
    
    private fun updateStatisticsUI(stats: DetailedStatistics) {
        // 基本统计信息
        binding.textViewTotalRecords.text = "${stats.totalRecords}次"
        
        if (stats.averageCycle > 0) {
            binding.textViewAverageCycle.text = "${stats.averageCycle}天"
        } else {
            binding.textViewAverageCycle.text = "暂无数据"
        }
        
        if (stats.averagePeriod > 0) {
            binding.textViewAveragePeriod.text = "${stats.averagePeriod}天"
        } else {
            binding.textViewAveragePeriod.text = "暂无数据"
        }
        
        // 最近一次经期
        if (stats.lastPeriodDate != null) {
            binding.textViewLastPeriod.text = dateFormat.format(stats.lastPeriodDate)
        } else {
            binding.textViewLastPeriod.text = "暂无记录"
        }
        
        // 预测下次经期
        if (stats.nextPredictedDate != null) {
            val today = Date()
            val daysUntil = ((stats.nextPredictedDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            val predictionText = when {
                daysUntil < 0 -> "${dateFormat.format(stats.nextPredictedDate)} (已延迟${-daysUntil}天)"
                daysUntil == 0 -> "${dateFormat.format(stats.nextPredictedDate)} (今天)"
                else -> "${dateFormat.format(stats.nextPredictedDate)} (还有${daysUntil}天)"
            }
            
            binding.textViewNextPredicted.text = predictionText
        } else {
            binding.textViewNextPredicted.text = "无法预测，需要更多数据"
        }
        
        // 周期规律性
        updateCycleRegularity(stats.cycleVariability)
        
        // 常见症状
        updateCommonSymptoms(stats.commonSymptoms)
        
        // 流量分布
        updateFlowDistribution(stats.flowDistribution)
        
        // 显示/隐藏内容
        if (stats.totalRecords > 0) {
            binding.cardBasicStats.visibility = View.VISIBLE
            binding.cardPeriodInfo.visibility = View.VISIBLE
            binding.cardSymptomsStats.visibility = View.VISIBLE
            binding.cardFlowStats.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        } else {
            binding.cardBasicStats.visibility = View.GONE
            binding.cardPeriodInfo.visibility = View.GONE
            binding.cardSymptomsStats.visibility = View.GONE
            binding.cardFlowStats.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        }
    }
    
    private fun updateCycleRegularity(variability: Int) {
        val regularityText = when {
            variability <= 2 -> "非常规律 (变化范围: ±${variability}天)"
            variability <= 4 -> "比较规律 (变化范围: ±${variability}天)"
            variability <= 7 -> "一般规律 (变化范围: ±${variability}天)"
            else -> "不太规律 (变化范围: ±${variability}天)"
        }
        
        binding.textViewCycleRegularity.text = regularityText
    }
    
    private fun updateCommonSymptoms(symptoms: Map<String, Int>) {
        if (symptoms.isNotEmpty()) {
            symptomAdapter.updateSymptoms(symptoms)
            binding.cardSymptomsStats.visibility = View.VISIBLE
        } else {
            binding.cardSymptomsStats.visibility = View.GONE
        }
    }
    
    private fun updateFlowDistribution(flowDistribution: Map<Int, Int>) {
        binding.layoutFlowDistribution.removeAllViews()
        
        if (flowDistribution.isNotEmpty()) {
            val totalRecords = flowDistribution.values.sum()
            val flowLevels = arrayOf("轻量", "中等", "大量", "极大量")
            
            flowDistribution.toSortedMap().forEach { (level, count) ->
                if (level in 1..4) {
                    val itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_flow_stat, binding.layoutFlowDistribution, false)
                    
                    val textFlowLevel = itemView.findViewById<TextView>(R.id.text_flow_level)
                    val textFlowCount = itemView.findViewById<TextView>(R.id.text_flow_count)
                    val progressFlow = itemView.findViewById<ProgressBar>(R.id.progress_flow)
                    
                    textFlowLevel.text = flowLevels[level - 1]
                    textFlowCount.text = "${count}次"
                    
                    val percentage = if (totalRecords > 0) (count * 100) / totalRecords else 0
                    progressFlow.progress = percentage
                    
                    binding.layoutFlowDistribution.addView(itemView)
                }
            }
            
            binding.cardFlowStats.visibility = View.VISIBLE
        } else {
            binding.cardFlowStats.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        binding.textViewError.text = message
        binding.cardError.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        binding.cardError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}