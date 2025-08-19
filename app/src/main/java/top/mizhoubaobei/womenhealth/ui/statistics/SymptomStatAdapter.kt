package top.mizhoubaobei.womenhealth.ui.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.mizhoubaobei.womenhealth.R

/**
 * 症状统计适配器
 */
class SymptomStatAdapter : RecyclerView.Adapter<SymptomStatAdapter.ViewHolder>() {
    
    private var symptoms: List<SymptomStat> = emptyList()
    private var maxCount: Int = 0
    
    fun updateSymptoms(symptomMap: Map<String, Int>) {
        symptoms = symptomMap.map { (name, count) ->
            SymptomStat(name, count)
        }.sortedByDescending { it.count }
        
        maxCount = symptoms.maxOfOrNull { it.count } ?: 0
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_symptom_stat, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(symptoms[position], maxCount)
    }
    
    override fun getItemCount(): Int = symptoms.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSymptomName: TextView = itemView.findViewById(R.id.text_symptom_name)
        private val textSymptomCount: TextView = itemView.findViewById(R.id.text_symptom_count)
        private val progressSymptom: ProgressBar = itemView.findViewById(R.id.progress_symptom)
        
        fun bind(symptom: SymptomStat, maxCount: Int) {
            textSymptomName.text = symptom.name
            textSymptomCount.text = "${symptom.count}次"
            
            // 设置进度条
            if (maxCount > 0) {
                val progress = (symptom.count * 100) / maxCount
                progressSymptom.progress = progress
            } else {
                progressSymptom.progress = 0
            }
        }
    }
}

/**
 * 症状统计数据类
 */
data class SymptomStat(
    val name: String,
    val count: Int
)