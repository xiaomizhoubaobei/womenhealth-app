package top.mizhoubaobei.womenhealth.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.mizhoubaobei.womenhealth.data.FlowLevel
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.data.MenstrualSymptom
import top.mizhoubaobei.womenhealth.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 月经记录列表适配器
 */
class RecordsAdapter(
    private val onItemClick: (MenstrualRecord) -> Unit,
    private val onDeleteClick: (MenstrualRecord) -> Unit
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {
    
    private var records = listOf<MenstrualRecord>()
    private val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    fun updateRecords(newRecords: List<MenstrualRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }
    
    override fun getItemCount(): Int = records.size
    
    inner class RecordViewHolder(
        private val binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(record: MenstrualRecord) {
            // 设置日期
            binding.textViewStartDate.text = fullDateFormat.format(record.startDate)
            
            if (record.endDate != null) {
                binding.textViewEndDate.text = "至 ${dateFormat.format(record.endDate)}"
                binding.textViewEndDate.visibility = View.VISIBLE
                
                // 计算经期长度
                val periodLength = ((record.endDate.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                binding.textViewPeriodLength.text = "${periodLength}天"
                binding.textViewPeriodLength.visibility = View.VISIBLE
            } else {
                binding.textViewEndDate.visibility = View.GONE
                binding.textViewPeriodLength.visibility = View.GONE
            }
            
            // 设置流量等级
            val flowLevel = FlowLevel.entries.find { it.level == record.flowLevel }
            binding.textViewFlowLevel.text = flowLevel?.displayName ?: "正常"
            
            // 设置流量颜色指示器
            val flowColor = when (record.flowLevel) {
                1 -> android.R.color.holo_blue_light
                2 -> android.R.color.holo_green_light
                3 -> android.R.color.holo_orange_light
                4 -> android.R.color.holo_red_light
                else -> android.R.color.holo_green_light
            }
            binding.viewFlowIndicator.setBackgroundResource(flowColor)
            
            // 设置症状
            if (!record.symptoms.isNullOrEmpty()) {
                val symptoms = record.symptoms.split(",").mapNotNull { symptomName ->
                    MenstrualSymptom.entries.find { it.name == symptomName.trim() }?.displayName
                }
                binding.textViewSymptoms.text = symptoms.joinToString("、")
                binding.textViewSymptoms.visibility = View.VISIBLE
                binding.labelSymptoms.visibility = View.VISIBLE
            } else {
                binding.textViewSymptoms.visibility = View.GONE
                binding.labelSymptoms.visibility = View.GONE
            }
            
            // 设置备注
            if (!record.notes.isNullOrEmpty()) {
                binding.textViewNotes.text = record.notes
                binding.textViewNotes.visibility = View.VISIBLE
                binding.labelNotes.visibility = View.VISIBLE
            } else {
                binding.textViewNotes.visibility = View.GONE
                binding.labelNotes.visibility = View.GONE
            }
            
            // 计算距离今天的天数
            val today = Calendar.getInstance()
            val recordDate = Calendar.getInstance()
            recordDate.time = record.startDate
            
            val daysDiff = ((today.timeInMillis - recordDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            when {
                daysDiff == 0 -> binding.textViewDaysAgo.text = "今天"
                daysDiff == 1 -> binding.textViewDaysAgo.text = "昨天"
                daysDiff < 30 -> binding.textViewDaysAgo.text = "${daysDiff}天前"
                daysDiff < 365 -> {
                    val monthsDiff = daysDiff / 30
                    binding.textViewDaysAgo.text = "${monthsDiff}个月前"
                }
                else -> {
                    val yearsDiff = daysDiff / 365
                    binding.textViewDaysAgo.text = "${yearsDiff}年前"
                }
            }
            
            // 设置点击事件
            binding.root.setOnClickListener {
                onItemClick(record)
            }
            
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(record)
            }
        }
    }
}