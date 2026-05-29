package top.mizhoubaobei.womenhealth.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: String, // format "yyyy-MM-dd"
    val endDate: String?,  // format "yyyy-MM-dd" or null if ongoing
    val flow: String,      // "极少" (Very Light), "较少" (Light), "正常" (Medium), "较多" (Heavy)
    val symptoms: String,  // Comma-separated: "痛经", "头痛", "腹胀", "乳房胀痛", "粉刺", "无症状"
    val mood: String,      // Comma-separated: "平静", "敏感", "郁闷", "开朗", "疲惫", "焦虑"
    val notes: String      // Custom custom message
)
