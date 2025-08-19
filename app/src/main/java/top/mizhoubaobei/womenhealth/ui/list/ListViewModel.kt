package top.mizhoubaobei.womenhealth.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.mizhoubaobei.womenhealth.data.AppDatabase
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import java.util.Calendar
import java.util.Date

/**
 * 列表界面的ViewModel
 */
class ListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val menstrualDao = database.menstrualDao()
    
    private val _allRecords = MutableLiveData<List<MenstrualRecord>>()
    val allRecords: LiveData<List<MenstrualRecord>> = _allRecords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * 加载所有记录
     */
    fun loadAllRecords() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                menstrualDao.getAllRecords().collect { records ->
                    _allRecords.value = records
                }
            } catch (e: Exception) {
                _errorMessage.value = "加载数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 保存记录
     */
    fun saveRecord(record: MenstrualRecord, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (record.id == 0L) {
                    menstrualDao.insertRecord(record)
                } else {
                    menstrualDao.updateRecord(record.copy(updatedAt = Date()))
                }
                onSuccess()
            } catch (e: Exception) {
                onError("保存失败: ${e.message}")
            }
        }
    }
    
    /**
     * 删除记录
     */
    fun deleteRecord(record: MenstrualRecord) {
        viewModelScope.launch {
            try {
                menstrualDao.deleteRecord(record)
            } catch (e: Exception) {
                _errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }
    
    /**
     * 获取统计信息
     */
    fun getStatistics(callback: (RecordStatistics) -> Unit) {
        viewModelScope.launch {
            try {
                val recentRecords = menstrualDao.getRecentRecords(6)
                val statistics = calculateStatistics(recentRecords)
                callback(statistics)
            } catch (e: Exception) {
                callback(RecordStatistics())
            }
        }
    }
    
    private fun calculateStatistics(records: List<MenstrualRecord>): RecordStatistics {
        if (records.size < 2) {
            return RecordStatistics(
                totalRecords = records.size,
                latestRecord = records.firstOrNull()
            )
        }
        
        // 计算平均周期长度
        var totalCycleDays = 0
        var cycleCount = 0
        
        for (i in 0 until records.size - 1) {
            val current = records[i]
            val previous = records[i + 1]
            val daysBetween = ((current.startDate.time - previous.startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            if (daysBetween > 0 && daysBetween < 60) { // 合理的周期范围
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
                if (periodLength > 0 && periodLength < 15) { // 合理的经期长度
                    totalPeriodDays += periodLength
                    periodCount++
                }
            }
        }
        
        val averagePeriod = if (periodCount > 0) totalPeriodDays / periodCount else 0
        
        // 预测下次经期
        val nextPredicted = if (averageCycle > 0) {
            val latestRecord = records.first()
            val calendar = Calendar.getInstance()
            calendar.time = latestRecord.startDate
            calendar.add(Calendar.DAY_OF_MONTH, averageCycle)
            calendar.time
        } else null
        
        return RecordStatistics(
            totalRecords = records.size,
            averageCycle = averageCycle,
            averagePeriod = averagePeriod,
            latestRecord = records.firstOrNull(),
            nextPredicted = nextPredicted
        )
    }
}

/**
 * 记录统计信息
 */
data class RecordStatistics(
    val totalRecords: Int = 0,
    val averageCycle: Int = 0,
    val averagePeriod: Int = 0,
    val latestRecord: MenstrualRecord? = null,
    val nextPredicted: Date? = null
)