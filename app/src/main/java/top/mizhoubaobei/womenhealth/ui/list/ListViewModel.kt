package top.mizhoubaobei.womenhealth.ui.list

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
 * 列表界面的ViewModel
 */
class ListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = SQLiteMenstrualStorage(application)
    
    private val _allRecords = MutableLiveData<List<MenstrualRecord>>()
    val allRecords: LiveData<List<MenstrualRecord>> = _allRecords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage
    
    private val _totalPages = MutableLiveData(0)
    val totalPages: LiveData<Int> = _totalPages
    
    /**
     * 分页加载记录
     */
    fun loadRecords(page: Int = 0, pageSize: Int = 20, lastRecordDate: Date? = null) {
        if (isLoading.value == true) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val records = if (lastRecordDate == null) {
                    storage.getRecordsByPage(page * pageSize, pageSize)
                } else {
                    storage.getRecordsBeforeDate(lastRecordDate, pageSize)
                }
                
                // 如果是第一页则直接替换，否则追加记录
                if (page == 0 || lastRecordDate == null) {
                    _allRecords.value = records
                } else {
                    _allRecords.value = (_allRecords.value ?: emptyList()) + records
                }
                
                _currentPage.value = page
                
                // 计算总页数
                val totalCount = if (lastRecordDate == null) {
                    storage.getRecordCount()
                } else {
                    storage.getRecordsCountBeforeDate(lastRecordDate)
                }
                _totalPages.value = (totalCount + pageSize - 1) / pageSize
            } catch (e: Exception) {
                _errorMessage.value = "加载数据失败: ${e.message}"
            } finally {
                _isLoading.postValue(false) // 使用postValue确保线程安全
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
                    storage.insertRecord(record)
                } else {
                    storage.updateRecord(record.copy(updatedAt = Date()))
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
                storage.deleteRecord(record)
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
                val recentRecords = storage.getRecentRecords(6)
                val statistics = calculateStatistics(recentRecords)
                callback(statistics)
            } catch (e: Exception) {
                callback(RecordStatistics())
            }
        }
    }
    
    private fun calculateStatistics(records: List<MenstrualRecord>): RecordStatistics {
        if (records.isEmpty()) return RecordStatistics()
        
        // 只保留必要的计算逻辑
        val latest = records.firstOrNull()
        
        // 使用windowed计算周期，更简洁高效
        val averageCycle = records.windowed(2)
            .mapNotNull { (prev, curr) -> 
                (curr.startDate.time - prev.startDate.time) / (1000 * 60 * 60 * 24) 
            }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toInt() ?: 0

        // 使用filter和map计算经期长度
        val averagePeriod = records
            .filter { it.endDate != null }
            .mapNotNull { 
                ((it.endDate!!.time - it.startDate.time) / (1000 * 60 * 60 * 24) + 1).takeIf { len -> len in 1..14 }
            }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toInt() ?: 0

        // 预测下次经期
        val nextPredicted = if (averageCycle > 0 && latest != null) {
            Calendar.getInstance().apply {
                time = latest.startDate
                add(Calendar.DAY_OF_MONTH, averageCycle)
            }.time
        } else null
        
        return RecordStatistics(
            totalRecords = records.size,
            averageCycle = averageCycle,
            averagePeriod = averagePeriod,
            latestRecord = latest,
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