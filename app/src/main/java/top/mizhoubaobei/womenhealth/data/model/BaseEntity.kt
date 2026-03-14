package top.mizhoubaobei.womenhealth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 基础实体类，包含所有数据表共有的字段
 */
@Entity(tableName = "base_entity")
abstract class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
    var syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
}

/**
 * 数据同步状态枚举
 */
enum class SyncStatus {
    LOCAL_ONLY,     // 仅本地存在
    SYNCED,         // 已同步到云端
    PENDING_SYNC,   // 等待同步
    SYNC_FAILED     // 同步失败
}