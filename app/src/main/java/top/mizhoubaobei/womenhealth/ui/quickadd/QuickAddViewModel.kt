package top.mizhoubaobei.womenhealth.ui.quickadd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.mizhoubaobei.womenhealth.data.SQLiteMenstrualStorage
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import java.util.Calendar
import java.util.Date

/**
 * 快速添加界面的ViewModel
 */
class QuickAddViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = SQLiteMenstrualStorage(application)
    
    private val _latestRecord = MutableLiveData<MenstrualRecord?>()
    val latestRecord: LiveData<MenstrualRecord?> = _latestRecord
    
    private val _predictedNextPeriod = MutableLiveData<Date?>()
    val predictedNextPeriod: LiveData<Date?> = _predictedNextPeriod
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _recordSavedEvent = MutableLiveData<Unit>()
    val recordSavedEvent: LiveData<Unit> = _recordSavedEvent
    
    /**
     * 加载最新记录
     */
    fun loadLatestRecord() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val record = storage.getLatestRecord()
                _latestRecord.value = record
                
                // 同时预测下次经期
                predictNextPeriod()
            } catch (e: Exception) {
                _errorMessage.value = "加载数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 保存新记录
     */
    fun saveRecord(record: MenstrualRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                storage.insertRecord(record)
                _recordSavedEvent.postValue(Unit)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新记录
     */
    fun updateRecord(record: MenstrualRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                storage.updateRecord(record)
                _recordSavedEvent.postValue(Unit)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "更新失败: ${e.message}"
            }
        }
    }
    
    /**
     * 预测下次经期
     */
    private fun predictNextPeriod() {
        viewModelScope.launch {
            try {
                val recentRecords = storage.getRecentRecords(3)
                if (recentRecords.size >= 2) {
                    // 计算平均周期长度
                    var totalCycleDays = 0
                    var cycleCount = 0
                    
                    for (i in 0 until recentRecords.size - 1) {
                        val current = recentRecords[i]
                        val previous = recentRecords[i + 1]
                        val daysBetween = ((current.startDate.time - previous.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
                        if (daysBetween > 15 && daysBetween < 45) { // 合理的周期范围
                            totalCycleDays += daysBetween
                            cycleCount++
                        }
                    }
                    
                    if (cycleCount > 0) {
                        val averageCycle = totalCycleDays / cycleCount
                        val lastPeriod = recentRecords.first().startDate
                        val calendar = Calendar.getInstance()
                        calendar.time = lastPeriod
                        calendar.add(Calendar.DAY_OF_MONTH, averageCycle)
                        _predictedNextPeriod.value = calendar.time
                    } else {
                        _predictedNextPeriod.value = null
                    }
                } else {
                    _predictedNextPeriod.value = null
                }
            } catch (e: Exception) {
                _predictedNextPeriod.value = null
            }
        }
    }
    
    /**
     * 获取周期统计信息
     */
    fun getCycleStats(callback: (CycleStats) -> Unit) {
        viewModelScope.launch {
            try {
                val recentRecords = storage.getRecentRecords(6)
                val stats = calculateCycleStats(recentRecords)
                callback(stats)
            } catch (e: Exception) {
                callback(CycleStats())
            }
        }
    }
    
    private fun calculateCycleStats(records: List<MenstrualRecord>): CycleStats {
        if (records.size < 2) {
            return CycleStats()
        }
        
        // 计算平均周期长度
        var totalCycleDays = 0
        var cycleCount = 0
        
        for (i in 0 until records.size - 1) {
            val current = records[i]
            val previous = records[i + 1]
            val daysBetween = ((current.startDate.time - previous.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            if (daysBetween > 15 && daysBetween < 45) {
                totalCycleDays += daysBetween
                cycleCount++
            }
        }
        
        val averageCycle = if (cycleCount > 0) totalCycleDays / cycleCount else 0
        
        // 计算平均经期长度
        var totalPeriodDays = 0
        var periodCount = 0
        
        records.forEach { record ->
            if (record.endDate != null) {
                val periodLength = ((record.endDate.time - record.startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                if (periodLength > 0 && periodLength < 15) {
                    totalPeriodDays += periodLength
                    periodCount++
                }
            }
        }
        
        val averagePeriod = if (periodCount > 0) totalPeriodDays / periodCount else 0
        
        return CycleStats(
            averageCycle = averageCycle,
            averagePeriod = averagePeriod,
            totalRecords = records.size
        )
    }
}

/**
 * 周期统计信息
 */
data class CycleStats(
    val averageCycle: Int = 0,
    val averagePeriod: Int = 0,
    val totalRecords: Int = 0
)