# 周期规律性评分功能 - 开发技术规范

## 文档版本
- 版本：v1.0
- 创建日期：2026-02-14
- 文档类型：功能开发技术规范
- 所属模块：智能预测引擎

---

## 目录

1. [功能概述](#功能概述)
2. [业务逻辑](#业务逻辑)
3. [评分算法](#评分算法)
4. [技术实现](#技术实现)
5. [数据模型](#数据模型)
6. [API设计](#api设计)
7. [UI设计](#ui设计)
8. [测试方案](#测试方案)
9. [风险提示](#风险提示)

---

## 功能概述

### 功能定义
周期规律性评分功能是基于用户历史经期数据，通过统计学方法评估月经周期规律性的智能分析功能。该功能通过计算多个维度的指标，生成一个综合评分（0-100分），帮助用户了解自身月经周期的健康状况。

### 核心价值
- **健康预警**：早期发现周期异常，提示潜在健康风险
- **数据可视化**：直观展示周期规律性，便于用户理解
- **个性化建议**：根据评分结果提供针对性的健康建议
- **趋势追踪**：长期监控周期规律性变化趋势

### 适用场景
- 用户记录至少3个周期后启用
- 每次记录新周期后自动更新评分
- 用户主动查看健康报告时展示
- 异常评分时触发健康提醒

### 使用频率
- 自动更新：每次记录新周期
- 用户查看：按需查看
- 健康提醒：评分异常时主动提醒

---

## 业务逻辑

### 数据采集

#### 输入数据
```
输入数据源：
1. 历史周期记录（MenstrualCycle）
   - 周期开始日期
   - 周期结束日期
   - 周期长度
   - 经期天数

2. 最少数据要求：至少3个完整周期
3. 推荐数据：6-12个周期以获得更准确的评分
```

#### 数据筛选规则
```kotlin
数据筛选条件：
1. 排除预测周期（isPredicted = false）
2. 只计算完整周期（startDate 和 endDate 都不为空）
3. 排除异常周期（cycleLength < 18 或 > 45）
4. 按时间排序，使用最近N个周期（默认6个）
```

### 评分触发时机

#### 自动触发
```
触发条件：
1. 用户记录新的周期开始
2. 用户记录周期结束
3. 每天定时任务（检查是否有未计算的周期）
```

#### 手动触发
```
触发条件：
1. 用户在健康统计页面查看
2. 用户点击"更新分析"按钮
3. 导出健康报告时
```

### 评分存储
```kotlin
存储策略：
1. 实时计算：不存储计算结果，每次重新计算
2. 缓存策略：缓存最近一次评分，有效期为24小时
3. 历史记录：每月保存一次评分，用于趋势分析
```

---

## 评分算法

### 评分维度

#### 维度1：周期一致性（权重：40%）
```
计算方法：
- 计算周期长度的标准差（Standard Deviation）
- 计算变异系数（CV = 标准差 / 平均周期长度）
- 根据变异系数评分

评分标准：
变异系数 < 0.07：40分（非常规律）
变异系数 0.07-0.15：30分（规律）
变异系数 0.15-0.25：20分（轻微不规律）
变异系数 0.25-0.35：10分（不规律）
变异系数 > 0.35：5分（非常不规律）
```

#### 维度2：周期正常性（权重：30%）
```
计算方法：
- 计算平均周期长度
- 检查周期长度是否在正常范围（21-35天）
- 计算异常周期比例

评分标准：
平均周期 25-30天：30分（非常正常）
平均周期 21-25天或30-35天：25分（正常）
平均周期 < 21天或 > 35天：15分（异常）
平均周期 < 18天或 > 45天：5分（严重异常）
```

#### 维度3：经期稳定性（权重：20%）
```
计算方法：
- 计算经期天数的标准差
- 计算平均经期天数

评分标准：
经期天数标准差 < 1天：20分（非常稳定）
经期天数标准差 1-2天：15分（稳定）
经期天数标准差 2-3天：10分（轻微不稳定）
经期天数标准差 > 3天：5分（不稳定）
```

#### 维度4：数据充足性（权重：10%）
```
计算方法：
- 统计用于计算的周期数量

评分标准：
周期数量 ≥ 12：10分（数据充足）
周期数量 6-11：8分（数据良好）
周期数量 3-5：5分（数据不足）
周期数量 < 3：0分（无法评分）
```

### 综合评分计算

```kotlin
/**
 * 周期规律性评分计算
 * @param cycles 周期记录列表
 * @return 评分结果对象
 */
fun calculateRegularityScore(cycles: List<MenstrualCycle>): RegularityScore {
    // 1. 数据验证
    if (cycles.size < 3) {
        return RegularityScore(
            score = 0,
            level = ScoreLevel.INSUFFICIENT_DATA,
            dimensions = emptyMap(),
            recommendations = listOf("请继续记录更多周期数据以获得准确评分")
        )
    }

    // 2. 计算各维度评分
    val consistencyScore = calculateConsistencyScore(cycles) // 维度1
    val normalityScore = calculateNormalityScore(cycles)     // 维度2
    val stabilityScore = calculateStabilityScore(cycles)     // 维度3
    val dataScore = calculateDataScore(cycles.size)          // 维度4

    // 3. 计算综合评分
    val totalScore = consistencyScore + normalityScore + stabilityScore + dataScore

    // 4. 确定评分等级
    val level = when (totalScore) {
        in 90..100 -> ScoreLevel.EXCELLENT
        in 80..89 -> ScoreLevel.GOOD
        in 70..79 -> ScoreLevel.FAIR
        in 60..69 -> ScoreLevel.POOR
        else -> ScoreLevel.VERY_POOR
    }

    // 5. 生成建议
    val recommendations = generateRecommendations(level, totalScore, cycles)

    return RegularityScore(
        score = totalScore,
        level = level,
        dimensions = mapOf(
            "consistency" to consistencyScore,
            "normality" to normalityScore,
            "stability" to stabilityScore,
            "data" to dataScore
        ),
        recommendations = recommendations
    )
}
```

### 评分等级定义

```kotlin
enum class ScoreLevel(
    val displayName: String,
    val color: String,
    val description: String
) {
    INSUFFICIENT_DATA(
        displayName = "数据不足",
        color = "#999999",
        description = "需要至少3个周期数据才能评分"
    ),
    EXCELLENT(
        displayName = "非常规律",
        color = "#4CAF50",
        description = "您的周期非常规律，健康状况良好"
    ),
    GOOD(
        displayName = "规律",
        color = "#8BC34A",
        description = "您的周期规律，健康状况良好"
    ),
    FAIR(
        displayName = "轻微不规律",
        color = "#FFC107",
        description = "您的周期轻微不规律，建议关注"
    ),
    POOR(
        displayName = "不规律",
        color = "#FF9800",
        description = "您的周期不规律，建议就医检查"
    ),
    VERY_POOR(
        displayName = "非常不规律",
        color = "#F44336",
        description = "您的周期非常不规律，强烈建议就医"
    )
}
```

### 算法优化

#### 数据平滑
```kotlin
/**
 * 使用移动平均平滑周期数据
 * @param cycles 原始周期数据
 * @param window 窗口大小（默认为3）
 * @return 平滑后的周期数据
 */
fun smoothCycleData(cycles: List<MenstrualCycle>, window: Int = 3): List<Float> {
    if (cycles.size < window) {
        return cycles.map { it.cycleLength.toFloat() }
    }

    val smoothed = mutableListOf<Float>()
    for (i in cycles.indices) {
        val start = maxOf(0, i - window / 2)
        val end = minOf(cycles.size, i + window / 2 + 1)
        val avg = cycles.subList(start, end)
            .map { it.cycleLength }
            .average()
            .toFloat()
        smoothed.add(avg)
    }

    return smoothed
}
```

#### 异常值剔除
```kotlin
/**
 * 使用IQR方法剔除异常值
 * @param values 原始数据
 * @return 剔除异常值后的数据
 */
fun removeOutliers(values: List<Float>): List<Float> {
    if (values.size < 4) return values

    val sorted = values.sorted()
    val q1 = sorted[(sorted.size * 0.25).toInt()]
    val q3 = sorted[(sorted.size * 0.75).toInt()]
    val iqr = q3 - q1
    val lowerBound = q1 - 1.5 * iqr
    val upperBound = q3 + 1.5 * iqr

    return values.filter { it in lowerBound..upperBound }
}
```

---

## 技术实现

### 数据模型

#### 评分结果实体
```kotlin
/**
 * 周期规律性评分结果
 */
data class RegularityScore(
    val score: Int,                           // 综合评分（0-100）
    val level: ScoreLevel,                     // 评分等级
    val dimensions: Map<String, Int>,          // 各维度评分
    val recommendations: List<String>,         // 健康建议
    val statistics: CycleStatistics,           // 统计数据
    val calculatedAt: Long = System.currentTimeMillis() // 计算时间
)

/**
 * 周期统计数据
 */
data class CycleStatistics(
    val averageCycleLength: Float,             // 平均周期长度
    val cycleStdDev: Float,                    // 周期标准差
    val cycleCoefficientOfVariation: Float,    // 周期变异系数
    val averagePeriodDuration: Float,          // 平均经期天数
    val periodStdDev: Float,                   // 经期标准差
    val abnormalCycleCount: Int,               // 异常周期数量
    val totalCycleCount: Int,                  // 总周期数量
    val trend: CycleTrend                      // 周期趋势
)

/**
 * 周期趋势
 */
enum class CycleTrend {
    STABLE,        // 稳定
    IMPROVING,     // 改善
    WORSENING,     // 恶化
    INSUFFICIENT   // 数据不足
}
```

### 核心类设计

#### 评分计算器
```kotlin
/**
 * 周期规律性评分计算器
 */
class RegularityScoreCalculator(
    private val minCycles: Int = 3,
    private val maxCycles: Int = 12
) {

    /**
     * 计算规律性评分
     */
    fun calculate(cycles: List<MenstrualCycle>): RegularityScore {
        // 1. 数据预处理
        val filteredCycles = filterCycles(cycles)
        val recentCycles = selectRecentCycles(filteredCycles)

        // 2. 数据验证
        if (recentCycles.size < minCycles) {
            return createInsufficientDataResult()
        }

        // 3. 计算统计数据
        val statistics = calculateStatistics(recentCycles)

        // 4. 计算各维度评分
        val dimensions = mapOf(
            "consistency" to calculateConsistencyScore(statistics),
            "normality" to calculateNormalityScore(statistics),
            "stability" to calculateStabilityScore(statistics),
            "data" to calculateDataScore(recentCycles.size)
        )

        // 5. 计算综合评分
        val totalScore = dimensions.values.sum()

        // 6. 确定评分等级
        val level = determineScoreLevel(totalScore)

        // 7. 生成建议
        val recommendations = generateRecommendations(
            level,
            totalScore,
            statistics
        )

        return RegularityScore(
            score = totalScore,
            level = level,
            dimensions = dimensions,
            recommendations = recommendations,
            statistics = statistics
        )
    }

    /**
     * 过滤周期数据
     */
    private fun filterCycles(cycles: List<MenstrualCycle>): List<MenstrualCycle> {
        return cycles.filter { cycle ->
            !cycle.isPredicted &&
            cycle.startDate != 0 &&
            cycle.endDate != null &&
            cycle.cycleLength in 18..45
        }
    }

    /**
     * 选择最近的周期
     */
    private fun selectRecentCycles(cycles: List<MenstrualCycle>): List<MenstrualCycle> {
        return cycles
            .sortedByDescending { it.startDate }
            .take(maxCycles)
            .sortedBy { it.startDate }
    }

    /**
     * 计算统计数据
     */
    private fun calculateStatistics(cycles: List<MenstrualCycle>): CycleStatistics {
        val cycleLengths = cycles.map { it.cycleLength.toFloat() }
        val periodDurations = cycles
            .filter { it.periodDuration != null }
            .map { it.periodDuration!!.toFloat() }

        val avgCycleLength = cycleLengths.average().toFloat()
        val avgPeriodDuration = periodDurations.average().toFloat()

        val cycleStdDev = calculateStandardDeviation(cycleLengths, avgCycleLength)
        val periodStdDev = if (periodDurations.isNotEmpty()) {
            calculateStandardDeviation(periodDurations, avgPeriodDuration)
        } else 0f

        val cv = if (avgCycleLength > 0) {
            cycleStdDev / avgCycleLength
        } else 0f

        val abnormalCount = cycles.count {
            it.cycleLength !in 21..35
        }

        val trend = analyzeTrend(cycles)

        return CycleStatistics(
            averageCycleLength = avgCycleLength,
            cycleStdDev = cycleStdDev,
            cycleCoefficientOfVariation = cv,
            averagePeriodDuration = avgPeriodDuration,
            periodStdDev = periodStdDev,
            abnormalCycleCount = abnormalCount,
            totalCycleCount = cycles.size,
            trend = trend
        )
    }

    /**
     * 计算标准差
     */
    private fun calculateStandardDeviation(
        values: List<Float>,
        mean: Float
    ): Float {
        if (values.isEmpty()) return 0f

        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance).toFloat()
    }

    /**
     * 分析趋势
     */
    private fun analyzeTrend(cycles: List<MenstrualCycle>): CycleTrend {
        if (cycles.size < 3) return CycleTrend.INSUFFICIENT

        val recentScores = cycles.takeLast(3).map { cycle ->
            calculateSingleCycleScore(cycle)
        }

        return when {
            recentScores[2] > recentScores[1] && recentScores[1] > recentScores[0] ->
                CycleTrend.IMPROVING
            recentScores[2] < recentScores[1] && recentScores[1] < recentScores[0] ->
                CycleTrend.WORSENING
            else -> CycleTrend.STABLE
        }
    }

    /**
     * 计算单个周期的评分
     */
    private fun calculateSingleCycleScore(cycle: MenstrualCycle): Float {
        var score = 100f

        // 周期长度评分
        when (cycle.cycleLength) {
            in 25..30 -> score -= 0
            in 21..25, in 30..35 -> score -= 10
            in 18..21, in 35..45 -> score -= 30
            else -> score -= 50
        }

        // 经期天数评分
        cycle.periodDuration?.let { duration ->
            when (duration) {
                in 3..7 -> score -= 0
                in 2..3, in 7..10 -> score -= 10
                else -> score -= 30
            }
        }

        return score
    }

    /**
     * 计算周期一致性评分
     */
    private fun calculateConsistencyScore(statistics: CycleStatistics): Int {
        return when (statistics.cycleCoefficientOfVariation) {
            in 0f..0.07f -> 40
            in 0.07f..0.15f -> 30
            in 0.15f..0.25f -> 20
            in 0.25f..0.35f -> 10
            else -> 5
        }
    }

    /**
     * 计算周期正常性评分
     */
    private fun calculateNormalityScore(statistics: CycleStatistics): Int {
        return when {
            statistics.averageCycleLength in 25f..30f -> 30
            statistics.averageCycleLength in 21f..25f ||
            statistics.averageCycleLength in 30f..35f -> 25
            statistics.averageCycleLength in 18f..21f ||
            statistics.averageCycleLength in 35f..45f -> 15
            else -> 5
        }
    }

    /**
     * 计算经期稳定性评分
     */
    private fun calculateStabilityScore(statistics: CycleStatistics): Int {
        return when (statistics.periodStdDev) {
            in 0f..1f -> 20
            in 1f..2f -> 15
            in 2f..3f -> 10
            else -> 5
        }
    }

    /**
     * 计算数据充足性评分
     */
    private fun calculateDataScore(cycleCount: Int): Int {
        return when (cycleCount) {
            in 12..Int.MAX_VALUE -> 10
            in 6..11 -> 8
            in 3..5 -> 5
            else -> 0
        }
    }

    /**
     * 确定评分等级
     */
    private fun determineScoreLevel(score: Int): ScoreLevel {
        return when (score) {
            in 90..100 -> ScoreLevel.EXCELLENT
            in 80..89 -> ScoreLevel.GOOD
            in 70..79 -> ScoreLevel.FAIR
            in 60..69 -> ScoreLevel.POOR
            else -> ScoreLevel.VERY_POOR
        }
    }

    /**
     * 生成健康建议
     */
    private fun generateRecommendations(
        level: ScoreLevel,
        score: Int,
        statistics: CycleStatistics
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (level) {
            ScoreLevel.EXCELLENT -> {
                recommendations.add("您的周期非常规律，继续保持健康的生活方式")
            }
            ScoreLevel.GOOD -> {
                recommendations.add("您的周期规律，继续保持良好的生活习惯")
            }
            ScoreLevel.FAIR -> {
                recommendations.add("您的周期轻微不规律，建议注意以下几点：")
                if (statistics.cycleCoefficientOfVariation > 0.15) {
                    recommendations.add("- 保持规律的作息时间")
                    recommendations.add("- 减少压力，适当运动")
                }
                if (statistics.averagePeriodDuration !in 3f..7f) {
                    recommendations.add("- 注意经期护理，如异常请就医")
                }
            }
            ScoreLevel.POOR -> {
                recommendations.add("您的周期不规律，建议：")
                recommendations.add("- 记录详细的生活习惯（饮食、运动、睡眠）")
                recommendations.add("- 避免过度节食或暴饮暴食")
                recommendations.add("- 如持续不规律，建议就医检查")
                if (statistics.abnormalCycleCount > 0) {
                    recommendations.add("- 您的周期长度超出正常范围，请重点关注")
                }
            }
            ScoreLevel.VERY_POOR -> {
                recommendations.add("您的周期非常不规律，强烈建议：")
                recommendations.add("- 尽快就医检查内分泌功能")
                recommendations.add("- 可能的疾病：多囊卵巢综合征、甲状腺功能异常等")
                recommendations.add("- 记录详细的症状和生活日志，帮助医生诊断")
            }
            ScoreLevel.INSUFFICIENT_DATA -> {
                recommendations.add("请继续记录至少3个周期数据以获得准确评分")
            }
        }

        return recommendations
    }

    /**
     * 创建数据不足结果
     */
    private fun createInsufficientDataResult(): RegularityScore {
        return RegularityScore(
            score = 0,
            level = ScoreLevel.INSUFFICIENT_DATA,
            dimensions = emptyMap(),
            recommendations = listOf("请继续记录至少3个周期数据以获得准确评分"),
            statistics = CycleStatistics(
                averageCycleLength = 0f,
                cycleStdDev = 0f,
                cycleCoefficientOfVariation = 0f,
                averagePeriodDuration = 0f,
                periodStdDev = 0f,
                abnormalCycleCount = 0,
                totalCycleCount = 0,
                trend = CycleTrend.INSUFFICIENT
            )
        )
    }
}
```

### Repository实现

```kotlin
/**
 * 周期规律性评分Repository
 */
class RegularityScoreRepository(
    private val menstrualCycleDao: MenstrualCycleDao,
    private val calculator: RegularityScoreCalculator = RegularityScoreCalculator()
) {

    /**
     * 计算当前评分
     */
    suspend fun calculateCurrentScore(): RegularityScore {
        val cycles = menstrualCycleDao.getRecentCycles()
        return calculator.calculate(cycles)
    }

    /**
     * 计算历史评分（用于趋势分析）
     */
    suspend fun calculateHistoricalScores(months: Int = 12): List<HistoricalScore> {
        val allCycles = menstrualCycleDao.getAllCycles()
        val historicalScores = mutableListOf<HistoricalScore>()

        for (i in 0 until months) {
            val endDate = System.currentTimeMillis() - (i * 30L * 24 * 60 * 60 * 1000)
            val startDate = endDate - (90L * 24 * 60 * 60 * 1000) // 3个月数据

            val cyclesInRange = allCycles.filter {
                it.startDate in startDate..endDate
            }

            if (cyclesInRange.size >= 3) {
                val score = calculator.calculate(cyclesInRange)
                historicalScores.add(
                    HistoricalScore(
                        date = endDate,
                        score = score.score,
                        level = score.level
                    )
                )
            }
        }

        return historicalScores.sortedBy { it.date }
    }

    /**
     * 获取评分趋势
     */
    suspend fun getScoreTrend(): ScoreTrend {
        val currentScore = calculateCurrentScore()
        val historicalScores = calculateHistoricalScores(6)

        if (historicalScores.size < 2) {
            return ScoreTrend.INSUFFICIENT
        }

        val recentScores = historicalScores.takeLast(3).map { it.score }
        val avgRecent = recentScores.average()
        val avgPrevious = historicalScores.dropLast(3).map { it.score }.average()

        return when {
            avgRecent > avgPrevious + 5 -> ScoreTrend.IMPROVING
            avgRecent < avgPrevious - 5 -> ScoreTrend.WORSENING
            else -> ScoreTrend.STABLE
        }
    }
}

/**
 * 历史评分
 */
data class HistoricalScore(
    val date: Long,
    val score: Int,
    val level: ScoreLevel
)

/**
 * 评分趋势
 */
enum class ScoreTrend {
    IMPROVING,     // 改善
    WORSENING,     // 恶化
    STABLE,        // 稳定
    INSUFFICIENT   // 数据不足
}
```

### ViewModel实现

```kotlin
/**
 * 周期规律性评分ViewModel
 */
@HiltViewModel
class RegularityScoreViewModel @Inject constructor(
    private val repository: RegularityScoreRepository
) : ViewModel() {

    private val _scoreState = MutableStateFlow<RegularityScore?>(null)
    val scoreState: StateFlow<RegularityScore?> = _scoreState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _historicalScores = MutableStateFlow<List<HistoricalScore>>(emptyList())
    val historicalScores: StateFlow<List<HistoricalScore>> = _historicalScores.asStateFlow()

    /**
     * 加载当前评分
     */
    fun loadCurrentScore() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val score = repository.calculateCurrentScore()
                _scoreState.value = score
            } catch (e: Exception) {
                _error.value = e.message ?: "加载评分失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载历史评分
     */
    fun loadHistoricalScores(months: Int = 12) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val scores = repository.calculateHistoricalScores(months)
                _historicalScores.value = scores
            } catch (e: Exception) {
                _error.value = e.message ?: "加载历史评分失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新评分
     */
    fun refreshScore() {
        loadCurrentScore()
        loadHistoricalScores()
    }
}
```

---

## API设计

### DAO接口

```kotlin
@Dao
interface MenstrualCycleDao {
    @Query("SELECT * FROM menstrual_cycles WHERE NOT isPredicted ORDER BY startDate DESC")
    suspend fun getAllCycles(): List<MenstrualCycle>

    @Query("SELECT * FROM menstrual_cycles WHERE NOT isPredicted ORDER BY startDate DESC LIMIT :limit")
    suspend fun getRecentCycles(limit: Int = 12): List<MenstrualCycle>

    @Query("SELECT * FROM menstrual_cycles WHERE startDate BETWEEN :startTime AND :endTime AND NOT isPredicted ORDER BY startDate DESC")
    suspend fun getCyclesInRange(startTime: Long, endTime: Long): List<MenstrualCycle>
}
```

### Use Cases

```kotlin
/**
 * 计算周期规律性评分用例
 */
class CalculateRegularityScoreUseCase(
    private val repository: RegularityScoreRepository
) {
    suspend operator fun invoke(): Result<RegularityScore> {
        return try {
            val score = repository.calculateCurrentScore()
            Result.success(score)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 获取评分趋势用例
 */
class GetScoreTrendUseCase(
    private val repository: RegularityScoreRepository
) {
    suspend operator fun invoke(): Result<ScoreTrend> {
        return try {
            val trend = repository.getScoreTrend()
            Result.success(trend)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## UI设计

### 评分展示界面

#### 布局结构
```xml
<!-- 评分展示布局 -->
<LinearLayout
    orientation="vertical"
    padding="16dp">

    <!-- 评分总览卡片 -->
    <Card
        elevation="4dp"
        cornerRadius="12dp">

        <LinearLayout
            orientation="horizontal"
            padding="20dp">

            <!-- 评分环形图 -->
            <com.example.womenhealthapp.widget.ScoreCircleView
                width="120dp"
                height="120dp"
                score="@{viewModel.scoreState.score}"
                level="@{viewModel.scoreState.level}" />

            <!-- 评分信息 -->
            <LinearLayout
                orientation="vertical"
                marginStart="20dp">

                <TextView
                    text="@{viewModel.scoreState.level.displayName}"
                    textSize="24sp"
                    textColor="@{viewModel.scoreState.level.color}" />

                <TextView
                    text="@{String.format('综合评分：%d分', viewModel.scoreState.score)}"
                    textSize="16sp"
                    marginTop="8dp" />

                <TextView
                    text="@{viewModel.scoreState.level.description}"
                    textSize="14sp"
                    marginTop="4dp"
                    textColor="@color/gray" />
            </LinearLayout>
        </LinearLayout>
    </Card>

    <!-- 维度评分卡片 -->
    <Card
        elevation="4dp"
        cornerRadius="12dp"
        marginTop="16dp">

        <LinearLayout
            orientation="vertical"
            padding="20dp">

            <TextView
                text="各维度评分"
                textSize="18sp"
                style="@style/TextAppearance.Bold" />

            <!-- 周期一致性 -->
            <DimensionProgressView
                title="周期一致性"
                score="@{viewModel.scoreState.dimensions['consistency']}"
                maxScore="40"
                marginTop="12dp" />

            <!-- 周期正常性 -->
            <DimensionProgressView
                title="周期正常性"
                score="@{viewModel.scoreState.dimensions['normality']}"
                maxScore="30"
                marginTop="12dp" />

            <!-- 经期稳定性 -->
            <DimensionProgressView
                title="经期稳定性"
                score="@{viewModel.scoreState.dimensions['stability']}"
                maxScore="20"
                marginTop="12dp" />

            <!-- 数据充足性 -->
            <DimensionProgressView
                title="数据充足性"
                score="@{viewModel.scoreState.dimensions['data']}"
                maxScore="10"
                marginTop="12dp" />
        </LinearLayout>
    </Card>

    <!-- 统计数据卡片 -->
    <Card
        elevation="4dp"
        cornerRadius="12dp"
        marginTop="16dp">

        <LinearLayout
            orientation="vertical"
            padding="20dp">

            <TextView
                text="统计数据"
                textSize="18sp"
                style="@style/TextAppearance.Bold" />

            <StatisticsItemView
                label="平均周期长度"
                value="@{String.format('%.1f天', viewModel.scoreState.statistics.averageCycleLength)}"
                marginTop="12dp" />

            <StatisticsItemView
                label="周期标准差"
                value="@{String.format('%.2f天', viewModel.scoreState.statistics.cycleStdDev)}"
                marginTop="8dp" />

            <StatisticsItemView
                label="变异系数"
                value="@{String.format('%.2f%%', viewModel.scoreState.statistics.cycleCoefficientOfVariation * 100)}"
                marginTop="8dp" />

            <StatisticsItemView
                label="平均经期天数"
                value="@{String.format('%.1f天', viewModel.scoreState.statistics.averagePeriodDuration)}"
                marginTop="8dp" />

            <StatisticsItemView
                label="异常周期数量"
                value="@{String.format('%d/%d', viewModel.scoreState.statistics.abnormalCycleCount, viewModel.scoreState.statistics.totalCycleCount)}"
                marginTop="8dp" />
        </LinearLayout>
    </Card>

    <!-- 健康建议卡片 -->
    <Card
        elevation="4dp"
        cornerRadius="12dp"
        marginTop="16dp">

        <LinearLayout
            orientation="vertical"
            padding="20dp">

            <TextView
                text="健康建议"
                textSize="18sp"
                style="@style/TextAppearance.Bold" />

            <RecyclerView
                id="@+id/recommendationsRecyclerView"
                layoutManager="LinearLayoutManager"
                marginTop="12dp"
                adapter="@{recommendationsAdapter}" />
        </LinearLayout>
    </Card>

    <!-- 历史趋势图表 -->
    <Card
        elevation="4dp"
        cornerRadius="12dp"
        marginTop="16dp">

        <LinearLayout
            orientation="vertical"
            padding="20dp">

            <TextView
                text="历史趋势"
                textSize="18sp"
                style="@style/TextAppearance.Bold" />

            <com.github.mikephil.charting.charts.LineChart
                id="@+id/trendChart"
                height="200dp"
                marginTop="12dp" />
        </LinearLayout>
    </Card>

</LinearLayout>
```

#### 评分环形图
```kotlin
/**
 * 评分环形图视图
 */
class ScoreCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var score: Int = 0
    private var level: ScoreLevel = ScoreLevel.INSUFFICIENT_DATA

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16.dp.toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 32.sp.toFloat()
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width - 16.dp) / 2f

        // 绘制背景圆环
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 绘制进度圆环
        paint.color = Color.parseColor(level.color)
        val sweepAngle = (score / 100f) * 360f
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            sweepAngle,
            false,
            paint
        )

        // 绘制分数
        textPaint.color = Color.parseColor(level.color)
        canvas.drawText(score.toString(), centerX, centerY + 12.sp, textPaint)
    }

    fun setScore(score: Int, level: ScoreLevel) {
        this.score = score
        this.level = level
        invalidate()
    }
}
```

#### 维度进度条
```kotlin
/**
 * 维度评分进度条
 */
class DimensionProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val scoreTextView: TextView
    private val progressBar: ProgressBar

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_dimension_progress, this)

        titleTextView = findViewById(R.id.titleTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        progressBar = findViewById(R.id.progressBar)
    }

    fun setData(title: String, score: Int, maxScore: Int) {
        titleTextView.text = title
        scoreTextView.text = "$score/$maxScore"
        progressBar.max = maxScore
        progressBar.progress = score
    }
}
```

---

## 测试方案

### 单元测试

#### 计算器测试
```kotlin
class RegularityScoreCalculatorTest {

    private val calculator = RegularityScoreCalculator()

    @Test
    fun `calculate should return insufficient data when cycles less than 3`() {
        val cycles = listOf(
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 29)
        )

        val result = calculator.calculate(cycles)

        assertEquals(0, result.score)
        assertEquals(ScoreLevel.INSUFFICIENT_DATA, result.level)
    }

    @Test
    fun `calculate should return excellent score for regular cycles`() {
        val cycles = listOf(
            createCycle(cycleLength = 28, periodDuration = 5),
            createCycle(cycleLength = 29, periodDuration = 5),
            createCycle(cycleLength = 28, periodDuration = 5),
            createCycle(cycleLength = 27, periodDuration = 6),
            createCycle(cycleLength = 28, periodDuration = 5),
            createCycle(cycleLength = 29, periodDuration = 5)
        )

        val result = calculator.calculate(cycles)

        assertTrue(result.score >= 90)
        assertEquals(ScoreLevel.EXCELLENT, result.level)
    }

    @Test
    fun `calculate should return poor score for irregular cycles`() {
        val cycles = listOf(
            createCycle(cycleLength = 28, periodDuration = 5),
            createCycle(cycleLength = 45, periodDuration = 8),
            createCycle(cycleLength = 21, periodDuration = 3),
            createCycle(cycleLength = 35, periodDuration = 7),
            createCycle(cycleLength = 20, periodDuration = 10),
            createCycle(cycleLength = 40, periodDuration = 6)
        )

        val result = calculator.calculate(cycles)

        assertTrue(result.score < 70)
        assertTrue(result.level == ScoreLevel.POOR || result.level == ScoreLevel.VERY_POOR)
    }

    @Test
    fun `calculate should filter out predicted cycles`() {
        val cycles = listOf(
            createCycle(cycleLength = 28, isPredicted = true),
            createCycle(cycleLength = 29, isPredicted = false),
            createCycle(cycleLength = 28, isPredicted = false),
            createCycle(cycleLength = 27, isPredicted = false)
        )

        val result = calculator.calculate(cycles)

        assertEquals(ScoreLevel.EXCELLENT, result.level)
    }

    @Test
    fun `calculate should return very poor for extremely irregular cycles`() {
        val cycles = listOf(
            createCycle(cycleLength = 15, periodDuration = 2),
            createCycle(cycleLength = 50, periodDuration = 12),
            createCycle(cycleLength = 20, periodDuration = 3),
            createCycle(cycleLength = 45, periodDuration = 10)
        )

        val result = calculator.calculate(cycles)

        assertTrue(result.score < 60)
        assertEquals(ScoreLevel.VERY_POOR, result.level)
    }

    private fun createCycle(
        cycleLength: Int,
        periodDuration: Int = 5,
        isPredicted: Boolean = false
    ): MenstrualCycle {
        return MenstrualCycle(
            cycleId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + periodDuration * 24 * 60 * 60 * 1000,
            cycleLength = cycleLength,
            periodDuration = periodDuration,
            isPredicted = isPredicted
        )
    }
}
```

### 集成测试

```kotlin
@RunWith(AndroidJUnit4::class)
class RegularityScoreIntegrationTest {

    private lateinit var database: WomenHealthDatabase
    private lateinit var dao: MenstrualCycleDao
    private lateinit var repository: RegularityScoreRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WomenHealthDatabase::class.java).build()
        dao = database.menstrualCycleDao()
        repository = RegularityScoreRepository(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `calculateCurrentScore should return accurate score from database`() = runTest {
        // 插入测试数据
        val cycles = (1..6).map { i ->
            MenstrualCycle(
                cycleId = i.toLong(),
                startDate = System.currentTimeMillis() - i * 30L * 24 * 60 * 60 * 1000,
                endDate = System.currentTimeMillis() - i * 30L * 24 * 60 * 60 * 1000 + 5 * 24 * 60 * 60 * 1000,
                cycleLength = 28 + (i % 3),
                periodDuration = 5 + (i % 2),
                isPredicted = false
            )
        }

        cycles.forEach { dao.insert(it) }

        // 计算评分
        val score = repository.calculateCurrentScore()

        // 验证结果
        assertTrue(score.score > 0)
        assertEquals(6, score.statistics.totalCycleCount)
        assertTrue(score.recommendations.isNotEmpty())
    }

    @Test
    fun `calculateHistoricalScores should return monthly scores`() = runTest {
        // 插入12个月的测试数据
        val cycles = (1..12).flatMap { month ->
            (1..3).map { i ->
                MenstrualCycle(
                    cycleId = (month * 10 + i).toLong(),
                    startDate = System.currentTimeMillis() - month * 30L * 24 * 60 * 60 * 1000 - i * 28L * 24 * 60 * 60 * 1000,
                    endDate = System.currentTimeMillis() - month * 30L * 24 * 60 * 60 * 1000 - i * 28L * 24 * 60 * 60 * 1000 + 5 * 24 * 60 * 60 * 1000,
                    cycleLength = 28,
                    periodDuration = 5,
                    isPredicted = false
                )
            }
        }

        cycles.forEach { dao.insert(it) }

        // 计算历史评分
        val historicalScores = repository.calculateHistoricalScores(6)

        // 验证结果
        assertTrue(historicalScores.size > 0)
        historicalScores.forEach { score ->
            assertTrue(score.score > 0)
        }
    }
}
```

### UI测试

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class RegularityScoreUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `should display score when data is available`() {
        // 导航到评分页面
        Espresso.onView(withId(R.id.navigation_health))
            .perform(click())

        Espresso.onView(withId(R.id.regularityScoreCard))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.scoreTextView))
            .check(matches(withText(containsString("分"))))
    }

    @Test
    fun `should show insufficient data message when no cycles`() {
        // 导航到评分页面
        Espresso.onView(withId(R.id.navigation_health))
            .perform(click())

        Espresso.onView(withId(R.id.insufficientDataMessage))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display dimension scores`() {
        // 导航到评分页面
        Espresso.onView(withId(R.id.navigation_health))
            .perform(click())

        Espresso.onView(withId(R.id.dimensionConsistency))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.dimensionNormality))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.dimensionStability))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.dimensionData))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display recommendations`() {
        // 导航到评分页面
        Espresso.onView(withId(R.id.navigation_health))
            .perform(click())

        Espresso.onView(withId(R.id.recommendationsRecyclerView))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.recommendationsRecyclerView))
            .check(RecyclerViewItemCountAssertion.greaterThan(0))
    }
}
```

---

## 风险提示

### 健康风险提示

#### 高风险提示
```
触发条件：
1. 评分 < 60分（VERY_POOR或POOR）
2. 周期长度 < 18天或 > 45天
3. 周期标准差 > 7天
4. 异常周期占比 > 50%

提示内容：
- 您的周期非常不规律，可能提示以下健康问题：
  • 多囊卵巢综合征（PCOS）
  • 甲状腺功能异常
  • 内分泌失调
  • 子宫内膜异位症
- 强烈建议尽快就医检查
- 记录详细的症状和生活日志，帮助医生诊断
```

#### 中风险提示
```
触发条件：
1. 评分 60-79分（FAIR）
2. 周期长度 21-22天或34-35天
3. 周期标准差 5-7天

提示内容：
- 您的周期轻微不规律，建议关注：
  • 保持规律的作息时间
  • 减少压力，适当运动
  • 注意饮食均衡
- 如持续不规律，建议就医检查
```

### 数据准确性提示

#### 数据不足提示
```
触发条件：
1. 周期数量 < 3个
2. 最近3个月内没有记录

提示内容：
- 当前数据不足，无法提供准确的评分
- 请继续记录至少3个周期数据
- 记录的数据越多，评分越准确
```

#### 数据异常提示
```
触发条件：
1. 检测到异常数据（周期长度 < 18天或 > 45天）
2. 数据缺失（缺少结束日期）

提示内容：
- 检测到异常数据，评分可能不准确
- 请检查并修正记录的周期数据
- 如数据确实异常，建议就医检查
```

### 用户引导

#### 初次使用引导
```
触发条件：
1. 用户首次查看评分页面
2. 记录周期数量 < 3个

引导内容：
1. 欢迎使用周期规律性评分功能
2. 该功能可以帮助您了解月经周期健康状况
3. 请至少记录3个完整周期以获得准确评分
4. 记录的周期越多，评分越准确
```

#### 功能说明
```
功能介绍：
- 周期规律性评分基于统计学方法分析您的月经周期
- 评分范围：0-100分，分数越高表示周期越规律
- 评分从4个维度计算：
  • 周期一致性（40%）：周期长度的一致性
  • 周期正常性（30%）：周期长度是否在正常范围
  • 经期稳定性（20%）：经期天数的一致性
  • 数据充足性（10%）：记录数据的数量
- 健康建议基于评分结果提供
```

### 免责声明

```
重要提示：
1. 本评分功能仅供参考，不作为医疗诊断依据
2. 如评分异常或持续不规律，请及时就医
3. 评分结果可能受多种因素影响，如压力、疾病、药物等
4. 如有疑问，请咨询专业医生
```

---

## 附录

### 评分算法流程图

```
开始
  ↓
获取历史周期数据
  ↓
数据验证（至少3个周期）
  ↓
筛选有效周期（排除预测周期、异常周期）
  ↓
选择最近N个周期（默认6个）
  ↓
计算统计数据
  ├─ 平均周期长度
  ├─ 周期标准差
  ├─ 变异系数
  ├─ 平均经期天数
  └─ 经期标准差
  ↓
计算各维度评分
  ├─ 周期一致性（40%）
  ├─ 周期正常性（30%）
  ├─ 经期稳定性（20%）
  └─ 数据充足性（10%）
  ↓
计算综合评分
  ↓
确定评分等级
  ↓
生成健康建议
  ↓
返回评分结果
  ↓
结束
```

### 评分标准速查表

| 综合评分 | 评分等级 | 颜色 | 建议 |
|---------|---------|------|------|
| 90-100 | 非常规律 | 绿色 | 继续保持健康生活方式 |
| 80-89 | 规律 | 浅绿 | 保持良好生活习惯 |
| 70-79 | 轻微不规律 | 黄色 | 注意作息和饮食 |
| 60-69 | 不规律 | 橙色 | 建议就医检查 |
| 0-59 | 非常不规律 | 红色 | 强烈建议就医 |
| 0 | 数据不足 | 灰色 | 继续记录周期数据 |

### 参考数据

#### 正常周期范围
- 标准范围：21-35天
- 平均周期：28天
- 理想周期：25-30天

#### 正常经期范围
- 标准范围：3-7天
- 平均经期：5天
- 理想经期：4-6天

#### 周期规律性标准
- 非常规律：变异系数 < 7%
- 规律：变异系数 7-15%
- 轻微不规律：变异系数 15-25%
- 不规律：变异系数 25-35%
- 非常不规律：变异系数 > 35%

---

**文档结束**