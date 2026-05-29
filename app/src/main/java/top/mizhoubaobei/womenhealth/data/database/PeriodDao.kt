package top.mizhoubaobei.womenhealth.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_records ORDER BY startDate DESC")
    fun getAllRecordsFlow(): Flow<List<PeriodRecord>>

    @Query("SELECT * FROM period_records ORDER BY startDate DESC")
    suspend fun getAllRecordsDirect(): List<PeriodRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PeriodRecord)

    @Update
    suspend fun update(record: PeriodRecord)

    @Delete
    suspend fun delete(record: PeriodRecord)

    @Query("SELECT * FROM period_records WHERE id = :id")
    fun getRecordById(id: Int): Flow<PeriodRecord?>
}
