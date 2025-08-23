package top.mizhoubaobei.womenhealth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

/**
 * 月经记录数据模型
 */
@Entity(tableName = "menstrual_records", indices = [
    Index(value = ["startDate"], name = "idx_start_date"),
    Index(value = ["endDate"], name = "idx_end_date"),
    Index(value = ["createdAt"], name = "idx_created_at"),
    Index(value = ["updatedAt"], name = "idx_updated_at")
])
data class MenstrualRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 开始日期
    @ColumnInfo(index = true)
    val startDate: Date,
    
    // 结束日期（可选）
    val endDate: Date? = null,
    
    // 周期长度（天数）
    val cycleLength: Int? = null,
    
    // 经期长度（天数）
    val periodLength: Int? = null,
    
    // 流量等级 (1-轻微, 2-正常, 3-较多, 4-很多)
    val flowLevel: Int = 2,
    
    // 症状标签
    val symptoms: String? = null,
    
    // 备注
    val notes: String? = null,
    
    // 创建时间
    val createdAt: Date = Date(),
    
    // 更新时间
    val updatedAt: Date = Date()
) : Serializable

/**
 * 症状枚举
 */
enum class MenstrualSymptom(val displayName: String) {
    CRAMPS("痛经"),
    HEADACHE("头痛"),
    MOOD_SWINGS("情绪波动"),
    BLOATING("腹胀"),
    BREAST_TENDERNESS("乳房胀痛"),
    FATIGUE("疲劳"),
    NAUSEA("恶心"),
    BACK_PAIN("腰痛"),
    ACNE("痤疮"),
    FOOD_CRAVINGS("食欲变化")
}

/**
 * 流量等级枚举
 */
enum class FlowLevel(val level: Int, val displayName: String, val description: String) {
    LIGHT(1, "轻微", "护垫或少量卫生巾"),
    NORMAL(2, "正常", "普通卫生巾，3-4小时更换"),
    HEAVY(3, "较多", "大容量卫生巾，2-3小时更换"),
    VERY_HEAVY(4, "很多", "超大容量卫生巾，1-2小时更换")
}