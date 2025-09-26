package top.mizhoubaobei.womenhealth.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import top.mizhoubaobei.womenhealth.data.SQLiteMenstrualStorage
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.data.MenstrualSymptom
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 统计界面的ViewModel
 */
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = SQLiteMenstrualStorage(application)
    
    private val _statistics = MutableLiveData<DetailedStatistics>()
    val statistics: LiveData<DetailedStatistics> = _statistics
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * 加载统计数据
     */
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _errorMessage.postValue("")
                
                // 使用first()获取单次数据而不是Flow
                val records = storage.getAllRecordsFlow().first()
                val stats = calculateDetailedStatistics(records)
                _statistics.postValue(stats)
            } catch (e: Exception) {
                _errorMessage.postValue("加载统计数据失败: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    private fun calculateDetailedStatistics(records: List<MenstrualRecord>): DetailedStatistics {
        if (records.isEmpty()) {
            return DetailedStatistics()
        }
        
        val sortedRecords = records.sortedByDescending { it.startDate }
        
        // 基本统计
        val totalRecords = records.size
        val lastPeriodDate = sortedRecords.firstOrNull()?.startDate
        
        // 计算周期统计
        val cycleStats = calculateCycleStatistics(sortedRecords)
        
        // 计算经期统计
        val periodStats = calculatePeriodStatistics(records)
        
        // 预测下次经期
        val nextPredicted = predictNextPeriod(sortedRecords, cycleStats.averageCycle)
        
        // 症状统计
        val symptomStats = calculateSymptomStatistics(records)
        
        // 流量统计
        val flowStats = calculateFlowStatistics(records)
        
        return DetailedStatistics(
            totalRecords = totalRecords,
            averageCycle = cycleStats.averageCycle,
            averagePeriod = periodStats.averagePeriod,
            lastPeriodDate = lastPeriodDate,
            nextPredictedDate = nextPredicted,
            cycleVariability = cycleStats.variability,
            commonSymptoms = symptomStats,
            flowDistribution = flowStats
        )
    }
    
    private fun calculateCycleStatistics(sortedRecords: List<MenstrualRecord>): CycleStatistics {
        if (sortedRecords.size < 2) {
            return CycleStatistics()
        }
        
        val cycleLengths = mutableListOf<Int>()
        
        for (i in 0 until sortedRecords.size - 1) {
            val current = sortedRecords[i]
            val previous = sortedRecords[i + 1]
            val daysBetween = ((current.startDate.time - previous.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            
            // 只考虑合理的周期长度
            if (daysBetween in 15..45) {
                cycleLengths.add(daysBetween)
            }
        }
        
        if (cycleLengths.isEmpty()) {
            return CycleStatistics()
        }
        
        val averageCycle = cycleLengths.average().toInt()
        
        // 计算变异性（标准差）
        val variance = cycleLengths.map { (it - averageCycle) * (it - averageCycle) }.average()
        val variability = sqrt(variance).toInt()
        
        return CycleStatistics(
            averageCycle = averageCycle,
            variability = variability,
            cycleLengths = cycleLengths
        )
    }
    
    private fun calculatePeriodStatistics(records: List<MenstrualRecord>): PeriodStatistics {
        val periodLengths = records.mapNotNull { record ->
            record.endDate?.let { endDate ->
                val length = ((endDate.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                if (length in 1..15) length else null
            }
        }
        
        val averagePeriod = if (periodLengths.isNotEmpty()) {
            periodLengths.average().toInt()
        } else 0
        
        return PeriodStatistics(
            averagePeriod = averagePeriod,
            periodLengths = periodLengths
        )
    }
    
    private fun predictNextPeriod(sortedRecords: List<MenstrualRecord>, averageCycle: Int): Date? {
        if (sortedRecords.isEmpty() || averageCycle <= 0) {
            return null
        }
        
        val lastPeriod = sortedRecords.first().startDate
        val calendar = Calendar.getInstance()
        calendar.time = lastPeriod
        calendar.add(Calendar.DAY_OF_MONTH, averageCycle)
        
        return calendar.time
    }
    
    private fun calculateSymptomStatistics(records: List<MenstrualRecord>): Map<String, Int> {
        val symptomCounts = mutableMapOf<String, Int>()
        
        records.forEach { record ->
            record.symptoms?.split(",")?.forEach { symptomName ->
                val trimmedName = symptomName.trim()
                if (trimmedName.isNotEmpty()) {
                    // 转换为显示名称
                    val displayName = MenstrualSymptom.values()
                        .find { it.name == trimmedName }?.displayName ?: trimmedName
                    
                    symptomCounts[displayName] = symptomCounts.getOrDefault(displayName, 0) + 1
                }
            }
        }
        
        return symptomCounts
    }
    
    private fun calculateFlowStatistics(records: List<MenstrualRecord>): Map<Int, Int> {
        val flowCounts = mutableMapOf<Int, Int>()
        
        records.forEach { record ->
            val flowLevel = record.flowLevel
            flowCounts[flowLevel] = flowCounts.getOrDefault(flowLevel, 0) + 1
        }
        
        return flowCounts
    }
}

/**
 * 详细统计信息
 */
data class DetailedStatistics(
    val totalRecords: Int = 0,
    val averageCycle: Int = 0,
    val averagePeriod: Int = 0,
    val lastPeriodDate: Date? = null,
    val nextPredictedDate: Date? = null,
    val cycleVariability: Int = 0,
    val commonSymptoms: Map<String, Int> = emptyMap(),
    val flowDistribution: Map<Int, Int> = emptyMap()
)

/**
 * 周期统计信息
 */
data class CycleStatistics(
    val averageCycle: Int = 0,
    val variability: Int = 0,
    val cycleLengths: List<Int> = emptyList()
)

/**
 * 经期统计信息
 */
data class PeriodStatistics(
    val averagePeriod: Int = 0,
    val periodLengths: List<Int> = emptyList()
)