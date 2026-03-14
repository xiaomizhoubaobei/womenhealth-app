package top.mizhoubaobei.womenhealth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * 宫颈记录实体类
 * 记录宫颈位置、开口、质地等周期性变化
 */
@Entity(tableName = "cervical_records")
data class CervicalRecord(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    
    /** 记录日期 */
    @ColumnInfo(name = "record_date")
    var recordDate: Date,
    
    /** 宫颈位置 (高位/中位/低位) */
    @ColumnInfo(name = "position")
    var position: CervicalPosition = CervicalPosition.MEDIUM,
    
    /** 宫颈开口 (闭合/微开/半开/全开) */
    @ColumnInfo(name = "opening")
    var opening: CervicalOpening = CervicalOpening.CLOSED,
    
    /** 宫颈质地 (软/中/硬) */
    @ColumnInfo(name = "texture")
    var texture: CervicalTexture = CervicalTexture.MEDIUM,
    
    /** 宫颈粘液描述 */
    @ColumnInfo(name = "mucus_description")
    var mucusDescription: String? = null,
    
    /** 宫颈粘液透明度 (清澈/乳白/浑浊) */
    @ColumnInfo(name = "mucus_clarity")
    var mucusClarity: MucusClarity = MucusClarity.CLEAR,
    
    /** 宫颈粘液拉丝度 (无/短/中/长) */
    @ColumnInfo(name = "mucus_stretchiness")
    var mucusStretchiness: MucusStretchiness = MucusStretchiness.NONE,
    
    /** 估计排卵概率 (0-100) */
    @ColumnInfo(name = "ovulation_probability")
    var ovulationProbability: Int = 0,
    
    /** 基础体温 */
    @ColumnInfo(name = "basal_temperature")
    var basalTemperature: Float? = null,
    
    /** 是否有性交疼痛 */
    @ColumnInfo(name = "intercourse_pain")
    var intercoursePain: Boolean = false,
    
    /** 其他观察 */
    @ColumnInfo(name = "observations")
    var observations: String? = null,
    
    /** 关联的生理期记录ID */
    @ColumnInfo(name = "period_record_id")
    var periodRecordId: Long? = null,
    
    override var createdAt: Date = Date(),
    override var updatedAt: Date = Date(),
    override var syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity() {
    
    /**
     * 宫颈位置枚举
     */
    enum class CervicalPosition(val displayName: String) {
        HIGH("高位"),     // 手指难以触及
        MEDIUM("中位"),   // 手指刚好触及
        LOW("低位")       // 手指容易触及甚至突出
    }
    
    /**
     * 宫颈开口枚举
     */
    enum class CervicalOpening(val displayName: String) {
        CLOSED("闭合"),
        SLIGHTLY_OPEN("微开"),
        HALF_OPEN("半开"),
        FULLY_OPEN("全开")
    }
    
    /**
     * 宫颈质地枚举
     */
    enum class CervicalTexture(val displayName: String) {
        SOFT("软"),
        MEDIUM("中"),
        HARD("硬")
    }
    
    /**
     * 粘液透明度枚举
     */
    enum class MucusClarity(val displayName: String) {
        CLEAR("清澈"),
        WHITE("乳白"),
        CLOUDY("浑浊")
    }
    
    /**
     * 粘液拉丝度枚举
     */
    enum class MucusStretchiness(val displayName: String) {
        NONE("无"),
        SHORT("短"),
        MEDIUM("中"),
        LONG("长")
    }
    
    /**
     * 计算生育力指数 (0-100)
     */
    fun calculateFertilityIndex(): Int {
        var index = 0
        
        // 位置评分
        index += when (position) {
            CervicalPosition.HIGH -> 10
            CervicalPosition.MEDIUM -> 30
            CervicalPosition.LOW -> 50
        }
        
        // 开口评分
        index += when (opening) {
            CervicalOpening.CLOSED -> 5
            CervicalOpening.SLIGHTLY_OPEN -> 25
            CervicalOpening.HALF_OPEN -> 45
            CervicalOpening.FULLY_OPEN -> 70
        }
        
        // 质地评分
        index += when (texture) {
            CervicalTexture.HARD -> 10
            CervicalTexture.MEDIUM -> 30
            CervicalTexture.SOFT -> 50
        }
        
        // 粘液质量评分
        index += when (mucusClarity) {
            MucusClarity.CLOUDY -> 5
            MucusClarity.WHITE -> 25
            MucusClarity.CLEAR -> 40
        }
        
        index += when (mucusStretchiness) {
            MucusStretchiness.NONE -> 5
            MucusStretchiness.SHORT -> 20
            MucusStretchiness.MEDIUM -> 35
            MucusStretchiness.LONG -> 50
        }
        
        return index.coerceIn(0, 100)
    }
    
    /**
     * 获取生育力描述
     */
    fun getFertilityDescription(): String {
        val index = calculateFertilityIndex()
        return when {
            index >= 80 -> "易孕期 - 最佳受孕时机"
            index >= 60 -> "较易孕期 - 受孕可能性较高"
            index >= 40 -> "一般期 - 受孕可能性一般"
            index >= 20 -> "不易孕期 - 受孕可能性较低"
            else -> "不易孕期 - 受孕可能性很低"
        }
    }
}