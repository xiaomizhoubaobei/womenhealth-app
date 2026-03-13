# PMS 提醒功能开发文档

## 文档版本
- 版本：v1.0
- 创建日期：2026-02-14
- 文档类型：开发技术文档

---

## 1. 功能概述

PMS（经前综合征）提醒功能帮助用户在经前综合征高发期提前做好准备，通过智能预测和多类型提醒，减轻PMS症状对日常生活的影响。

### 核心价值
- **提前预警**：基于周期预测，提前通知用户PMS症状可能出现的时间
- **症状缓解**：在症状出现前提供护理建议和应对措施
- **生活指导**：提醒用户调整饮食、作息和生活习惯
- **心理支持**：提供情绪管理建议和减压方法
- **个性化定制**：根据用户症状历史提供个性化提醒

### 核心功能
- PMS症状预测与提醒
- 症状严重程度分级提醒
- 多维度提醒（饮食、运动、情绪、护理）
- 智能提醒时机调整
- 提醒内容个性化
- 提醒历史记录与统计分析

---

## 2. 数据库设计

### 2.1 PMS提醒配置表（pms_reminder_settings）

```kotlin
@Entity(tableName = "pms_reminder_settings")
data class PMSReminderSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val userId: Long = 1,
    val isEnabled: Boolean = true,           // 是否启用PMS提醒
    
    // 提醒时机设置
    val advanceDays: Int = 7,                // 提前提醒天数（3-14天）
    val reminderFrequency: String = "daily", // 提醒频率：daily/every2days/every3days
    
    // 提醒类型开关
    val symptomPredictionEnabled: Boolean = true,  // 症状预测提醒
    val dietReminderEnabled: Boolean = true,       // 饮食提醒
    val exerciseReminderEnabled: Boolean = true,   // 运动提醒
    val moodReminderEnabled: Boolean = true,       // 情绪管理提醒
    val careReminderEnabled: Boolean = true,       // 护理提醒
    val supplementReminderEnabled: Boolean = false, // 补充剂提醒
    
    // 提醒时间设置
    val preferredReminderTime: Long = 32400000,    // 偏好提醒时间（时间戳，默认9:00）
    
    // 症状严重程度阈值
    val mildSymptomThreshold: Int = 2,        // 轻度症状阈值
    val moderateSymptomThreshold: Int = 3,    // 中度症状阈值
    val severeSymptomThreshold: Int = 4,      // 重度症状阈值
    
    // 个性化设置
    val customReminders: String? = null,      // 自定义提醒内容（JSON）
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2.2 PMS提醒记录表（pms_reminder_history）

```kotlin
@Entity(tableName = "pms_reminder_history")
data class PMSReminderHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val cycleId: Long?,                      // 关联的月经周期ID
    val reminderDate: Long,                  // 提醒日期（时间戳）
    val reminderTime: Long,                  // 提醒时间（时间戳）
    
    // 提醒类型
    val reminderType: String,                // 提醒类型
    val reminderSubtype: String? = null,     // 提醒子类型
    
    // 提醒内容
    val reminderTitle: String,               // 提醒标题
    val reminderMessage: String,             // 提醒内容
    val actionSuggestion: String? = null,    // 建议行动
    
    // 症状预测
    val predictedSeverity: Int? = null,      // 预测症状严重程度（0-5）
    val predictedSymptoms: String? = null,   // 预测症状列表（JSON）
    
    // 状态
    val isDelivered: Boolean = false,        // 是否已送达
    val isRead: Boolean = false,             // 是否已读
    val isActionPerformed: Boolean = false,  // 是否执行了建议行动
    
    // 用户反馈
    val userRating: Int? = null,             // 用户评分（1-5）
    val userFeedback: String? = null,        // 用户反馈
    
    // 时间戳
    val deliveredAt: Long? = null,           // 送达时间
    val readAt: Long? = null,                // 阅读时间
    val actionPerformedAt: Long? = null,     // 行动执行时间
    
    val createdAt: Long = System.currentTimeMillis()
)
```

### 2.3 PMS症状预测表（pms_symptom_prediction）

```kotlin
@Entity(tableName = "pms_symptom_prediction")
data class PMSSymptomPrediction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val cycleId: Long,                       // 关联的月经周期ID
    val predictionDate: Long,                // 预测日期（时间戳）
    
    // 预测症状
    val moodSwingsPrediction: Int?,          // 情绪波动预测（0-5）
    val irritabilityPrediction: Int?,        // 易怒预测（0-5）
    val anxietyPrediction: Int?,             // 焦虑预测（0-5）
    val depressionPrediction: Int?,          // 抑郁预测（0-5）
    val abdominalPainPrediction: Int?,       // 腹痛预测（0-5）
    val headachePrediction: Int?,            // 头痛预测（0-5）
    val breastTendernessPrediction: Int?,    // 乳房胀痛预测（0-5）
    val bloatingPrediction: Int?,            // 腹胀预测（0-5）
    val fatiguePrediction: Int?,             // 疲劳预测（0-5）
    val sleepDisturbancePrediction: Int?,    // 睡眠障碍预测（0-5）
    
    // 预测依据
    val predictionConfidence: Float,         // 预测置信度（0.0-1.0）
    val referenceCycles: String,             // 参考周期ID列表（JSON）
    
    // 整体评估
    val overallSeverity: Int,                // 整体严重程度（0-5）
    val pmsType: String,                     // PMS类型：mild/moderate/severe/pmdd
    
    // 建议
    val recommendations: String,             // 建议内容（JSON）
    
    // 验证
    val actualSeverity: Int? = null,         // 实际严重程度（用户记录后更新）
    val predictionAccuracy: Float? = null,   // 预测准确率
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 3. 提醒类型设计

### 3.1 症状预测提醒

**提醒时机**：PMS症状预测日期前1天

**提醒内容**：
- 预测症状类型和严重程度
- 预计持续时间
- 历史症状对比
- 应对建议

**示例**：
```
⚠️ PMS症状预测提醒

根据您的周期历史，预测明天可能出现PMS症状：
• 情绪波动：中度（3/5）
• 腹痛：轻度（2/5）
• 疲劳：中度（3/5）

建议提前准备：
✓ 准备缓解痛经的药物
✓ 安排充足的休息时间
✓ 调整工作计划，避免高压任务

[查看详细应对指南]
```

### 3.2 饮食提醒

**提醒时机**：PMS症状出现期间，每日定时提醒

**提醒内容**：
- 推荐食物（补充营养、缓解症状）
- 避免食物（可能加重症状）
- 饮食建议

**示例**：
```
🥗 PMS饮食建议

今天的饮食建议：
✓ 推荐食物：
  - 富含镁的食物：坚果、香蕉、全谷物
  - 富含钙的食物：牛奶、酸奶、绿叶蔬菜
  - 富含维生素B6的食物：鸡肉、鱼、土豆

✗ 避免食物：
  - 高盐食物（可能加重腹胀）
  - 咖啡因（可能加重焦虑）
  - 高糖食物（可能引起血糖波动）

[查看更多食谱]
```

### 3.3 运动提醒

**提醒时机**：PMS症状出现期间，根据症状严重程度调整频率

**提醒内容**：
- 推荐运动类型
- 运动强度建议
- 运动时长建议

**示例**：
```
🏃‍♀️ PMS运动建议

今天的运动建议：
• 类型：轻度瑜伽或散步
• 强度：低强度
• 时长：20-30分钟
• 时间：下午或傍晚

轻度运动有助于：
✓ 缓解情绪波动
✓ 减轻腹痛
✓ 改善睡眠质量

[开始运动记录]
```

### 3.4 情绪管理提醒

**提醒时机**：PMS症状出现期间，每日2次（上午和下午）

**提醒内容**：
- 情绪状态提示
- 情绪管理技巧
- 冥想或呼吸练习建议

**示例**：
```
🧘 PMS情绪管理

情绪状态提示：
• 今天可能出现情绪波动，请保持耐心

情绪管理技巧：
✓ 深呼吸练习（4-7-8呼吸法）
✓ 正念冥想5分钟
✓ 写情绪日记
✓ 与信任的人聊天

[开始冥想练习] [情绪日记]
```

### 3.5 护理提醒

**提醒时机**：根据具体症状类型触发

**提醒内容**：
- 症状护理方法
- 物理治疗建议
- 药物使用提醒

**示例**：
```
💊 PMS护理提醒

腹痛护理建议：
✓ 热敷：使用热水袋或热毛巾敷在小腹
✓ 按摩：轻柔按摩下腹部
✓ 药物：如需要，可服用止痛药（请遵医嘱）

乳房胀痛护理：
✓ 选择支撑性好的内衣
✓ 避免剧烈运动
✓ 冷敷或热敷均可缓解

[查看更多护理方法]
```

### 3.6 补充剂提醒

**提醒时机**：每日定时提醒

**提醒内容**：
- 推荐补充剂（钙、镁、维生素B6等）
- 服用剂量
- 服用时间

**示例**：
```
💊 补充剂提醒

今日补充剂：
• 钙片：500mg，睡前服用
• 镁片：250mg，餐后服用
• 维生素B6：50mg，早餐后服用

注意事项：
✓ 请遵医嘱服用
✓ 不要超量服用
✓ 如有不适请立即停止

[标记已服用]
```

---

## 4. DAO 接口设计

```kotlin
@Dao
interface PMSReminderSettingsDao {
    
    // 基础 CRUD 操作
    @Query("SELECT * FROM pms_reminder_settings WHERE userId = :userId")
    suspend fun getSettingsByUserId(userId: Long): PMSReminderSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PMSReminderSettings): Long

    @Update
    suspend fun updateSettings(settings: PMSReminderSettings)

    @Query("UPDATE pms_reminder_settings SET isEnabled = :isEnabled WHERE userId = :userId")
    suspend fun updateReminderEnabled(userId: Long, isEnabled: Boolean)
}

@Dao
interface PMSReminderHistoryDao {
    
    // 基础 CRUD 操作
    @Insert
    suspend fun insertReminder(reminder: PMSReminderHistory): Long

    @Update
    suspend fun updateReminder(reminder: PMSReminderHistory)

    @Query("SELECT * FROM pms_reminder_history WHERE id = :id")
    suspend fun getReminderById(id: Long): PMSReminderHistory?

    @Query("SELECT * FROM pms_reminder_history WHERE cycleId = :cycleId ORDER BY reminderDate DESC")
    fun getRemindersByCycle(cycleId: Long): Flow<List<PMSReminderHistory>>

    @Query("SELECT * FROM pms_reminder_history WHERE reminderDate >= :startDate AND reminderDate <= :endDate ORDER BY reminderDate DESC")
    fun getRemindersByDateRange(startDate: Long, endDate: Long): Flow<List<PMSReminderHistory>>

    @Query("SELECT * FROM pms_reminder_history WHERE reminderDate = :date")
    suspend fun getRemindersByDate(date: Long): List<PMSReminderHistory>

    @Query("UPDATE pms_reminder_history SET isDelivered = :isDelivered, deliveredAt = :deliveredAt WHERE id = :id")
    suspend fun updateReminderDelivered(id: Long, isDelivered: Boolean, deliveredAt: Long)

    @Query("UPDATE pms_reminder_history SET isRead = :isRead, readAt = :readAt WHERE id = :id")
    suspend fun updateReminderRead(id: Long, isRead: Boolean, readAt: Long)

    @Query("UPDATE pms_reminder_history SET isActionPerformed = :isActionPerformed, actionPerformedAt = :performedAt WHERE id = :id")
    suspend fun updateReminderActionPerformed(id: Long, isActionPerformed: Boolean, performedAt: Long)

    @Query("SELECT COUNT(*) FROM pms_reminder_history WHERE reminderType = :type AND reminderDate >= :startDate AND reminderDate <= :endDate")
    suspend fun getReminderCountByType(type: String, startDate: Long, endDate: Long): Int

    @Query("SELECT AVG(userRating) FROM pms_reminder_history WHERE userRating IS NOT NULL")
    suspend fun getAverageUserRating(): Float?

    @Query("DELETE FROM pms_reminder_history WHERE reminderDate < :date")
    suspend fun deleteRemindersBeforeDate(date: Long)
}

@Dao
interface PMSSymptomPredictionDao {
    
    // 基础 CRUD 操作
    @Insert
    suspend fun insertPrediction(prediction: PMSSymptomPrediction): Long

    @Update
    suspend fun updatePrediction(prediction: PMSSymptomPrediction)

    @Query("SELECT * FROM pms_symptom_prediction WHERE id = :id")
    suspend fun getPredictionById(id: Long): PMSSymptomPrediction?

    @Query("SELECT * FROM pms_symptom_prediction WHERE cycleId = :cycleId")
    suspend fun getPredictionsByCycle(cycleId: Long): List<PMSSymptomPrediction>

    @Query("SELECT * FROM pms_symptom_prediction WHERE predictionDate = :date")
    suspend fun getPredictionByDate(date: Long): PMSSymptomPrediction?

    @Query("SELECT * FROM pms_symptom_prediction WHERE predictionDate >= :startDate AND predictionDate <= :endDate ORDER BY predictionDate DESC")
    suspend fun getPredictionsInRange(startDate: Long, endDate: Long): List<PMSSymptomPrediction>

    @Query("UPDATE pms_symptom_prediction SET actualSeverity = :actualSeverity, predictionAccuracy = :accuracy WHERE id = :id")
    suspend fun updatePredictionAccuracy(id: Long, actualSeverity: Int, accuracy: Float)

    @Query("SELECT AVG(predictionAccuracy) FROM pms_symptom_prediction WHERE predictionAccuracy IS NOT NULL")
    suspend fun getAveragePredictionAccuracy(): Float?

    @Query("SELECT * FROM pms_symptom_prediction WHERE overallSeverity >= :severity ORDER BY predictionDate DESC LIMIT 10")
    suspend fun getSeverePredictions(severity: Int): List<PMSSymptomPrediction>

    @Query("SELECT * FROM pms_symptom_prediction WHERE pmsType = :pmsType ORDER BY predictionDate DESC LIMIT 10")
    suspend fun getPredictionsByType(pmsType: String): List<PMSSymptomPrediction>
}
```

---

## 5. Repository 层实现

```kotlin
class PMSReminderRepository(
    private val pmsReminderSettingsDao: PMSReminderSettingsDao,
    private val pmsReminderHistoryDao: PMSReminderHistoryDao,
    private val pmsSymptomPredictionDao: PMSSymptomPredictionDao,
    private val menstrualCycleDao: MenstrualCycleDao,
    private val pmsSymptomDao: PMSSymptomDao
) {
    
    // ============ 提醒设置管理 ============
    
    suspend fun getReminderSettings(): PMSReminderSettings? {
        return pmsReminderSettingsDao.getSettingsByUserId(1L)
    }
    
    suspend fun saveReminderSettings(settings: PMSReminderSettings): Long {
        return pmsReminderSettingsDao.insertSettings(settings)
    }
    
    suspend fun updateReminderSettings(settings: PMSReminderSettings) {
        pmsReminderSettingsDao.updateSettings(settings)
    }
    
    suspend fun toggleReminderEnabled(enabled: Boolean) {
        pmsReminderSettingsDao.updateReminderEnabled(1L, enabled)
    }
    
    // ============ 提醒记录管理 ============
    
    suspend fun createReminder(reminder: PMSReminderHistory): Long {
        return pmsReminderHistoryDao.insertReminder(reminder)
    }
    
    fun getRemindersByCycle(cycleId: Long): Flow<List<PMSReminderHistory>> {
        return pmsReminderHistoryDao.getRemindersByCycle(cycleId)
    }
    
    fun getRemindersByDateRange(startDate: Long, endDate: Long): Flow<List<PMSReminderHistory>> {
        return pmsReminderHistoryDao.getRemindersByDateRange(startDate, endDate)
    }
    
    suspend fun getTodayReminders(): List<PMSReminderHistory> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return pmsReminderHistoryDao.getRemindersByDate(today)
    }
    
    suspend fun markReminderDelivered(reminderId: Long) {
        pmsReminderHistoryDao.updateReminderDelivered(reminderId, true, System.currentTimeMillis())
    }
    
    suspend fun markReminderRead(reminderId: Long) {
        pmsReminderHistoryDao.updateReminderRead(reminderId, true, System.currentTimeMillis())
    }
    
    suspend fun markReminderActionPerformed(reminderId: Long) {
        pmsReminderHistoryDao.updateReminderActionPerformed(reminderId, true, System.currentTimeMillis())
    }
    
    // ============ 症状预测管理 ============
    
    suspend fun createPrediction(prediction: PMSSymptomPrediction): Long {
        return pmsSymptomPredictionDao.insertPrediction(prediction)
    }
    
    suspend fun getPredictionByCycle(cycleId: Long): List<PMSSymptomPrediction> {
        return pmsSymptomPredictionDao.getPredictionsByCycle(cycleId)
    }
    
    suspend fun getPredictionByDate(date: Long): PMSSymptomPrediction? {
        return pmsSymptomPredictionDao.getPredictionByDate(date)
    }
    
    suspend fun updatePredictionAccuracy(predictionId: Long, actualSeverity: Int) {
        val prediction = pmsSymptomPredictionDao.getPredictionById(predictionId) ?: return
        val accuracy = 1f - Math.abs(prediction.overallSeverity - actualSeverity) / 5f
        pmsSymptomPredictionDao.updatePredictionAccuracy(predictionId, actualSeverity, accuracy)
    }
    
    suspend fun getAveragePredictionAccuracy(): Float? {
        return pmsSymptomPredictionDao.getAveragePredictionAccuracy()
    }
    
    // ============ 智能预测逻辑 ============
    
    /**
     * 预测下一个周期的PMS症状
     */
    suspend fun predictNextCyclePMS(): PMSSymptomPrediction? {
        // 获取最近的周期
        val recentCycles = menstrualCycleDao.getRecentCycles()
        if (recentCycles.size < 3) return null
        
        val latestCycle = recentCycles.first()
        val nextCycleStartDate = latestCycle.startDate + calculateAverageCycleLength(recentCycles) * 24 * 60 * 60 * 1000
        
        // 获取历史PMS症状数据
        val historicalSymptoms = mutableListOf<PMSSymptom>()
        recentCycles.forEach { cycle ->
            val symptoms = pmsSymptomDao.getSymptomsByCycle(cycle.cycleId).first()
            historicalSymptoms.addAll(symptoms)
        }
        
        if (historicalSymptoms.isEmpty()) return null
        
        // 计算预测值（基于历史平均值）
        val prediction = PMSSymptomPrediction(
            cycleId = latestCycle.cycleId,
            predictionDate = nextCycleStartDate - 7 * 24 * 60 * 60 * 1000, // PMS通常在经期前7天
            moodSwingsPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.moodSwings }),
            irritabilityPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.irritability }),
            anxietyPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.anxiety }),
            depressionPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.depression }),
            abdominalPainPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.abdominalPain }),
            headachePrediction = calculateAverage(historicalSymptoms.mapNotNull { it.headache }),
            breastTendernessPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.breastTenderness }),
            bloatingPrediction = calculateAverage(historicalSymptoms.mapNotNull { it.bloating }),
            fatiguePrediction = calculateAverage(historicalSymptoms.mapNotNull { it.fatigue }),
            sleepDisturbancePrediction = calculateAverage(historicalSymptoms.mapNotNull { it.sleepDisturbance }),
            predictionConfidence = calculateConfidence(recentCycles.size),
            referenceCycles = recentCycles.map { it.cycleId }.toString(),
            overallSeverity = 0, // 将在下面计算
            pmsType = "",
            recommendations = generateRecommendations(historicalSymptoms)
        )
        
        // 计算整体严重程度
        val severityValues = listOfNotNull(
            prediction.moodSwingsPrediction,
            prediction.irritabilityPrediction,
            prediction.anxietyPrediction,
            prediction.depressionPrediction,
            prediction.abdominalPainPrediction,
            prediction.headachePrediction,
            prediction.breastTendernessPrediction,
            prediction.bloatingPrediction,
            prediction.fatiguePrediction,
            prediction.sleepDisturbancePrediction
        )
        prediction.overallSeverity = calculateAverage(severityValues).toInt()
        
        // 确定PMS类型
        prediction.pmsType = when (prediction.overallSeverity) {
            in 0..2 -> "mild"
            in 3..4 -> "moderate"
            5 -> "severe"
            else -> "unknown"
        }
        
        return prediction
    }
    
    private fun calculateAverage(values: List<Int>): Int {
        if (values.isEmpty()) return 0
        return values.average().toInt()
    }
    
    private fun calculateAverageCycleLength(cycles: List<MenstrualCycle>): Int {
        return cycles.map { it.cycleLength }.average().toInt()
    }
    
    private fun calculateConfidence(cycleCount: Int): Float {
        // 基于历史数据量计算置信度
        return when (cycleCount) {
            1, 2 -> 0.5f
            3, 4 -> 0.7f
            5, 6 -> 0.85f
            else -> 0.9f
        }
    }
    
    private fun generateRecommendations(symptoms: List<PMSSymptom>): String {
        val recommendations = mutableListOf<String>()
        
        val avgMood = symptoms.mapNotNull { it.moodSwings }.average()
        val avgPain = symptoms.mapNotNull { it.abdominalPain }.average()
        val avgFatigue = symptoms.mapNotNull { it.fatigue }.average()
        
        if (avgMood >= 3) {
            recommendations.add("情绪管理：进行冥想或深呼吸练习")
        }
        if (avgPain >= 3) {
            recommendations.add("疼痛缓解：准备热敷和止痛药")
        }
        if (avgFatigue >= 3) {
            recommendations.add("休息调整：保证充足睡眠，减少高强度活动")
        }
        
        return recommendations.toString()
    }
    
    // ============ 提醒内容生成 ============
    
    /**
     * 生成症状预测提醒
     */
    suspend fun generateSymptomPredictionReminder(prediction: PMSSymptomPrediction): PMSReminderHistory {
        val symptoms = mutableListOf<String>()
        prediction.moodSwingsPrediction?.let { symptoms.add("情绪波动：${getSeverityLabel(it)}") }
        prediction.abdominalPainPrediction?.let { symptoms.add("腹痛：${getSeverityLabel(it)}") }
        prediction.fatiguePrediction?.let { symptoms.add("疲劳：${getSeverityLabel(it)}") }
        
        return PMSReminderHistory(
            cycleId = prediction.cycleId,
            reminderDate = prediction.predictionDate,
            reminderTime = System.currentTimeMillis(),
            reminderType = "symptom_prediction",
            reminderTitle = "⚠️ PMS症状预测提醒",
            reminderMessage = buildString {
                appendLine("根据您的周期历史，预测可能出现PMS症状：")
                symptoms.forEach { appendLine("• $it") }
                appendLine()
                appendLine("预计持续时间：3-7天")
            },
            predictedSeverity = prediction.overallSeverity,
            predictedSymptoms = symptoms.toString(),
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成饮食提醒
     */
    fun generateDietReminder(severity: Int): PMSReminderHistory {
        val (recommended, avoid) = when (severity) {
            1, 2 -> getLightDietRecommendations()
            3, 4 -> getModerateDietRecommendations()
            5 -> getSevereDietRecommendations()
            else -> getLightDietRecommendations()
        }
        
        return PMSReminderHistory(
            cycleId = null,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "diet",
            reminderSubtype = "nutrition",
            reminderTitle = "🥗 PMS饮食建议",
            reminderMessage = buildString {
                appendLine("今天的饮食建议：")
                appendLine()
                appendLine("✓ 推荐食物：")
                recommended.forEach { appendLine("  - $it") }
                appendLine()
                appendLine("✗ 避免食物：")
                avoid.forEach { appendLine("  - $it") }
            },
            predictedSeverity = severity,
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成运动提醒
     */
    fun generateExerciseReminder(severity: Int): PMSReminderHistory {
        val (type, intensity, duration) = when (severity) {
            1, 2 -> Triple("瑜伽或散步", "低强度", "30-45分钟")
            3, 4 -> Triple("轻度拉伸或散步", "极低强度", "20-30分钟")
            5 -> Triple("休息为主", "休息", "无")
            else -> Triple("瑜伽或散步", "低强度", "30-45分钟")
        }
        
        return PMSReminderHistory(
            cycleId = null,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "exercise",
            reminderSubtype = "physical_activity",
            reminderTitle = "🏃‍♀️ PMS运动建议",
            reminderMessage = buildString {
                appendLine("今天的运动建议：")
                appendLine("• 类型：$type")
                appendLine("• 强度：$intensity")
                appendLine("• 时长：$duration")
                appendLine()
                if (severity <= 4) {
                    appendLine("轻度运动有助于：")
                    appendLine("✓ 缓解情绪波动")
                    appendLine("✓ 减轻腹痛")
                    appendLine("✓ 改善睡眠质量")
                }
            },
            predictedSeverity = severity,
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成情绪管理提醒
     */
    fun generateMoodReminder(): PMSReminderHistory {
        return PMSReminderHistory(
            cycleId = null,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "mood",
            reminderSubtype = "emotional_support",
            reminderTitle = "🧘 PMS情绪管理",
            reminderMessage = buildString {
                appendLine("情绪状态提示：")
                appendLine("• 今天可能出现情绪波动，请保持耐心")
                appendLine()
                appendLine("情绪管理技巧：")
                appendLine("✓ 深呼吸练习（4-7-8呼吸法）")
                appendLine("✓ 正念冥想5分钟")
                appendLine("✓ 写情绪日记")
                appendLine("✓ 与信任的人聊天")
            },
            predictedSeverity = 3,
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成护理提醒
     */
    fun generateCareReminder(symptomType: String): PMSReminderHistory {
        val careAdvice = when (symptomType) {
            "abdominal_pain" -> getAbdominalPainCareAdvice()
            "breast_tenderness" -> getBreastTendernessCareAdvice()
            "headache" -> getHeadacheCareAdvice()
            else -> getGeneralCareAdvice()
        }
        
        return PMSReminderHistory(
            cycleId = null,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "care",
            reminderSubtype = symptomType,
            reminderTitle = "💊 PMS护理提醒",
            reminderMessage = careAdvice,
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    // ============ 辅助方法 ============
    
    private fun getSeverityLabel(severity: Int): String {
        return when (severity) {
            1 -> "轻度（1/5）"
            2 -> "轻度（2/5）"
            3 -> "中度（3/5）"
            4 -> "重度（4/5）"
            5 -> "严重（5/5）"
            else -> "无（0/5）"
        }
    }
    
    private fun getLightDietRecommendations(): Pair<List<String>, List<String>> {
        val recommended = listOf(
            "富含镁的食物：坚果、香蕉、全谷物",
            "富含钙的食物：牛奶、酸奶、绿叶蔬菜",
            "富含维生素B6的食物：鸡肉、鱼、土豆"
        )
        val avoid = listOf(
            "高盐食物（可能加重腹胀）",
            "咖啡因（可能加重焦虑）",
            "高糖食物（可能引起血糖波动）"
        )
        return Pair(recommended, avoid)
    }
    
    private fun getModerateDietRecommendations(): Pair<List<String>, List<String>> {
        val recommended = listOf(
            "富含镁的食物：坚果、香蕉、全谷物",
            "富含钙的食物：牛奶、酸奶、绿叶蔬菜",
            "富含维生素B6的食物：鸡肉、鱼、土豆",
            "富含铁的食物：红肉、菠菜、豆类"
        )
        val avoid = listOf(
            "高盐食物（可能加重腹胀）",
            "咖啡因（可能加重焦虑）",
            "高糖食物（可能引起血糖波动）",
            "辛辣食物（可能加重腹痛）"
        )
        return Pair(recommended, avoid)
    }
    
    private fun getSevereDietRecommendations(): Pair<List<String>, List<String>> {
        val recommended = listOf(
            "富含镁的食物：坚果、香蕉、全谷物",
            "富含钙的食物：牛奶、酸奶、绿叶蔬菜",
            "富含维生素B6的食物：鸡肉、鱼、土豆",
            "富含铁的食物：红肉、菠菜、豆类",
            "富含Omega-3的食物：鱼类、亚麻籽、核桃"
        )
        val avoid = listOf(
            "高盐食物（可能加重腹胀）",
            "咖啡因（可能加重焦虑）",
            "高糖食物（可能引起血糖波动）",
            "辛辣食物（可能加重腹痛）",
            "酒精（可能加重症状）"
        )
        return Pair(recommended, avoid)
    }
    
    private fun getAbdominalPainCareAdvice(): String {
        return buildString {
            appendLine("腹痛护理建议：")
            appendLine("✓ 热敷：使用热水袋或热毛巾敷在小腹")
            appendLine("✓ 按摩：轻柔按摩下腹部")
            appendLine("✓ 药物：如需要，可服用止痛药（请遵医嘱）")
            appendLine()
            appendLine("乳房胀痛护理：")
            appendLine("✓ 选择支撑性好的内衣")
            appendLine("✓ 避免剧烈运动")
            appendLine("✓ 冷敷或热敷均可缓解")
        }
    }
    
    private fun getBreastTendernessCareAdvice(): String {
        return buildString {
            appendLine("乳房胀痛护理：")
            appendLine("✓ 选择支撑性好的内衣")
            appendLine("✓ 避免剧烈运动")
            appendLine("✓ 冷敷或热敷均可缓解")
            appendLine("✓ 减少咖啡因摄入")
            appendLine("✓ 服用维生素B6和镁补充剂（请遵医嘱）")
        }
    }
    
    private fun getHeadacheCareAdvice(): String {
        return buildString {
            appendLine("头痛护理建议：")
            appendLine("✓ 休息：在安静、黑暗的房间休息")
            appendLine("✓ 冷敷：在额头或颈部冷敷")
            appendLine("✓ 按摩：轻柔按摩太阳穴和颈部")
            appendLine("✓ 补水：充分饮水")
            appendLine("✓ 药物：如需要，可服用止痛药（请遵医嘱）")
        }
    }
    
    private fun getGeneralCareAdvice(): String {
        return buildString {
            appendLine("PMS综合护理建议：")
            appendLine("✓ 充足休息：保证7-8小时睡眠")
            appendLine("✓ 规律作息：保持规律的作息时间")
            appendLine("✓ 适度运动：进行轻度运动如散步、瑜伽")
            appendLine("✓ 压力管理：学习放松技巧，减轻压力")
            appendLine("✓ 营养均衡：保持健康的饮食习惯")
        }
    }
}
```

---

## 6. ViewModel 层实现

```kotlin
class PMSReminderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: PMSReminderRepository
    
    // 提醒设置
    private val _reminderSettings = MutableStateFlow<PMSReminderSettings?>(null)
    val reminderSettings: StateFlow<PMSReminderSettings?> = _reminderSettings.asStateFlow()
    
    // 提醒历史
    private val _reminderHistory = MutableStateFlow<List<PMSReminderHistory>>(emptyList())
    val reminderHistory: StateFlow<List<PMSReminderHistory>> = _reminderHistory.asStateFlow()
    
    // 今日提醒
    private val _todayReminders = MutableStateFlow<List<PMSReminderHistory>>(emptyList())
    val todayReminders: StateFlow<List<PMSReminderHistory>> = _todayReminders.asStateFlow()
    
    // 症状预测
    private val _symptomPrediction = MutableStateFlow<PMSSymptomPrediction?>(null)
    val symptomPrediction: StateFlow<PMSSymptomPrediction?> = _symptomPrediction.asStateFlow()
    
    // UI 状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        val database = WomenHealthDatabase.getDatabase(application)
        repository = PMSReminderRepository(
            pmsReminderSettingsDao = database.pmsReminderSettingsDao(),
            pmsReminderHistoryDao = database.pmsReminderHistoryDao(),
            pmsSymptomPredictionDao = database.pmsSymptomPredictionDao(),
            menstrualCycleDao = database.menstrualCycleDao(),
            pmsSymptomDao = database.pmsSymptomDao()
        )
        
        loadReminderSettings()
        loadTodayReminders()
    }
    
    // ============ 提醒设置管理 ============
    
    private fun loadReminderSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val settings = repository.getReminderSettings()
                if (settings == null) {
                    // 创建默认设置
                    val defaultSettings = PMSReminderSettings(userId = 1L)
                    repository.saveReminderSettings(defaultSettings)
                    _reminderSettings.value = defaultSettings
                } else {
                    _reminderSettings.value = settings
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }
    
    fun updateReminderSettings(settings: PMSReminderSettings) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateReminderSettings(settings)
                _reminderSettings.value = settings
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }
    
    fun toggleReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleReminderEnabled(enabled)
                _reminderSettings.value?.let { settings ->
                    _reminderSettings.value = settings.copy(isEnabled = enabled)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // ============ 提醒历史管理 ============
    
    private fun loadTodayReminders() {
        viewModelScope.launch {
            try {
                val reminders = repository.getTodayReminders()
                _todayReminders.value = reminders
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun loadRemindersByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                repository.getRemindersByDateRange(startDate, endDate).collect { reminders ->
                    _reminderHistory.value = reminders
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // ============ 症状预测管理 ============
    
    fun predictNextCyclePMS() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val prediction = repository.predictNextCyclePMS()
                if (prediction != null) {
                    repository.createPrediction(prediction)
                    _symptomPrediction.value = prediction
                    generateRemindersForPrediction(prediction)
                }
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }
    
    private fun generateRemindersForPrediction(prediction: PMSSymptomPrediction) {
        viewModelScope.launch {
            try {
                // 生成症状预测提醒
                val symptomReminder = repository.generateSymptomPredictionReminder(prediction)
                repository.createReminder(symptomReminder)
                
                // 根据预测严重程度生成其他提醒
                val severity = prediction.overallSeverity
                val settings = _reminderSettings.value ?: return@launch
                
                if (settings.dietReminderEnabled) {
                    val dietReminder = repository.generateDietReminder(severity)
                    repository.createReminder(dietReminder)
                }
                
                if (settings.exerciseReminderEnabled) {
                    val exerciseReminder = repository.generateExerciseReminder(severity)
                    repository.createReminder(exerciseReminder)
                }
                
                if (settings.moodReminderEnabled) {
                    val moodReminder = repository.generateMoodReminder()
                    repository.createReminder(moodReminder)
                }
                
                if (settings.careReminderEnabled) {
                    // 根据主要症状生成护理提醒
                    val mainSymptom = when {
                        prediction.abdominalPainPrediction ?: 0 >= 3 -> "abdominal_pain"
                        prediction.breastTendernessPrediction ?: 0 >= 3 -> "breast_tenderness"
                        prediction.headachePrediction ?: 0 >= 3 -> "headache"
                        else -> "general"
                    }
                    val careReminder = repository.generateCareReminder(mainSymptom)
                    repository.createReminder(careReminder)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // ============ 提醒操作 ============
    
    fun markReminderAsRead(reminderId: Long) {
        viewModelScope.launch {
            try {
                repository.markReminderRead(reminderId)
                loadTodayReminders()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun markReminderActionPerformed(reminderId: Long) {
        viewModelScope.launch {
            try {
                repository.markReminderActionPerformed(reminderId)
                loadTodayReminders()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // ============ 清除错误 ============
    
    fun clearError() {
        _error.value = null
    }
}
```

---

## 7. WorkManager 提醒调度

### 7.1 PMS提醒Worker

```kotlin
class PMSReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val database = WomenHealthDatabase.getDatabase(context)
    private val repository = PMSReminderRepository(
        pmsReminderSettingsDao = database.pmsReminderSettingsDao(),
        pmsReminderHistoryDao = database.pmsReminderHistoryDao(),
        pmsSymptomPredictionDao = database.pmsSymptomPredictionDao(),
        menstrualCycleDao = database.menstrualCycleDao(),
        pmsSymptomDao = database.pmsSymptomDao()
    )
    
    override suspend fun doWork(): Result {
        return try {
            // 检查提醒设置
            val settings = repository.getReminderSettings()
            if (settings?.isEnabled != true) {
                return Result.success()
            }
            
            // 检查是否需要生成新的预测
            val latestPrediction = repository.getLatestPrediction()
            val shouldGenerateNewPrediction = shouldGeneratePrediction(latestPrediction)
            
            if (shouldGenerateNewPrediction) {
                // 生成新的症状预测
                val prediction = repository.predictNextCyclePMS()
                if (prediction != null) {
                    repository.createPrediction(prediction)
                    
                    // 生成提醒
                    generateReminders(prediction, settings)
                }
            }
            
            // 检查是否需要发送提醒
            val todayReminders = repository.getTodayReminders()
            todayReminders.forEach { reminder ->
                if (!reminder.isDelivered) {
                    sendReminder(reminder)
                    repository.markReminderDelivered(reminder.id)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun shouldGeneratePrediction(latestPrediction: PMSSymptomPrediction?): Boolean {
        if (latestPrediction == null) return true
        
        val predictionDate = latestPrediction.predictionDate
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // 如果预测日期已经过去7天以上，需要生成新的预测
        return (today - predictionDate) > 7 * 24 * 60 * 60 * 1000
    }
    
    private suspend fun generateReminders(
        prediction: PMSSymptomPrediction,
        settings: PMSReminderSettings
    ) {
        // 生成症状预测提醒
        val symptomReminder = repository.generateSymptomPredictionReminder(prediction)
        repository.createReminder(symptomReminder)
        
        // 根据设置生成其他提醒
        val severity = prediction.overallSeverity
        
        if (settings.dietReminderEnabled) {
            val dietReminder = repository.generateDietReminder(severity)
            repository.createReminder(dietReminder)
        }
        
        if (settings.exerciseReminderEnabled) {
            val exerciseReminder = repository.generateExerciseReminder(severity)
            repository.createReminder(exerciseReminder)
        }
        
        if (settings.moodReminderEnabled) {
            val moodReminder = repository.generateMoodReminder()
            repository.createReminder(moodReminder)
        }
        
        if (settings.careReminderEnabled) {
            val mainSymptom = when {
                prediction.abdominalPainPrediction ?: 0 >= 3 -> "abdominal_pain"
                prediction.breastTendernessPrediction ?: 0 >= 3 -> "breast_tenderness"
                prediction.headachePrediction ?: 0 >= 3 -> "headache"
                else -> "general"
            }
            val careReminder = repository.generateCareReminder(mainSymptom)
            repository.createReminder(careReminder)
        }
    }
    
    private fun sendReminder(reminder: PMSReminderHistory) {
        val context = applicationContext
        
        // 创建通知渠道
        val channelId = "pms_reminder_channel"
        val channelName = "PMS提醒"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "PMS症状提醒"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        // 创建点击意图
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminder.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建通知
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.reminderTitle)
            .setContentText(reminder.reminderMessage.take(100) + "...")
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.reminderMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // 发送通知
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminder.id.toInt(), notification)
    }
    
    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<PMSReminderWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "pms_reminder_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        private fun calculateInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val preferredTime = 9 * 60 * 60 * 1000 // 9:00 AM
            
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            return if (calendar.timeInMillis > now) {
                calendar.timeInMillis - now
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis - now
            }
        }
    }
}
```

### 7.2 在Application中初始化WorkManager

```kotlin
class WomenHealthApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化PMS提醒Worker
        PMSReminderWorker.schedule(this)
    }
}
```

---

## 8. UI 层实现

### 8.1 PMS提醒设置界面

```kotlin
@Composable
fun PMSReminderSettingsScreen(
    viewModel: PMSReminderViewModel = viewModel()
) {
    val settings by viewModel.reminderSettings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var isEnabled by remember { mutableStateOf(settings?.isEnabled ?: true) }
    var advanceDays by remember { mutableStateOf(settings?.advanceDays ?: 7) }
    var preferredTime by remember { mutableStateOf(settings?.preferredReminderTime ?: 32400000L) }
    
    var dietReminderEnabled by remember { mutableStateOf(settings?.dietReminderEnabled ?: true) }
    var exerciseReminderEnabled by remember { mutableStateOf(settings?.exerciseReminderEnabled ?: true) }
    var moodReminderEnabled by remember { mutableStateOf(settings?.moodReminderEnabled ?: true) }
    var careReminderEnabled by remember { mutableStateOf(settings?.careReminderEnabled ?: true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PMS提醒设置") },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }
            
            // 主开关
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启用PMS提醒",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "在PMS症状出现前接收提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { enabled ->
                            isEnabled = enabled
                            viewModel.toggleReminderEnabled(enabled)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提醒时机
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "提醒时机",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 提前天数
                    Text(
                        text = "提前提醒天数：$advanceDays 天",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = advanceDays.toFloat(),
                        onValueChange = { advanceDays = it.toInt() },
                        valueRange = 3f..14f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 提醒时间
                    Text(
                        text = "提醒时间",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTime(preferredTime),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提醒类型
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "提醒类型",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 饮食提醒
                    ReminderTypeItem(
                        title = "饮食提醒",
                        description = "接收饮食建议和食谱推荐",
                        icon = Icons.Default.Restaurant,
                        enabled = dietReminderEnabled,
                        onToggle = { dietReminderEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 运动提醒
                    ReminderTypeItem(
                        title = "运动提醒",
                        description = "接收运动建议和活动指导",
                        icon = Icons.Default.DirectionsRun,
                        enabled = exerciseReminderEnabled,
                        onToggle = { exerciseReminderEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 情绪管理提醒
                    ReminderTypeItem(
                        title = "情绪管理提醒",
                        description = "接收情绪管理技巧和减压方法",
                        icon = Icons.Default.Psychology,
                        enabled = moodReminderEnabled,
                        onToggle = { moodReminderEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 护理提醒
                    ReminderTypeItem(
                        title = "护理提醒",
                        description = "接收症状护理方法和建议",
                        icon = Icons.Default.HealthAndSafety,
                        enabled = careReminderEnabled,
                        onToggle = { careReminderEnabled = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    settings?.let {
                        val updatedSettings = it.copy(
                            isEnabled = isEnabled,
                            advanceDays = advanceDays,
                            preferredReminderTime = preferredTime,
                            dietReminderEnabled = dietReminderEnabled,
                            exerciseReminderEnabled = exerciseReminderEnabled,
                            moodReminderEnabled = moodReminderEnabled,
                            careReminderEnabled = careReminderEnabled
                        )
                        viewModel.updateReminderSettings(updatedSettings)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("保存设置")
                }
            }
        }
    }
    
    if (error != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        ) {
            Text(error ?: "未知错误")
        }
    }
}

@Composable
fun ReminderTypeItem(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

private fun formatTime(timeMillis: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}
```

### 8.2 PMS提醒历史界面

```kotlin
@Composable
fun PMSReminderHistoryScreen(
    viewModel: PMSReminderViewModel = viewModel()
) {
    val reminderHistory by viewModel.reminderHistory.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("今日提醒", "历史记录")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PMS提醒") },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 打开设置 */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab栏
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> TodayRemindersContent(todayReminders, isLoading, viewModel)
                1 -> HistoryRemindersContent(reminderHistory, isLoading, viewModel)
            }
        }
    }
}

@Composable
fun TodayRemindersContent(
    reminders: List<PMSReminderHistory>,
    isLoading: Boolean,
    viewModel: PMSReminderViewModel
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (reminders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.NotificationsNone,
            title = "暂无今日提醒",
            description = "您今天还没有收到PMS提醒"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reminders) { reminder ->
            ReminderCard(
                reminder = reminder,
                onMarkAsRead = { viewModel.markReminderAsRead(reminder.id) },
                onMarkAsActionPerformed = { viewModel.markReminderActionPerformed(reminder.id) }
            )
        }
    }
}

@Composable
fun HistoryRemindersContent(
    reminders: List<PMSReminderHistory>,
    isLoading: Boolean,
    viewModel: PMSReminderViewModel
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (reminders.isEmpty()) {
        EmptyState(
            icon = Icons.Default.History,
            title = "暂无历史记录",
            description = "您还没有PMS提醒历史记录"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reminders) { reminder ->
            ReminderHistoryCard(reminder = reminder)
        }
    }
}

@Composable
fun ReminderCard(
    reminder: PMSReminderHistory,
    onMarkAsRead: () -> Unit,
    onMarkAsActionPerformed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isRead) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isRead) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.reminderTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (reminder.isRead) FontWeight.Normal else FontWeight.Bold
                )
                
                if (!reminder.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = reminder.reminderMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            reminder.actionSuggestion?.let { suggestion ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 $suggestion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!reminder.isRead) {
                    OutlinedButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("标记已读")
                    }
                }
                
                if (!reminder.isActionPerformed) {
                    Button(onClick = onMarkAsActionPerformed) {
                        Text("完成行动")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已完成",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderHistoryCard(reminder: PMSReminderHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.reminderTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reminder.isActionPerformed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = formatDate(reminder.reminderDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = reminder.reminderMessage.take(100) + if (reminder.reminderMessage.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val now = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }
    
    return when {
        calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> "今天"
        
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "昨天"
        
        else -> {
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
```

---

## 9. 集成到主应用

### 9.1 更新数据库

```kotlin
@Database(
    entities = [
        MenstrualCycle::class,
        PMSSymptom::class,
        PMSReminderSettings::class,
        PMSReminderHistory::class,
        PMSSymptomPrediction::class,
        // ... 其他实体
    ],
    version = 2,
    exportSchema = false
)
abstract class WomenHealthDatabase : RoomDatabase() {
    
    abstract fun menstrualCycleDao(): MenstrualCycleDao
    abstract fun pmsSymptomDao(): PMSSymptomDao
    abstract fun pmsReminderSettingsDao(): PMSReminderSettingsDao
    abstract fun pmsReminderHistoryDao(): PMSReminderHistoryDao
    abstract fun pmsSymptomPredictionDao(): PMSSymptomPredictionDao
    
    companion object {
        @Volatile
        private var INSTANCE: WomenHealthDatabase? = null
        
        fun getDatabase(context: Context): WomenHealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WomenHealthDatabase::class.java,
                    "women_health_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 9.2 更新主界面导航

```kotlin
@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", "首页", Icons.Default.Home),
        BottomNavItem("cycle", "经期", Icons.Default.CalendarMonth),
        BottomNavItem("pms", "PMS", Icons.Default.Notifications),
        BottomNavItem("health", "健康", Icons.Default.HealthAndSafety),
        BottomNavItem("profile", "我的", Icons.Default.Person)
    )
    
    NavigationBar {
        items.forEach { item ->
            val selected = navController.currentDestination?.route == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
```

### 9.3 更新主导航图

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigation(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("cycle") { CycleScreen() }
            composable("pms") { PMSReminderHistoryScreen() }
            composable("health") { HealthScreen() }
            composable("profile") { ProfileScreen() }
            
            composable("pms/settings") { PMSReminderSettingsScreen() }
        }
    }
}
```

---

## 10. 测试策略

### 10.1 单元测试

```kotlin
@RunWith(AndroidJUnit4::class)
class PMSReminderRepositoryTest {
    
    private lateinit var database: WomenHealthDatabase
    private lateinit var repository: PMSReminderRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WomenHealthDatabase::class.java).build()
        repository = PMSReminderRepository(
            pmsReminderSettingsDao = database.pmsReminderSettingsDao(),
            pmsReminderHistoryDao = database.pmsReminderHistoryDao(),
            pmsSymptomPredictionDao = database.pmsSymptomPredictionDao(),
            menstrualCycleDao = database.menstrualCycleDao(),
            pmsSymptomDao = database.pmsSymptomDao()
        )
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun testCreateReminder() = runTest {
        val reminder = PMSReminderHistory(
            cycleId = 1L,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "diet",
            reminderTitle = "饮食提醒",
            reminderMessage = "测试提醒内容",
            isDelivered = false
        )
        
        val id = repository.createReminder(reminder)
        val savedReminder = repository.getReminderById(id)
        
        assertNotNull(savedReminder)
        assertEquals(reminder.reminderTitle, savedReminder?.reminderTitle)
    }
    
    @Test
    fun testPredictionAccuracy() = runTest {
        val prediction = PMSSymptomPrediction(
            cycleId = 1L,
            predictionDate = System.currentTimeMillis(),
            moodSwingsPrediction = 3,
            irritabilityPrediction = 4,
            anxietyPrediction = 2,
            depressionPrediction = 1,
            abdominalPainPrediction = 3,
            headachePrediction = 2,
            breastTendernessPrediction = 2,
            bloatingPrediction = 1,
            fatiguePrediction = 3,
            sleepDisturbancePrediction = 2,
            predictionConfidence = 0.8f,
            referenceCycles = "[1, 2, 3]",
            overallSeverity = 3,
            pmsType = "moderate",
            recommendations = "{}"
        )
        
        val id = repository.createPrediction(prediction)
        repository.updatePredictionAccuracy(id, 4) // 实际严重程度
        
        val updatedPrediction = repository.getPredictionById(id)
        assertNotNull(updatedPrediction)
        assertEquals(4, updatedPrediction?.actualSeverity)
        assertTrue((updatedPrediction?.predictionAccuracy ?: 0f) > 0f)
    }
}
```

### 10.2 UI测试

```kotlin
@RunWith(AndroidJUnit4::class)
class PMSReminderScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testPMSReminderSettingsScreen() {
        composeTestRule.setContent {
            PMSReminderSettingsScreen()
        }
        
        // 验证标题
        composeTestRule.onNodeWithText("PMS提醒设置").assertIsDisplayed()
        
        // 验证主开关
        composeTestRule.onNodeWithText("启用PMS提醒").assertIsDisplayed()
        
        // 点击开关
        composeTestRule.onNodeWithText("启用PMS提醒").performClick()
        
        // 验证提醒时机部分
        composeTestRule.onNodeWithText("提醒时机").assertIsDisplayed()
        
        // 验证提醒类型部分
        composeTestRule.onNodeWithText("提醒类型").assertIsDisplayed()
        
        // 验证保存按钮
        composeTestRule.onNodeWithText("保存设置").assertIsDisplayed()
    }
    
    @Test
    fun testReminderCardDisplay() {
        val reminder = PMSReminderHistory(
            cycleId = 1L,
            reminderDate = System.currentTimeMillis(),
            reminderTime = System.currentTimeMillis(),
            reminderType = "diet",
            reminderTitle = "🥗 PMS饮食建议",
            reminderMessage = "今天的饮食建议：\n• 推荐食物：\n  - 富含镁的食物",
            isDelivered = false,
            isRead = false
        )
        
        composeTestRule.setContent {
            ReminderCard(
                reminder = reminder,
                onMarkAsRead = {},
                onMarkAsActionPerformed = {}
            )
        }
        
        // 验证标题
        composeTestRule.onNodeWithText("🥗 PMS饮食建议").assertIsDisplayed()
        
        // 验证内容
        composeTestRule.onNodeWithText("今天的饮食建议：").assertIsDisplayed()
        
        // 验证按钮
        composeTestRule.onNodeWithText("标记已读").assertIsDisplayed()
        composeTestRule.onNodeWithText("完成行动").assertIsDisplayed()
    }
}
```

---

## 11. 性能优化

### 11.1 数据库查询优化

```kotlin
@Dao
interface PMSReminderHistoryDao {
    
    // 使用索引优化查询
    @Query("SELECT * FROM pms_reminder_history WHERE reminderDate = :date")
    suspend fun getRemindersByDate(date: Long): List<PMSReminderHistory>
    
    // 分页查询
    @Query("SELECT * FROM pms_reminder_history ORDER BY reminderDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getRemindersPaged(limit: Int, offset: Int): List<PMSReminderHistory>
    
    // 只查询必要字段
    @Query("SELECT id, reminderTitle, reminderMessage, isRead, isActionPerformed FROM pms_reminder_history WHERE reminderDate >= :startDate")
    fun getSimpleReminders(startDate: Long): Flow<List<SimpleReminder>>
}

// 简化数据类
data class SimpleReminder(
    val id: Long,
    val reminderTitle: String,
    val reminderMessage: String,
    val isRead: Boolean,
    val isActionPerformed: Boolean
)
```

### 11.2 内存优化

```kotlin
class PMSReminderViewModel(application: Application) : AndroidViewModel(application) {
    
    // 使用 Flow 而不是 LiveData，减少内存占用
    private val _reminderHistory = MutableStateFlow<List<PMSReminderHistory>>(emptyList())
    val reminderHistory: StateFlow<List<PMSReminderHistory>> = _reminderHistory.asStateFlow()
    
    // 使用分页加载
    fun loadRemindersPaged(page: Int, pageSize: Int = 20) {
        viewModelScope.launch {
            val offset = page * pageSize
            val reminders = repository.getRemindersPaged(pageSize, offset)
            _reminderHistory.value = if (page == 0) reminders else _reminderHistory.value + reminders
        }
    }
    
    // 清理旧数据
    fun cleanOldReminders(daysToKeep: Int = 90) {
        viewModelScope.launch {
            val cutoffDate = System.currentTimeMillis() - daysToKeep * 24 * 60 * 60 * 1000L
            repository.deleteRemindersBeforeDate(cutoffDate)
        }
    }
}
```

---

## 12. 数据库迁移

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 PMS提醒设置表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS pms_reminder_settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId INTEGER NOT NULL,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                advanceDays INTEGER NOT NULL DEFAULT 7,
                reminderFrequency TEXT NOT NULL DEFAULT 'daily',
                symptomPredictionEnabled INTEGER NOT NULL DEFAULT 1,
                dietReminderEnabled INTEGER NOT NULL DEFAULT 1,
                exerciseReminderEnabled INTEGER NOT NULL DEFAULT 1,
                moodReminderEnabled INTEGER NOT NULL DEFAULT 1,
                careReminderEnabled INTEGER NOT NULL DEFAULT 1,
                supplementReminderEnabled INTEGER NOT NULL DEFAULT 0,
                preferredReminderTime INTEGER NOT NULL DEFAULT 32400000,
                mildSymptomThreshold INTEGER NOT NULL DEFAULT 2,
                moderateSymptomThreshold INTEGER NOT NULL DEFAULT 3,
                severeSymptomThreshold INTEGER NOT NULL DEFAULT 4,
                customReminders TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)
        
        // 创建 PMS提醒历史表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS pms_reminder_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cycleId INTEGER,
                reminderDate INTEGER NOT NULL,
                reminderTime INTEGER NOT NULL,
                reminderType TEXT NOT NULL,
                reminderSubtype TEXT,
                reminderTitle TEXT NOT NULL,
                reminderMessage TEXT NOT NULL,
                actionSuggestion TEXT,
                predictedSeverity INTEGER,
                predictedSymptoms TEXT,
                isDelivered INTEGER NOT NULL DEFAULT 0,
                isRead INTEGER NOT NULL DEFAULT 0,
                isActionPerformed INTEGER NOT NULL DEFAULT 0,
                userRating INTEGER,
                userFeedback TEXT,
                deliveredAt INTEGER,
                readAt INTEGER,
                actionPerformedAt INTEGER,
                createdAt INTEGER NOT NULL
            )
        """)
        
        // 创建 PMS症状预测表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS pms_symptom_prediction (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cycleId INTEGER NOT NULL,
                predictionDate INTEGER NOT NULL,
                moodSwingsPrediction INTEGER,
                irritabilityPrediction INTEGER,
                anxietyPrediction INTEGER,
                depressionPrediction INTEGER,
                abdominalPainPrediction INTEGER,
                headachePrediction INTEGER,
                breastTendernessPrediction INTEGER,
                bloatingPrediction INTEGER,
                fatiguePrediction INTEGER,
                sleepDisturbancePrediction INTEGER,
                predictionConfidence REAL NOT NULL,
                referenceCycles TEXT NOT NULL,
                overallSeverity INTEGER NOT NULL,
                pmsType TEXT NOT NULL,
                recommendations TEXT NOT NULL,
                actualSeverity INTEGER,
                predictionAccuracy REAL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pms_reminder_history_reminderDate ON pms_reminder_history(reminderDate)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pms_reminder_history_cycleId ON pms_reminder_history(cycleId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pms_symptom_prediction_cycleId ON pms_symptom_prediction(cycleId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_pms_symptom_prediction_predictionDate ON pms_symptom_prediction(predictionDate)")
    }
}
```

---

## 13. 总结

本文档详细描述了PMS提醒功能的开发方案，包括：

1. **数据库设计**：三个核心数据表（提醒设置、提醒历史、症状预测）
2. **提醒类型**：六种提醒类型（症状预测、饮食、运动、情绪管理、护理、补充剂）
3. **技术实现**：完整的Repository、ViewModel、UI实现
4. **WorkManager调度**：自动定时提醒功能
5. **测试策略**：单元测试和UI测试示例
6. **性能优化**：数据库查询优化和内存优化
7. **数据迁移**：数据库版本升级方案

该功能通过智能预测和多维度提醒，帮助用户更好地管理PMS症状，提升生活质量。

---

**文档版本**: 1.0  
**创建日期**: 2026-02-14  
**维护者**: 开发团队