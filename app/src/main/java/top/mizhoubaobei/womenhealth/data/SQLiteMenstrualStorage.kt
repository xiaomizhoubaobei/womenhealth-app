package top.mizhoubaobei.womenhealth.data

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * SQLite实现的月经记录存储
 */
class SQLiteMenstrualStorage(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.menstrualDao()
    
    suspend fun getAllRecords(): List<MenstrualRecord> = withContext(Dispatchers.IO) {
        dao.getRecentRecords(Int.MAX_VALUE) // 使用已有的同步方法获取所有记录
    }
    
    fun getAllRecordsFlow(): Flow<List<MenstrualRecord>> = dao.getAllRecords().flowOn(Dispatchers.IO)
    
    suspend fun getRecordsByDateRange(startDate: Date, endDate: Date): List<MenstrualRecord> {
        return dao.getRecordsByDateRange(startDate, endDate).first()
    }
    
    suspend fun getLatestRecord(): MenstrualRecord? {
        return dao.getLatestRecord()
    }
    
    suspend fun getRecentRecords(limit: Int): List<MenstrualRecord> {
        return dao.getRecentRecords(limit)
    }
    
    suspend fun getRecordsByPage(page: Int, pageSize: Int): List<MenstrualRecord> {
        return dao.getRecordsByPage(page * pageSize, pageSize)
    }
    
    suspend fun insertRecord(record: MenstrualRecord): Long {
        return withContext(Dispatchers.IO) {
            dao.insertRecord(record).also {
                database.query(SimpleSQLiteQuery("SELECT changes()")).use { cursor ->
                    cursor.moveToFirst()
                    if (cursor.getInt(0) == 0) {
                        throw IllegalStateException("插入记录失败")
                    }
                }
            }
        }
    }
    
    suspend fun updateRecord(record: MenstrualRecord) {
        withContext(Dispatchers.IO) {
            dao.updateRecord(record.copy(updatedAt = Date())).also {
                database.query(SimpleSQLiteQuery("SELECT changes()")).use { cursor ->
                    cursor.moveToFirst()
                    if (cursor.getInt(0) == 0) {
                        throw IllegalStateException("更新记录失败")
                    }
                }
            }
        }
    }
    
    suspend fun deleteRecord(record: MenstrualRecord) {
        withContext(Dispatchers.IO) {
            dao.deleteRecord(record).also {
                database.query(SimpleSQLiteQuery("SELECT changes()")).use { cursor ->
                    cursor.moveToFirst()
                    if (cursor.getInt(0) == 0) {
                        throw IllegalStateException("删除记录失败")
                    }
                }
            }
        }
    }
    
    // TODO: 待开发的统计功能 - 用于统计记录总数
    suspend fun getRecordCount(): Int {
        return dao.getRecordCount()
    }
    
    fun getRecordsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<MenstrualRecord>> {
        return dao.getRecordsByMonth(startOfMonth, endOfMonth).flowOn(Dispatchers.IO)
    }
    
    suspend fun getRecordsBeforeDate(beforeDate: Date, pageSize: Int): List<MenstrualRecord> {
        return dao.getRecordsBeforeDate(beforeDate, pageSize)
    }
    
    suspend fun getRecordsCountBeforeDate(date: Date): Int {
        return dao.getRecordsCountBeforeDate(date)
    }
}