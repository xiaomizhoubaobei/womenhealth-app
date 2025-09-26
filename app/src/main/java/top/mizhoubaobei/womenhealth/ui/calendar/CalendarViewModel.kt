package top.mizhoubaobei.womenhealth.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import top.mizhoubaobei.womenhealth.data.SQLiteMenstrualStorage
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import java.util.Calendar
import java.util.Date

/**
 * 日历界面的ViewModel
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = SQLiteMenstrualStorage(application)
    
    private val _monthlyRecords = MutableLiveData<List<MenstrualRecord>>()
    val monthlyRecords: LiveData<List<MenstrualRecord>> = _monthlyRecords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * 加载指定月份的记录
     */
    fun loadMonthlyRecords(year: Int, month: Int) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                
                // 计算月份的开始和结束时间戳
                val calendar = Calendar.getInstance().apply {
                    set(year, month, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfMonth = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                val endOfMonth = calendar.timeInMillis
                
                // 使用first()获取单次数据而不是Flow
                val records = storage.getRecordsByMonth(startOfMonth, endOfMonth).first()
                _monthlyRecords.postValue(records)
            } catch (e: Exception) {
                _errorMessage.postValue("加载数据失败: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    /**
     * 保存月经记录
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
    fun deleteRecord(record: MenstrualRecord, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                storage.deleteRecord(record)
                onSuccess()
            } catch (e: Exception) {
                onError("删除失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取指定日期的记录
     */
    fun getRecordByDate(date: Date, callback: (MenstrualRecord?) -> Unit) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val startOfNextDay = calendar.time
                
                val records = storage.getRecordsByDateRange(startOfDay, startOfNextDay)
                callback(records.firstOrNull())
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    
    /**
     * 预测下次月经日期
     */
    fun predictNextPeriod(callback: (Date?) -> Unit) {
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
                        totalCycleDays += daysBetween
                        cycleCount++
                    }
                    
                    if (cycleCount > 0) {
                        val averageCycle = totalCycleDays / cycleCount
                        val lastPeriod = recentRecords.first().startDate
                        val calendar = Calendar.getInstance()
                        calendar.time = lastPeriod
                        calendar.add(Calendar.DAY_OF_MONTH, averageCycle)
                        callback(calendar.time)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
}