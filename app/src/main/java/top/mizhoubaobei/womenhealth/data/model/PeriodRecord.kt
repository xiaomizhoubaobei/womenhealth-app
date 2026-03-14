package top.mizhoubaobei.womenhealth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * 生理期记录实体类
 * 记录月经开始/结束时间、流量、症状等信息
 */
@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    
    /** 月经开始日期 */
    @ColumnInfo(name = "start_date")
    var startDate: Date,
    
    /** 月经结束日期 */
    @ColumnInfo(name = "end_date")
    var endDate: Date?,
    
    /** 月经天数 */
    @ColumnInfo(name = "cycle_length")
    var cycleLength: Int? = null,
    
    /** 月经量等级 (1-5, 1为很少，5为很多) */
    @ColumnInfo(name = "flow_level")
    var flowLevel: Int = 3,
    
    /** 血块情况描述 */
    @ColumnInfo(name = "clots_description")
    var clotsDescription: String? = null,
    
    /** 痛经程度 (1-10) */
    @ColumnInfo(name = "pain_level")
    var painLevel: Int = 0,
    
    /** 是否使用止痛药 */
    @ColumnInfo(name = "used_medicine")
    var usedMedicine: Boolean = false,
    
    /** 伴随症状列表 */
    @ColumnInfo(name = "symptoms")
    var symptoms: String? = null, // JSON数组格式存储
    
    /** 情绪状态 */
    @ColumnInfo(name = "mood_state")
    var moodState: String? = null,
    
    /** 体力状态 (1-10) */
    @ColumnInfo(name = "energy_level")
    var energyLevel: Int = 5,
    
    /** 睡眠质量 (1-10) */
    @ColumnInfo(name = "sleep_quality")
    var sleepQuality: Int = 5,
    
    /** 笔记 */
    @ColumnInfo(name = "notes")
    var notes: String? = null,
    
    /** 预测的排卵期 */
    @ColumnInfo(name = "predicted_ovulation")
    var predictedOvulation: Date? = null,
    
    override var createdAt: Date = Date(),
    override var updatedAt: Date = Date(),
    override var syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity() {
    
    /**
     * 计算月经持续天数
     */
    fun getDurationDays(): Int {
        return if (endDate != null) {
            val diff = endDate!!.time - startDate.time
            (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        } else {
            1
        }
    }
    
    /**
     * 验证数据有效性
     */
    fun isValid(): Boolean {
        return startDate.before(Date()) && 
               (endDate == null || endDate!!.after(startDate)) &&
               flowLevel in 1..5 &&
               painLevel in 0..10 &&
               energyLevel in 1..10 &&
               sleepQuality in 1..10
    }
}