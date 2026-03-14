package top.mizhoubaobei.womenhealth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * 孕期记录实体类
 * 记录孕周、产检、胎儿发育等信息
 */
@Entity(tableName = "pregnancy_records")
data class PregnancyRecord(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    
    /** 末次月经日期 */
    @ColumnInfo(name = "last_period_date")
    var lastPeriodDate: Date,
    
    /** 受孕日期 (如果有确切日期) */
    @ColumnInfo(name = "conception_date")
    var conceptionDate: Date? = null,
    
    /** 当前孕周 */
    @ColumnInfo(name = "current_week")
    var currentWeek: Int = 0,
    
    /** 当前孕天 */
    @ColumnInfo(name = "current_day")
    var currentDay: Int = 0,
    
    /** 预产期 */
    @ColumnInfo(name = "due_date")
    var dueDate: Date? = null,
    
    /** 胎儿数量 */
    @ColumnInfo(name = "fetus_count")
    var fetusCount: Int = 1,
    
    /** 超声检查记录 (JSON格式) */
    @ColumnInfo(name = "ultrasound_records")
    var ultrasoundRecords: String? = null,
    
    /** 体重记录 (JSON格式，包含日期和体重) */
    @ColumnInfo(name = "weight_records")
    var weightRecords: String? = null,
    
    /** 血压记录 (JSON格式) */
    @ColumnInfo(name = "blood_pressure_records")
    var bloodPressureRecords: String? = null,
    
    /** 胎动开始时间 */
    @ColumnInfo(name = "fetal_movement_start")
    var fetalMovementStart: Date? = null,
    
    /** 孕期症状记录 */
    @ColumnInfo(name = "pregnancy_symptoms")
    var pregnancySymptoms: String? = null,
    
    /** 产检计划 (JSON格式) */
    @ColumnInfo(name = "checkup_schedule")
    var checkupSchedule: String? = null,
    
    /** 孕期日记 */
    @ColumnInfo(name = "pregnancy_diary")
    var pregnancyDiary: String? = null,
    
    /** 营养补充记录 */
    @ColumnInfo(name = "nutrition_supplements")
    var nutritionSupplements: String? = null,
    
    /** 运动记录 */
    @ColumnInfo(name = "exercise_records")
    var exerciseRecords: String? = null,
    
    /** 高危因素 */
    @ColumnInfo(name = "risk_factors")
    var riskFactors: String? = null,
    
    /** 医生建议 */
    @ColumnInfo(name = "doctor_advice")
    var doctorAdvice: String? = null,
    
    /** 是否多胞胎 */
    @ColumnInfo(name = "is_multiple")
    var isMultiple: Boolean = false,
    
    /** 试管婴儿标识 */
    @ColumnInfo(name = "ivf_pregnancy")
    var ivfPregnancy: Boolean = false,
    
    override var createdAt: Date = Date(),
    override var updatedAt: Date = Date(),
    override var syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity() {
    
    /**
     * 计算当前孕周和孕天
     */
    fun calculateCurrentWeekAndDay(): Pair<Int, Int> {
        val today = Date()
        val diffTime = today.time - lastPeriodDate.time
        val diffDays = (diffTime / (1000 * 60 * 60 * 24)).toInt()
        val weeks = diffDays / 7
        val days = diffDays % 7
        return Pair(weeks, days)
    }
    
    /**
     * 计算预产期
     */
    fun calculateDueDate(): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = lastPeriodDate
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 280) // 280天 = 40周
        return calendar.time
    }
    
    /**
     * 验证数据有效性
     */
    fun isValid(): Boolean {
        return lastPeriodDate.before(Date()) &&
               currentWeek >= 0 && currentWeek <= 42 &&
               currentDay >= 0 && currentDay <= 6 &&
               fetusCount > 0
    }
    
    /**
     * 获取孕期阶段描述
     */
    fun getPregnancyStage(): String {
        return when {
            currentWeek < 12 -> "孕早期"
            currentWeek < 28 -> "孕中期"
            currentWeek <= 42 -> "孕晚期"
            else -> "已过期"
        }
    }
}