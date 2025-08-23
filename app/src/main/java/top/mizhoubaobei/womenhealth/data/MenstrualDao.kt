package top.mizhoubaobei.womenhealth.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 月经记录数据访问对象
 */
@Dao
interface MenstrualDao {
    
    @Query("SELECT * FROM menstrual_records ORDER BY startDate DESC")
    fun getAllRecords(): Flow<List<MenstrualRecord>>
    
    @Query("SELECT * FROM menstrual_records WHERE startDate BETWEEN :startDate AND :endDate ORDER BY startDate DESC")
    fun getRecordsByDateRange(startDate: Date, endDate: Date): Flow<List<MenstrualRecord>>
    
    @Query("SELECT * FROM menstrual_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MenstrualRecord?
    
    @Query("SELECT * FROM menstrual_records ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestRecord(): MenstrualRecord?
    
    @Query("SELECT * FROM menstrual_records ORDER BY startDate DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int): List<MenstrualRecord>
    
    @Query("SELECT * FROM menstrual_records ORDER BY startDate DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getRecordsByPage(offset: Int, pageSize: Int): List<MenstrualRecord>
    
    @Query("SELECT * FROM menstrual_records WHERE startDate < :beforeDate ORDER BY startDate DESC LIMIT :pageSize")
    suspend fun getRecordsBeforeDate(beforeDate: Date, pageSize: Int): List<MenstrualRecord>
    
    @Query("SELECT * FROM menstrual_records WHERE startDate > :afterDate ORDER BY startDate ASC LIMIT :pageSize")
    suspend fun getRecordsAfterDate(afterDate: Date, pageSize: Int): List<MenstrualRecord>
    
    @Query("SELECT COUNT(*) FROM menstrual_records WHERE startDate < :date")
    suspend fun getRecordsCountBeforeDate(date: Date): Int
    
    @Query("SELECT COUNT(*) FROM menstrual_records WHERE startDate > :date")
    suspend fun getRecordsCountAfterDate(date: Date): Int
    
    @Insert
    suspend fun insertRecord(record: MenstrualRecord): Long
    
    @Update
    suspend fun updateRecord(record: MenstrualRecord)
    
    @Delete
    suspend fun deleteRecord(record: MenstrualRecord)
    
    @Query("DELETE FROM menstrual_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
    
    @Query("SELECT COUNT(*) FROM menstrual_records")
    suspend fun getRecordCount(): Int
    
    // 获取指定月份的记录
    @Query("SELECT * FROM menstrual_records WHERE startDate BETWEEN :startOfMonth AND :endOfMonth ORDER BY startDate")
    fun getRecordsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<MenstrualRecord>>
}