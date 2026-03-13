# 周期变化趋势功能开发文档

## 文档版本
- 版本：v1.0
- 创建日期：2026-02-15
- 文档类型：功能开发规范

---

## 目录

1. [功能概述](#功能概述)
2. [需求分析](#需求分析)
3. [数据模型设计](#数据模型设计)
4. [算法设计](#算法设计)
5. [UI/UX设计](#uiux设计)
6. [技术实现方案](#技术实现方案)
7. [API设计](#api设计)
8. [测试方案](#测试方案)
9. [性能优化](#性能优化)
10. [未来扩展](#未来扩展)

---

## 功能概述

### 功能定位
周期变化趋势分析功能是女性健康管理应用的核心分析模块，通过对用户历史周期数据的深度分析，提供：
- 周期规律性评估
- 周期变化趋势可视化
- 异常周期预警
- 健康风险评估
- 预测模型优化

### 核心价值
- **科学评估**：基于统计学方法评估周期规律性
- **趋势识别**：识别周期长度、症状强度的变化趋势
- **异常预警**：及时发现周期异常，提醒用户关注
- **个性化建议**：根据趋势分析提供个性化健康建议
- **预测优化**：利用趋势数据优化经期预测准确性

### 目标用户
- 青少年女性（初潮-18岁）：建立周期规律性基线
- 育龄期女性：监测周期变化，辅助备孕或避孕
- 备孕女性：识别排卵规律，提高受孕几率
- 围绝经期女性（45岁+）：监测周期紊乱，提前预警

---

## 需求分析

### 用户故事

#### 1. 周期规律性评估
**用户**：我想知道我的月经周期是否规律

**需求**：
- 基于最近3-6个月的周期数据计算规律性
- 显示规律性评级（非常规律/比较规律/不规律）
- 提供规律性解释和建议

**验收标准**：
- 至少3个完整周期数据才能计算
- 规律性评级基于周期长度的标准差
- 提供可视化的规律性指标

---

#### 2. 周期趋势可视化
**用户**：我想看看我的周期长度最近有什么变化

**需求**：
- 显示最近6-12个月的周期长度变化曲线
- 支持按月、按季查看
- 显示平均周期长度和标准差

**验收标准**：
- 图表清晰易读，支持缩放
- 显示趋势线和平均线
- 支持点击查看详细信息

---

#### 3. 症状趋势分析
**用户**：我想知道我的痛经症状最近是变重了还是变轻了

**需求**：
- 显示痛经强度变化趋势
- 显示其他症状（头痛、腰痛等）的出现频率
- 识别症状模式和触发因素

**验收标准**：
- 症状强度使用1-10分级
- 显示症状热力图
- 提供症状缓解建议

---

#### 4. 异常周期预警
**用户**：如果我的周期突然变得很不规律，提醒我

**需求**：
- 检测周期异常（过长、过短、突然变化）
- 显示异常详情和可能原因
- 提供就医建议

**验收标准**：
- 异常检测基于历史数据
- 预警级别分高/中/低
- 提供清晰的可操作建议

---

#### 5. 健康风险评估
**用户**：我想知道我的周期变化是否提示健康问题

**需求**：
- 基于周期数据评估健康风险
- 识别潜在的健康问题（如PCOS、内分泌失调等）
- 提供就医建议和参考资料

**验收标准**：
- 风险评估基于医学指南
- 不做诊断，只做建议
- 提供参考文献

---

### 功能优先级

| 优先级 | 功能模块 | 说明 |
|--------|----------|------|
| P0 | 周期规律性评估 | 核心功能，必须实现 |
| P0 | 周期趋势可视化 | 核心功能，必须实现 |
| P1 | 症状趋势分析 | 重要功能，需要实现 |
| P1 | 异常周期预警 | 重要功能，需要实现 |
| P2 | 健康风险评估 | 高级功能，可选实现 |

---

## 数据模型设计

### 现有数据模型

#### MenstrualCycle（周期实体）
```kotlin
@Entity(tableName = "menstrual_cycles")
data class MenstrualCycle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: Long,                    // 开始日期（时间戳）
    val endDate: Long?,                     // 结束日期（时间戳）
    val cycleLength: Int,                   // 周期长度（天）
    val flowIntensity: String,              // 流量强度：light, medium, heavy
    val symptoms: String?,                  // 症状描述（JSON格式）
    val notes: String?,                     // 备注
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 新增数据模型

#### CycleTrendAnalysis（趋势分析结果）
```kotlin
@Entity(tableName = "cycle_trend_analysis")
data class CycleTrendAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val analysisDate: Long,                 // 分析日期
    val analysisType: String,               // 分析类型：regularity, trend, risk
    val timeRangeStart: Long,               // 分析时间范围开始
    val timeRangeEnd: Long,                 // 分析时间范围结束
    val resultData: String,                 // 分析结果（JSON格式）
    val insights: String?,                  // 洞察和建议
    val confidence: Float,                  // 置信度（0-1）
    val createdAt: Long = System.currentTimeMillis()
)
```

#### CycleRegularotyScore（规律性评分）
```kotlin
data class CycleRegularityScore(
    val score: Float,                       // 规律性评分（0-100）
    val level: RegularityLevel,             // 规律性等级
    val avgCycleLength: Float,              // 平均周期长度
    val stdDeviation: Float,                // 标准差
    val cv: Float,                          // 变异系数（标准差/平均值）
    val range: IntRange,                    // 周期长度范围
    val outliers: List<Long>                // 异常周期ID列表
)

enum class RegularityLevel {
    VERY_REGULAR,       // 非常规律（CV < 0.05）
    REGULAR,            // 比较规律（0.05 <= CV < 0.10）
    SOMEWHAT_IRREGULAR, // 有些不规律（0.10 <= CV < 0.15）
    IRREGULAR           // 不规律（CV >= 0.15）
}
```

#### CycleTrendMetrics（趋势指标）
```kotlin
data class CycleTrendMetrics(
    val slope: Float,                       // 趋势斜率（正数表示周期变长）
    val rSquared: Float,                    // 拟合度（0-1）
    val trendDirection: TrendDirection,     // 趋势方向
    val changeRate: Float,                  // 变化率（每月变化天数）
    val significance: TrendSignificance     // 显著性
)

enum class TrendDirection {
    STABLE,             // 稳定
    INCREASING,         // 增长
    DECREASING,         // 下降
    FLUCTUATING         // 波动
}

enum class TrendSignificance {
    HIGH,               // 高度显著
    MODERATE,           // 中等显著
    LOW,                // 轻微显著
    NOT_SIGNIFICANT     // 不显著
}
```

#### SymptomTrendData（症状趋势数据）
```kotlin
data class SymptomTrendData(
    val symptomType: SymptomType,           // 症状类型
    val averageIntensity: Float,            // 平均强度（1-10）
    val frequency: Float,                   // 出现频率（0-1）
    val trendDirection: TrendDirection,     // 趋势方向
    val severity: SymptomSeverity,          // 严重程度
    val monthlyData: List<SymptomMonthData> // 按月数据
)

data class SymptomMonthData(
    val month: Int,                         // 月份（相对分析起点）
    val totalDays: Int,                     // 该月记录天数
    val occurrenceDays: Int,                // 出现天数
    val avgIntensity: Float,                // 平均强度
    val maxIntensity: Int                   // 最大强度
)

enum class SymptomType {
    DYSMENORRHEA,       // 痛经
    HEADACHE,           // 头痛
    BACK_PAIN,          // 腰痛
    BREAST_PAIN,        // 乳房胀痛
    BLOATING,           // 腹胀
    ACNE,               // 痤疮
    INSOMNIA,           // 失眠
    FATIGUE             // 疲劳
}

enum class SymptomSeverity {
    MILD,               // 轻度
    MODERATE,           // 中度
    SEVERE              // 重度
}
```

#### CycleAnomaly（周期异常）
```kotlin
data class CycleAnomaly(
    val cycleId: Long,                      // 周期ID
    val anomalyType: AnomalyType,           // 异常类型
    val severity: AnomalySeverity,          // 严重程度
    val expectedRange: IntRange,            // 预期范围
    val actualValue: Int,                   // 实际值
    val deviation: Float,                   // 偏差（绝对值）
    val deviationPercentage: Float,         // 偏差百分比
    val possibleCauses: List<String>,       // 可能原因
    val recommendations: List<String>,      // 建议
    val shouldConsultDoctor: Boolean        // 是否建议就医
)

enum class AnomalyType {
    TOO_SHORT,          // 周期过短
    TOO_LONG,           // 周期过长
    SUDDEN_SHORTENING,  // 突然缩短
    SUDDEN_LENGTHENING, // 突然延长
    EXTREME_FLUCTUATION // 极端波动
}

enum class AnomalySeverity {
    LOW,                // 低度异常
    MEDIUM,             // 中度异常
    HIGH                // 高度异常
}
```

#### HealthRiskAssessment（健康风险评估）
```kotlin
data class HealthRiskAssessment(
    val riskType: HealthRiskType,           // 风险类型
    val riskLevel: RiskLevel,               // 风险等级
    val probability: Float,                 // 概率（0-1）
    val evidence: List<String>,             // 证据
    val recommendations: List<String>,      // 建议
    val referenceLinks: List<String>        // 参考链接
)

enum class HealthRiskType {
    PCOS,               // 多囊卵巢综合征
    HYPOTHYROIDISM,     // 甲状腺功能减退
    ANEMIA,             // 贫血
    STRESS_RELATED,     // 压力相关
    PERIMENOPAUSE,      // 围绝经期
    NORMAL_VARIATION    // 正常变异
}

enum class RiskLevel {
    LOW,                // 低风险
    MODERATE,           // 中等风险
    HIGH                // 高风险
}
```

---

## 算法设计

### 1. 周期规律性评估算法

#### 算法概述
使用统计学方法评估周期规律性，基于最近3-6个周期数据。

#### 输入
- 周期列表：List<MenstrualCycle>
- 周期数量：3-6个

#### 输出
- 规律性评分：CycleRegularityScore

#### 算法步骤

**Step 1: 数据预处理**
```kotlin
fun prepareCycles(cycles: List<MenstrualCycle>): List<Int> {
    return cycles
        .filter { !it.endDate.isNullOrBlank() }
        .map { it.cycleLength }
        .sorted()
}
```

**Step 2: 计算统计指标**
```kotlin
fun calculateStatistics(cycleLengths: List<Int>): Statistics {
    val n = cycleLengths.size
    val mean = cycleLengths.sum().toDouble() / n
    val variance = cycleLengths.map { (it - mean) * (it - mean) }.sum() / (n - 1)
    val stdDev = sqrt(variance)
    val cv = stdDev / mean  // 变异系数

    return Statistics(
        mean = mean,
        stdDev = stdDev,
        cv = cv,
        range = cycleLengths.first()..cycleLengths.last()
    )
}
```

**Step 3: 规律性评级**
```kotlin
fun assessRegularity(cv: Float, n: Int): RegularityLevel {
    // 考虑样本数量的调整因子
    val adjustmentFactor = when {
        n >= 6 -> 1.0
        n >= 4 -> 0.9
        else -> 0.8
    }

    val adjustedCV = cv / adjustmentFactor

    return when {
        adjustedCV < 0.05 -> RegularityLevel.VERY_REGULAR
        adjustedCV < 0.10 -> RegularityLevel.REGULAR
        adjustedCV < 0.15 -> RegularityLevel.SOMEWHAT_IRREGULAR
        else -> RegularityLevel.IRREGULAR
    }
}
```

**Step 4: 计算规律性评分**
```kotlin
fun calculateRegularityScore(regularityLevel: RegularityLevel, cv: Float): Float {
    return when (regularityLevel) {
        RegularityLevel.VERY_REGULAR -> 90 + (1 - cv / 0.05) * 10
        RegularityLevel.REGULAR -> 70 + (1 - cv / 0.10) * 20
        RegularityLevel.SOMEWHAT_IRREGULAR -> 50 + (1 - cv / 0.15) * 20
        RegularityLevel.IRREGULAR -> maxOf(0f, 50 - (cv - 0.15) * 100)
    }.coerceIn(0f, 100f)
}
```

**Step 5: 检测异常周期**
```kotlin
fun detectOutliers(cycleLengths: List<Int>, mean: Double, stdDev: Double): List<Int> {
    val lowerBound = mean - 2 * stdDev
    val upperBound = mean + 2 * stdDev

    return cycleLengths.filter { it < lowerBound || it > upperBound }
}
```

**Step 6: 生成洞察**
```kotlin
fun generateRegularityInsights(score: CycleRegularityScore): List<String> {
    val insights = mutableListOf<String>()

    when (score.level) {
        RegularityLevel.VERY_REGULAR -> {
            insights.add("您的月经周期非常规律，健康状况良好。")
            insights.add("规律周期是身体健康的重要指标。")
        }
        RegularityLevel.REGULAR -> {
            insights.add("您的月经周期比较规律，基本正常。")
            insights.add("继续保持良好的生活习惯。")
        }
        RegularityLevel.SOMEWHAT_IRREGULAR -> {
            insights.add("您的月经周期有些不规律，建议关注。")
            insights.add("可能是压力、饮食或睡眠变化导致的。")
            if (score.outliers.isNotEmpty()) {
                insights.add("检测到${score.outliers.size}个异常周期，建议记录更多细节。")
            }
        }
        RegularityLevel.IRREGULAR -> {
            insights.add("您的月经周期明显不规律，建议就医咨询。")
            insights.add("周期不规律可能与多种因素有关，需要专业评估。")
            insights.add("建议记录症状、饮食、运动等详细信息。")
        }
    }

    return insights
}
```

#### 复杂度分析
- 时间复杂度：O(n)，n为周期数量
- 空间复杂度：O(n)

---

### 2. 周期趋势分析算法

#### 算法概述
使用线性回归分析周期长度的变化趋势。

#### 输入
- 周期列表：List<MenstrualCycle>
- 最小周期数量：4个

#### 输出
- 趋势指标：CycleTrendMetrics

#### 算法步骤

**Step 1: 数据准备**
```kotlin
fun prepareTrendData(cycles: List<MenstrualCycle>): List<Pair<Int, Int>> {
    // 按时间排序
    val sortedCycles = cycles.sortedBy { it.startDate }

    // 生成时间序列（月份）
    val firstMonth = sortedCycles.first().startDate.toMonth()
    return sortedCycles.mapIndexed { index, cycle ->
        val monthIndex = (cycle.startDate.toMonth() - firstMonth).toInt()
        Pair(monthIndex, cycle.cycleLength)
    }
}
```

**Step 2: 线性回归**
```kotlin
fun linearRegression(data: List<Pair<Int, Int>>): RegressionResult {
    val n = data.size
    val sumX = data.sumOf { it.first.toDouble() }
    val sumY = data.sumOf { it.second.toDouble() }
    val sumXY = data.sumOf { it.first.toDouble() * it.second.toDouble() }
    val sumX2 = data.sumOf { it.first.toDouble() * it.first.toDouble() }
    val sumY2 = data.sumOf { it.second.toDouble() * it.second.toDouble() }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    // 计算R²（拟合度）
    val meanY = sumY / n
    val ssTotal = data.sumOf { (it.second - meanY) * (it.second - meanY) }
    val ssResidual = data.sumOf {
        val predicted = slope * it.first + intercept
        (it.second - predicted) * (it.second - predicted)
    }
    val rSquared = 1 - ssResidual / ssTotal

    return RegressionResult(
        slope = slope,
        intercept = intercept,
        rSquared = rSquared
    )
}
```

**Step 3: 趋势方向判断**
```kotlin
fun determineTrendDirection(slope: Float, rSquared: Float): TrendDirection {
    val significance = rSquared > 0.7

    return when {
        !significance || abs(slope) < 0.1 -> TrendDirection.STABLE
        slope > 0.5 -> TrendDirection.INCREASING
        slope < -0.5 -> TrendDirection.DECREASING
        slope > 0 -> TrendDirection.INCREASING
        else -> TrendDirection.DECREASING
    }
}
```

**Step 4: 变化率计算**
```kotlin
fun calculateChangeRate(slope: Float): Float {
    // 斜率表示每月变化的天数
    return slope
}
```

**Step 5: 显著性评估**
```kotlin
fun assessSignificance(rSquared: Float, n: Int): TrendSignificance {
    // 考虑样本数量的调整
    val adjustedRSquared = rSquared * (n - 1) / (n - 2)

    return when {
        adjustedRSquared > 0.8 -> TrendSignificance.HIGH
        adjustedRSquared > 0.5 -> TrendSignificance.MODERATE
        adjustedRSquared > 0.3 -> TrendSignificance.LOW
        else -> TrendSignificance.NOT_SIGNIFICANT
    }
}
```

**Step 6: 生成趋势洞察**
```kotlin
fun generateTrendInsights(metrics: CycleTrendMetrics): List<String> {
    val insights = mutableListOf<String>()

    when (metrics.trendDirection) {
        TrendDirection.STABLE -> {
            insights.add("您的周期长度保持稳定。")
            if (metrics.significance == TrendSignificance.HIGH) {
                insights.add("数据显示您的周期非常稳定。")
            }
        }
        TrendDirection.INCREASING -> {
            val changeDesc = if (abs(metrics.changeRate) >= 1.0) {
                "${metrics.changeRate.toInt()}天/月"
            } else {
                "${(metrics.changeRate * 30).toInt()}天/年"
            }
            insights.add("您的周期长度在增加，平均每月增加$changeDesc。")
            if (metrics.significance == TrendSignificance.HIGH) {
                insights.add("这一趋势较为明显，建议关注。")
                insights.add("可能与年龄、激素变化或生活方式有关。")
            }
        }
        TrendDirection.DECREASING -> {
            val changeDesc = if (abs(metrics.changeRate) >= 1.0) {
                "${abs(metrics.changeRate).toInt()}天/月"
            } else {
                "${(abs(metrics.changeRate) * 30).toInt()}天/年"
            }
            insights.add("您的周期长度在减少，平均每月减少$changeDesc。")
            if (metrics.significance == TrendSignificance.HIGH) {
                insights.add("这一趋势较为明显，建议关注。")
            }
        }
        TrendDirection.FLUCTUATING -> {
            insights.add("您的周期长度波动较大。")
            insights.add("可能与压力、饮食或睡眠变化有关。")
        }
    }

    return insights
}
```

#### 复杂度分析
- 时间复杂度：O(n)，n为周期数量
- 空间复杂度：O(n)

---

### 3. 症状趋势分析算法

#### 算法概述
分析症状的出现频率和强度变化趋势。

#### 输入
- 症状记录列表：List<SymptomRecord>
- 时间范围：3-12个月

#### 输出
- 症状趋势数据：List<SymptomTrendData>

#### 算法步骤

**Step 1: 症状数据聚合**
```kotlin
fun aggregateSymptomsByMonth(
    records: List<SymptomRecord>,
    months: Int
): Map<SymptomType, List<SymptomMonthData>> {
    val result = mutableMapOf<SymptomType, MutableList<SymptomMonthData>>()
    val startDate = records.minByOrNull { it.date }?.date ?: return result

    records.forEach { record ->
        val monthIndex = getMonthIndex(record.date, startDate, months)

        // 痛经
        if (record.dysmenorrheaLevel > 0) {
            addSymptomData(result, SymptomType.DYSMENORRHEA, monthIndex, record.dysmenorrheaLevel)
        }

        // 头痛
        if (record.hasHeadache) {
            addSymptomData(result, SymptomType.HEADACHE, monthIndex, 5.0f)
        }

        // 其他症状...
    }

    return result
}
```

**Step 2: 计算趋势指标**
```kotlin
fun calculateSymptomTrend(monthlyData: List<SymptomMonthData>): SymptomTrendData {
    val avgIntensity = monthlyData.map { it.avgIntensity }.average().toFloat()
    val frequency = monthlyData.map { it.occurrenceDays.toFloat() / it.totalDays }.average()

    // 线性回归分析趋势
    val intensities = monthlyData.map { it.avgIntensity.toDouble() }
    val regression = linearRegression(intensities.indices.zip(intensities))

    val trendDirection = determineTrendDirection(regression.slope.toFloat(), regression.rSquared)

    // 严重程度评估
    val severity = when {
        avgIntensity >= 8 || frequency >= 0.8 -> SymptomSeverity.SEVERE
        avgIntensity >= 5 || frequency >= 0.5 -> SymptomSeverity.MODERATE
        else -> SymptomSeverity.MILD
    }

    return SymptomTrendData(
        symptomType = SymptomType.DYSMENORRHEA,
        averageIntensity = avgIntensity,
        frequency = frequency,
        trendDirection = trendDirection,
        severity = severity,
        monthlyData = monthlyData
    )
}
```

**Step 3: 生成症状洞察**
```kotlin
fun generateSymptomInsights(trendData: SymptomTrendData): List<String> {
    val insights = mutableListOf<String>()

    val symptomName = when (trendData.symptomType) {
        SymptomType.DYSMENORRHEA -> "痛经"
        SymptomType.HEADACHE -> "头痛"
        SymptomType.BACK_PAIN -> "腰痛"
        SymptomType.BREAST_PAIN -> "乳房胀痛"
        SymptomType.BLOATING -> "腹胀"
        SymptomType.ACNE -> "痤疮"
        SymptomType.INSOMNIA -> "失眠"
        SymptomType.FATIGUE -> "疲劳"
    }

    insights.add("您的${symptomName}平均强度为${String.format("%.1f", trendData.averageIntensity)}分。")
    insights.add("${symptomName}出现频率为${String.format("%.0f", trendData.frequency * 100)}%。")

    when (trendData.severity) {
        SymptomSeverity.MILD -> {
            insights.add("${symptomName}程度较轻，属于正常范围。")
        }
        SymptomSeverity.MODERATE -> {
            insights.add("${symptomName}程度中等，建议关注。")
            when (trendData.trendDirection) {
                TrendDirection.INCREASING -> insights.add("${symptomName}有加重趋势，建议调整生活方式。")
                TrendDirection.DECREASING -> insights.add("${symptomName}有改善趋势，继续保持。")
                else -> {}
            }
        }
        SymptomSeverity.SEVERE -> {
            insights.add("${symptomName}程度较重，建议就医咨询。")
            when (trendData.trendDirection) {
                TrendDirection.INCREASING -> insights.add("${symptomName}持续加重，请尽快就医。")
                else -> {}
            }
        }
    }

    return insights
}
```

#### 复杂度分析
- 时间复杂度：O(n)，n为记录数量
- 空间复杂度：O(n)

---

### 4. 异常周期检测算法

#### 算法概述
基于历史数据检测异常周期。

#### 输入
- 周期列表：List<MenstrualCycle>
- 最小周期数量：4个

#### 输出
- 异常周期列表：List<CycleAnomaly>

#### 算法步骤

**Step 1: 计算基线统计**
```kotlin
fun calculateBaseline(cycles: List<MenstrualCycle>): BaselineStats {
    val cycleLengths = cycles.map { it.cycleLength }
    val mean = cycleLengths.average()
    val stdDev = cycleLengths.map { (it - mean) * (it - mean) }.average().let { sqrt(it) }

    return BaselineStats(
        mean = mean,
        stdDev = stdDev,
        lowerBound = mean - 2 * stdDev,
        upperBound = mean + 2 * stdDev
    )
}
```

**Step 2: 检测异常**
```kotlin
fun detectAnomalies(
    cycles: List<MenstrualCycle>,
    baseline: BaselineStats
): List<CycleAnomaly> {
    return cycles.mapNotNull { cycle ->
        val length = cycle.cycleLength
        val deviation = abs(length - baseline.mean)
        val deviationPercentage = (deviation / baseline.mean) * 100

        val anomalyType = when {
            length < baseline.lowerBound && deviationPercentage > 30 -> AnomalyType.TOO_SHORT
            length > baseline.upperBound && deviationPercentage > 30 -> AnomalyType.TOO_LONG
            length < baseline.mean - baseline.stdDev -> AnomalyType.SUDDEN_SHORTENING
            length > baseline.mean + baseline.stdDev -> AnomalyType.SUDDEN_LENGTHENING
            else -> null
        }

        anomalyType?.let { type ->
            val severity = when (deviationPercentage) {
                in 0.0..15.0 -> AnomalySeverity.LOW
                in 15.0..30.0 -> AnomalySeverity.MEDIUM
                else -> AnomalySeverity.HIGH
            }

            CycleAnomaly(
                cycleId = cycle.id,
                anomalyType = type,
                severity = severity,
                expectedRange = baseline.lowerBound.toInt()..baseline.upperBound.toInt(),
                actualValue = length,
                deviation = deviation,
                deviationPercentage = deviationPercentage,
                possibleCauses = getPossibleCauses(type),
                recommendations = getRecommendations(type, severity),
                shouldConsultDoctor = severity == AnomalySeverity.HIGH
            )
        }
    }
}
```

**Step 3: 生成可能原因**
```kotlin
fun getPossibleCauses(anomalyType: AnomalyType): List<String> {
    return when (anomalyType) {
        AnomalyType.TOO_SHORT -> listOf(
            "压力过大",
            "营养不良",
            "过度运动",
            "激素失调"
        )
        AnomalyType.TOO_LONG -> listOf(
            "压力过大",
            "体重变化",
            "激素失调",
            "多囊卵巢综合征（PCOS）"
        )
        AnomalyType.SUDDEN_SHORTENING -> listOf(
            "生活方式改变",
            "饮食变化",
            "压力增加",
            "药物影响"
        )
        AnomalyType.SUDDEN_LENGTHENING -> listOf(
            "生活方式改变",
            "体重增加",
            "压力增加",
            "激素变化"
        )
        AnomalyType.EXTREME_FLUCTUATION -> listOf(
            "压力过大",
            "饮食不规律",
            "睡眠不足",
            "激素失调"
        )
    }
}
```

**Step 4: 生成建议**
```kotlin
fun getRecommendations(
    anomalyType: AnomalyType,
    severity: AnomalySeverity
): List<String> {
    val recommendations = mutableListOf<String>()

    recommendations.add("记录更多周期数据以确认趋势。")
    recommendations.add("保持规律的作息和饮食。")
    recommendations.add("适当运动，但避免过度。")

    if (severity == AnomalySeverity.HIGH) {
        recommendations.add("建议咨询妇科医生。")
        recommendations.add("可能需要激素检查。")
    }

    return recommendations
}
```

#### 复杂度分析
- 时间复杂度：O(n)，n为周期数量
- 空间复杂度：O(n)

---

### 5. 健康风险评估算法

#### 算法概述
基于周期数据和症状数据评估健康风险。

#### 输入
- 周期列表：List<MenstrualCycle>
- 症状记录列表：List<SymptomRecord>
- 用户年龄：Int

#### 输出
- 健康风险评估：List<HealthRiskAssessment>

#### 算法步骤

**Step 1: PCOS风险评估**
```kotlin
fun assessPCOSRisk(
    cycles: List<MenstrualCycle>,
    symptoms: List<SymptomRecord>
): HealthRiskAssessment? {
    // Rotterdam标准（3选2）
    var criteriaCount = 0

    // 标准1：月经稀发或闭经
    val avgCycleLength = cycles.map { it.cycleLength }.average()
    if (avgCycleLength > 35 || avgCycleLength < 21) {
        criteriaCount++
    }

    // 标准2：高雄激素血症（基于症状）
    val hasAcne = symptoms.any { it.hasAcne }
    val hasBloating = symptoms.any { it.hasBloating }
    if (hasAcne || hasBloating) {
        criteriaCount++
    }

    // 标准3：卵巢多囊（需超声，这里无法判断，假设为否）

    val probability = when (criteriaCount) {
        2 -> 0.7f
        1 -> 0.4f
        else -> 0.1f
    }

    if (probability < 0.3) return null

    val riskLevel = when {
        probability > 0.6 -> RiskLevel.HIGH
        probability > 0.4 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
    }

    return HealthRiskAssessment(
        riskType = HealthRiskType.PCOS,
        riskLevel = riskLevel,
        probability = probability,
        evidence = listOf(
            "周期不规律" to (avgCycleLength > 35 || avgCycleLength < 21),
            "相关症状" to (hasAcne || hasBloating)
        ).filter { it.second }.map { it.first },
        recommendations = listOf(
            "建议咨询妇科内分泌科医生",
            "可能需要进行激素水平检查",
            "可能需要进行超声检查"
        ),
        referenceLinks = listOf(
            "https://www.mayoclinic.org/diseases-conditions/pcos/symptoms-causes/syc-20353439"
        )
    )
}
```

**Step 2: 贫血风险评估**
```kotlin
fun assessAnemiaRisk(
    cycles: List<MenstrualCycle>,
    symptoms: List<SymptomRecord>
): HealthRiskAssessment? {
    // 评估经血量
    val heavyPeriods = cycles.count { it.flowIntensity == "heavy" }
    val totalPeriods = cycles.size
    val heavyPeriodRatio = heavyPeriods.toFloat() / totalPeriods

    // 评估症状
    val hasFatigue = symptoms.any { it.hasFatigue }
    val hasHeadache = symptoms.any { it.hasHeadache }
    val hasInsomnia = symptoms.any { it.hasInsomnia }

    var riskScore = 0
    if (heavyPeriodRatio > 0.5) riskScore += 2
    if (heavyPeriodRatio > 0.3) riskScore += 1
    if (hasFatigue) riskScore += 1
    if (hasHeadache) riskScore += 1
    if (hasInsomnia) riskScore += 1

    val probability = minOf(1.0f, riskScore * 0.15f)

    if (probability < 0.3) return null

    val riskLevel = when {
        probability > 0.6 -> RiskLevel.HIGH
        probability > 0.4 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
    }

    return HealthRiskAssessment(
        riskType = HealthRiskType.ANEMIA,
        riskLevel = riskLevel,
        probability = probability,
        evidence = listOf(
            "经血量大" to (heavyPeriodRatio > 0.3),
            "疲劳症状" to hasFatigue,
            "头痛症状" to hasHeadache,
            "失眠症状" to hasInsomnia
        ).filter { it.second }.map { it.first },
        recommendations = listOf(
            "建议进行血常规检查",
            "考虑补充铁剂",
            "增加富含铁的食物摄入"
        ),
        referenceLinks = listOf(
            "https://www.mayoclinic.org/diseases-conditions/iron-deficiency-anemia/symptoms-causes/syc-20355040"
        )
    )
}
```

**Step 3: 围绝经期风险评估**
```kotlin
fun assessPerimenopauseRisk(
    cycles: List<MenstrualCycle>,
    age: Int
): HealthRiskAssessment? {
    if (age < 40) return null

    // 评估周期变化
    val cycleLengths = cycles.map { it.cycleLength }
    val stdDev = sqrt(cycleLengths.map { (it - cycleLengths.average()) * (it - cycleLengths.average()) }.average())

    // 评估趋势
    val regression = linearRegression(cycleLengths.indices.zip(cycleLengths.map { it.toDouble() }))

    var probability = 0f
    val evidence = mutableListOf<String>()

    if (age >= 45) {
        probability += 0.3f
        evidence.add("年龄45岁以上")
    }
    if (stdDev > 7) {
        probability += 0.3f
        evidence.add("周期变化大")
    }
    if (regression.slope > 0.5 && regression.rSquared > 0.5) {
        probability += 0.2f
        evidence.add("周期延长趋势")
    }
    if (cycleLengths.any { it > 40 }) {
        probability += 0.2f
        evidence.add("出现长周期")
    }

    if (probability < 0.4) return null

    val riskLevel = when {
        probability > 0.7 -> RiskLevel.HIGH
        probability > 0.5 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
    }

    return HealthRiskAssessment(
        riskType = HealthRiskType.PERIMENOPAUSE,
        riskLevel = riskLevel,
        probability = probability,
        evidence = evidence,
        recommendations = listOf(
            "这是正常的生理过程",
            "建议定期妇科检查",
            "如有严重症状，可咨询激素替代治疗"
        ),
        referenceLinks = listOf(
            "https://www.mayoclinic.org/healthy-lifestyle/womens-health/in-depth/perimenopause/art-20046173"
        )
    )
}
```

#### 复杂度分析
- 时间复杂度：O(n)，n为周期数量
- 空间复杂度：O(n)

---

## UI/UX设计

### 整体设计原则
- **Material Design 3**：遵循最新Material Design规范
- **数据可视化**：优先使用图表展示数据
- **渐进式揭示**：重要信息优先，细节可展开
- **色彩心理学**：使用色彩传达信息（红=警示，绿=正常）
- **响应式设计**：适配不同屏幕尺寸

### 主要界面设计

#### 1. 趋势分析主界面（TrendAnalysisFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  周期趋势分析                      [返回]│
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  规律性评分卡片                      │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  85分                        │  │ │
│  │  │  [进度环] 比较规律           │  │ │
│  │  │  平均周期：28天               │  │ │
│  │  │  标准差：1.5天                │  │ │
│  │  └───────────────────────────────┘  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  周期趋势图                          │ │
│  │  [折线图 - 周期长度变化]             │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  平均：28天                   │  │ │
│  │  │  趋势：稳定                   │  │ │
│  │  │  [查看详情]                   │  │ │
│  │  └───────────────────────────────┘  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  症状趋势                            │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  痛经：[趋势图]               │  │ │
│  │  │  其他症状：[展开]             │  │ │
│  │  └───────────────────────────────┘  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  洞察与建议                          │ │
│  │  • 您的月经周期比较规律             │ │
│  │  • 继续保持良好的生活习惯           │ │
│  │  [查看更多建议]                     │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**组件设计**
- 规律性评分卡片：使用环形进度条显示评分
- 趋势图：使用 MPAndroidChart 绘制折线图
- 症状趋势：使用迷你图表或进度条
- 洞察建议：使用卡片列表

---

#### 2. 周期规律性详情界面（RegularityDetailFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  周期规律性详情               [返回]     │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  规律性评分                          │ │
│  │  [大号分数：85分]                   │ │
│  │  等级：比较规律                     │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  统计数据                            │ │
│  │  • 平均周期长度：28天               │ │
│  │  • 最短周期：26天                   │ │
│  │  • 最长周期：30天                   │ │
│  │  • 标准差：1.5天                    │ │
│  │  • 变异系数：5.4%                   │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  历史周期分布                        │ │
│  │  [柱状图 - 周期长度分布]            │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  洞察与解释                          │ │
│  │  变异系数（CV）用于评估周期规律性   │ │
│  │  • CV < 5%：非常规律               │ │
│  │  • 5% ≤ CV < 10%：比较规律         │ │
│  │  • 10% ≤ CV < 15%：有些不规律      │ │
│  │  • CV ≥ 15%：不规律                │ │
│  │  您的CV为5.4%，属于比较规律。       │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

#### 3. 周期趋势详情界面（TrendDetailFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  周期趋势详情                 [返回]     │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  时间范围选择                        │ │
│  │  [3个月] [6个月] [12个月] [全部]   │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  周期长度趋势图                      │ │
│  │  [折线图 + 平均线 + 趋势线]         │ │
│  │  支持缩放、滑动                      │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  趋势统计                            │ │
│  │  • 趋势方向：稳定                   │ │
│  │  • 变化率：0.1天/月                 │ │
│  │  • 拟合度：0.72                     │ │
│  │  • 显著性：中等                     │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  洞察与建议                          │ │
│  │  • 您的周期长度保持稳定             │ │
│  │  • 稳定的周期是健康的标志           │ │
│  │  • 继续保持当前的生活方式           │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

#### 4. 症状趋势详情界面（SymptomTrendFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  症状趋势分析                 [返回]     │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  症状选择                            │ │
│  │  [痛经 ▼] [头痛] [腰痛] [其他...]  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  痛经强度趋势                        │ │
│  │  [折线图 - 强度变化]                │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  痛状统计                            │ │
│  │  • 平均强度：4.5分                 │ │
│  │  • 出现频率：75%                    │ │
│  │  • 严重程度：中等                   │ │
│  │  • 趋势方向：稳定                   │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  痛经强度分布                        │ │
│  │  [饼图或柱状图 - 强度分布]          │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  缓解建议                            │ │
│  │  • 热敷腹部                         │ │
│  │  • 服用止痛药                       │ │
│  │  • 轻度运动                         │ │
│  │  [查看更多建议]                     │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

#### 5. 异常周期预警界面（AnomalyAlertFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  异常周期预警                 [返回]     │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  预警级别                            │ │
│  │  [红色] 高度异常：检测到2个异常周期 │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  异常周期列表                        │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  2026-01-15 - 2026-02-10     │  │ │
│  │  │  周期长度：41天                │  │ │
│  │  │  异常类型：周期过长            │  │ │
│  │  │  严重程度：高                  │  │ │
│  │  │  [查看详情]                   │  │ │
│  │  └───────────────────────────────┘  │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  2025-12-05 - 2025-12-30     │  │ │
│  │  │  周期长度：18天                │  │ │
│  │  │  异常类型：周期过短            │  │ │
│  │  │  严重程度：中                  │  │ │
│  │  │  [查看详情]                   │  │ │
│  │  └───────────────────────────────┘  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  可能原因                            │ │
│  │  • 压力过大                         │ │
│  │  • 体重变化                         │ │
│  │  • 激素失调                         │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  建议操作                            │ │
│  │  [记录更多细节] [咨询医生]          │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

#### 6. 健康风险评估界面（HealthRiskFragment）

**布局结构**
```
┌─────────────────────────────────────────┐
│  健康风险评估                 [返回]     │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  风险概览                            │ │
│  │  • 高风险：0                        │ │
│  │  • 中等风险：1                      │ │
│  │  • 低风险：2                        │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  风险列表                            │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  [黄色] 中等风险：贫血        │  │ │
│  │  │  概率：45%                    │  │ │
│  │  │  证据：经血量大、疲劳症状    │  │ │
│  │  │  [查看详情]                   │  │ │
│  │  └───────────────────────────────┘  │ │
│  │  ┌───────────────────────────────┐  │ │
│  │  │  [绿色] 低风险：PCOS          │  │ │
│  │  │  概率：25%                    │  │ │
│  │  │  证据：周期略长               │  │ │
│  │  │  [查看详情]                   │  │ │
│  │  └───────────────────────────────┘  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │  免责声明                            │ │
│  │  本评估仅供参考，不做医疗诊断。     │ │
│  │  如有健康问题，请咨询专业医生。     │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

### 颜色方案

#### 状态色
```xml
<!-- 规律性评分 -->
<color name="regularity_very_good">#4CAF50</color>    <!-- 绿色 -->
<color name="regularity_good">#8BC34A</color>          <!-- 浅绿 -->
<color name="regularity_fair">#FFC107</color>          <!-- 黄色 -->
<color name="regularity_poor">#FF9800</color>          <!-- 橙色 -->
<color name="regularity_bad">#F44336</color>           <!-- 红色 -->

<!-- 趋势方向 -->
<color name="trend_stable">#2196F3</color>             <!-- 蓝色 -->
<color name="trend_increasing">#FF5722</color>         <!-- 橙红 -->
<color name="trend_decreasing">#9C27B0</color>         <!-- 紫色 -->

<!-- 症状严重程度 -->
<color name="symptom_mild">#81C784</color>             <!-- 浅绿 -->
<color name="symptom_moderate">#FFB74D</color>         <!-- 浅橙 -->
<color name="symptom_severe">#E57373</color>           <!-- 浅红 -->

<!-- 风险等级 -->
<color name="risk_low">#81C784</color>                 <!-- 浅绿 -->
<color name="risk_moderate">#FFB74D</color>            <!-- 浅橙 -->
<color name="risk_high">#E57373</color>                <!-- 浅红 -->

<!-- 异常严重程度 -->
<color name="anomaly_low">#FFC107</color>              <!-- 黄色 -->
<color name="anomaly_medium">#FF9800</color>           <!-- 橙色 -->
<color name="anomaly_high">#F44336</color>             <!-- 红色 -->
```

---

### 交互设计

#### 1. 图表交互
- **点击**：显示数据点的详细信息
- **长按**：高亮显示数据点
- **双击**：重置缩放
- **捏合**：缩放图表
- **滑动**：平移图表

#### 2. 卡片交互
- **点击**：展开详细信息
- **滑动**：左右切换不同数据
- **长按**：显示更多操作菜单

#### 3. 筛选交互
- **下拉选择**：选择时间范围
- **标签切换**：切换不同症状类型
- **开关**：启用/禁用功能

---

## 技术实现方案

### 技术栈

#### 图表库
- **MPAndroidChart**：用于绘制趋势图、柱状图、饼图
- **版本**：v3.1.0

#### 数据库
- **Room Database**：用于存储分析结果
- **版本**：v2.6.1

#### 协程
- **Kotlin Coroutines**：用于异步数据处理
- **Flow**：用于响应式数据流

#### 依赖注入
- **Hilt**：用于依赖注入
- **版本**：v2.48

#### 日期处理
- **ThreeTen Android Backport**：用于日期计算
- **版本**：v1.4.6

---

### 项目结构

```
app/src/main/java/com/example/womenhealthapp/
├── data/
│   ├── dao/
│   │   ├── MenstrualCycleDao.kt
│   │   ├── SymptomRecordDao.kt
│   │   └── CycleTrendAnalysisDao.kt        # 新增
│   ├── database/
│   │   └── WomenHealthDatabase.kt
│   ├── entity/
│   │   ├── MenstrualCycle.kt
│   │   ├── SymptomRecord.kt
│   │   └── CycleTrendAnalysis.kt          # 新增
│   └── repository/
│       └── WomenHealthRepository.kt
├── analysis/                               # 新增
│   ├── regularity/
│   │   ├── CycleRegularityAnalyzer.kt
│   │   ├── CycleRegularityScore.kt
│   │   └── RegularityLevel.kt
│   ├── trend/
│   │   ├── CycleTrendAnalyzer.kt
│   │   ├── CycleTrendMetrics.kt
│   │   ├── TrendDirection.kt
│   │   └── TrendSignificance.kt
│   ├── symptom/
│   │   ├── SymptomTrendAnalyzer.kt
│   │   ├── SymptomTrendData.kt
│   │   ├── SymptomType.kt
│   │   └── SymptomSeverity.kt
│   ├── anomaly/
│   │   ├── AnomalyDetector.kt
│   │   ├── CycleAnomaly.kt
│   │   ├── AnomalyType.kt
│   │   └── AnomalySeverity.kt
│   └── risk/
│       ├── HealthRiskAssessor.kt
│       ├── HealthRiskAssessment.kt
│       ├── HealthRiskType.kt
│       └── RiskLevel.kt
├── ui/
│   ├── trend/                               # 新增
│   │   ├── TrendAnalysisFragment.kt
│   │   ├── RegularityDetailFragment.kt
│   │   ├── TrendDetailFragment.kt
│   │   ├── SymptomTrendFragment.kt
│   │   ├── AnomalyAlertFragment.kt
│   │   └── HealthRiskFragment.kt
│   ├── cycle/
│   │   ├── CycleFragment.kt
│   │   └── CycleAdapter.kt
│   └── health/
│       └── HealthFragment.kt
├── viewmodel/
│   ├── WomenHealthViewModel.kt
│   └── TrendAnalysisViewModel.kt            # 新增
└── utils/
    ├── StatisticsUtils.kt                   # 新增
    ├── ChartUtils.kt                        # 新增
    └── DateUtils.kt
```

---

### 核心实现

#### 1. 周期规律性分析器

```kotlin
class CycleRegularityAnalyzer {
    suspend fun analyze(
        cycles: List<MenstrualCycle>,
        minCycles: Int = 3
    ): Result<CycleRegularityScore> {
        if (cycles.size < minCycles) {
            return Result.failure(InsufficientDataException("需要至少$minCycles 个周期数据"))
        }

        val cycleLengths = cycles.map { it.cycleLength }
        val statistics = calculateStatistics(cycleLengths)
        val regularityLevel = assessRegularity(statistics.cv, cycles.size)
        val score = calculateRegularityScore(regularityLevel, statistics.cv)
        val outliers = detectOutliers(cycleLengths, statistics.mean, statistics.stdDev)

        return Result.success(
            CycleRegularityScore(
                score = score,
                level = regularityLevel,
                avgCycleLength = statistics.mean.toFloat(),
                stdDeviation = statistics.stdDev.toFloat(),
                cv = statistics.cv,
                range = cycleLengths.min()..cycleLengths.max(),
                outliers = outliers
            )
        )
    }

    private fun calculateStatistics(cycleLengths: List<Int>): Statistics {
        val n = cycleLengths.size
        val mean = cycleLengths.sum().toDouble() / n
        val variance = cycleLengths.map { (it - mean) * (it - mean) }.sum() / (n - 1)
        val stdDev = sqrt(variance)
        val cv = stdDev / mean

        return Statistics(mean, stdDev, cv)
    }

    // ... 其他辅助方法
}
```

---

#### 2. 周期趋势分析器

```kotlin
class CycleTrendAnalyzer {
    suspend fun analyze(
        cycles: List<MenstrualCycle>,
        minCycles: Int = 4
    ): Result<CycleTrendMetrics> {
        if (cycles.size < minCycles) {
            return Result.failure(InsufficientDataException("需要至少$minCycles 个周期数据"))
        }

        val sortedCycles = cycles.sortedBy { it.startDate }
        val trendData = prepareTrendData(sortedCycles)
        val regression = linearRegression(trendData)

        val trendDirection = determineTrendDirection(regression.slope, regression.rSquared)
        val changeRate = calculateChangeRate(regression.slope)
        val significance = assessSignificance(regression.rSquared, cycles.size)

        return Result.success(
            CycleTrendMetrics(
                slope = regression.slope.toFloat(),
                rSquared = regression.rSquared.toFloat(),
                trendDirection = trendDirection,
                changeRate = changeRate,
                significance = significance
            )
        )
    }

    private fun linearRegression(data: List<Pair<Int, Int>>): RegressionResult {
        val n = data.size
        val sumX = data.sumOf { it.first.toDouble() }
        val sumY = data.sumOf { it.second.toDouble() }
        val sumXY = data.sumOf { it.first.toDouble() * it.second.toDouble() }
        val sumX2 = data.sumOf { it.first.toDouble() * it.first.toDouble() }

        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        // 计算R²
        val meanY = sumY / n
        val ssTotal = data.sumOf { (it.second - meanY) * (it.second - meanY) }
        val ssResidual = data.sumOf {
            val predicted = slope * it.first + intercept
            (it.second - predicted) * (it.second - predicted)
        }
        val rSquared = 1 - ssResidual / ssTotal

        return RegressionResult(slope, intercept, rSquared)
    }

    // ... 其他辅助方法
}
```

---

#### 3. 趋势分析ViewModel

```kotlin
@HiltViewModel
class TrendAnalysisViewModel @Inject constructor(
    private val repository: WomenHealthRepository,
    private val regularityAnalyzer: CycleRegularityAnalyzer,
    private val trendAnalyzer: CycleTrendAnalyzer,
    private val symptomAnalyzer: SymptomTrendAnalyzer,
    private val anomalyDetector: AnomalyDetector,
    private val riskAssessor: HealthRiskAssessor
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendAnalysisUiState>(TrendAnalysisUiState.Loading)
    val uiState: StateFlow<TrendAnalysisUiState> = _uiState.asStateFlow()

    init {
        loadTrendAnalysis()
    }

    fun loadTrendAnalysis() {
        viewModelScope.launch {
            _uiState.value = TrendAnalysisUiState.Loading

            try {
                val cycles = repository.getRecentCycles(6)
                val symptoms = repository.getRecentSymptoms(180) // 6个月

                if (cycles.size < 3) {
                    _uiState.value = TrendAnalysisUiState.InsufficientData
                    return@launch
                }

                // 并行执行分析
                val regularityResult = async { regularityAnalyzer.analyze(cycles) }
                val trendResult = async { trendAnalyzer.analyze(cycles) }
                val symptomResult = async { symptomAnalyzer.analyze(symptoms) }
                val anomalyResult = async { anomalyDetector.detect(cycles) }
                val riskResult = async { riskAssessor.assess(cycles, symptoms, 25) }

                val regularity = regularityResult.await().getOrThrow()
                val trend = trendResult.await().getOrThrow()
                val symptomTrends = symptomResult.await().getOrThrow()
                val anomalies = anomalyResult.await().getOrThrow()
                val risks = riskResult.await().getOrThrow()

                _uiState.value = TrendAnalysisUiState.Success(
                    regularityScore = regularity,
                    trendMetrics = trend,
                    symptomTrends = symptomTrends,
                    anomalies = anomalies,
                    healthRisks = risks
                )
            } catch (e: Exception) {
                _uiState.value = TrendAnalysisUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun refreshAnalysis() {
        loadTrendAnalysis()
    }
}

sealed class TrendAnalysisUiState {
    object Loading : TrendAnalysisUiState()
    object InsufficientData : TrendAnalysisUiState()
    data class Success(
        val regularityScore: CycleRegularityScore,
        val trendMetrics: CycleTrendMetrics,
        val symptomTrends: List<SymptomTrendData>,
        val anomalies: List<CycleAnomaly>,
        val healthRisks: List<HealthRiskAssessment>
    ) : TrendAnalysisUiState()
    data class Error(val message: String) : TrendAnalysisUiState()
}
```

---

#### 4. 趋势分析Fragment

```kotlin
class TrendAnalysisFragment : Fragment() {
    private val viewModel: TrendAnalysisViewModel by viewModels()
    private lateinit var binding: FragmentTrendAnalysisBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrendAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()
        setupClickListeners()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is TrendAnalysisUiState.Loading -> showLoading()
                    is TrendAnalysisUiState.InsufficientData -> showInsufficientData()
                    is TrendAnalysisUiState.Success -> showAnalysisResult(state)
                    is TrendAnalysisUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showAnalysisResult(state: TrendAnalysisUiState.Success) {
        // 显示规律性评分
        binding.regularityScoreView.setScore(state.regularityScore.score)
        binding.regularityLevelText.text = getRegularityLevelText(state.regularityScore.level)

        // 显示趋势图
        binding.trendChartView.setData(state.trendMetrics)

        // 显示症状趋势
        binding.symptomTrendView.setSymptoms(state.symptomTrends)

        // 显示异常预警
        if (state.anomalies.isNotEmpty()) {
            binding.anomalyAlertView.setAnomalies(state.anomalies)
            binding.anomalyAlertView.visibility = View.VISIBLE
        } else {
            binding.anomalyAlertView.visibility = View.GONE
        }

        // 显示健康风险
        if (state.healthRisks.isNotEmpty()) {
            binding.healthRiskView.setRisks(state.healthRisks)
            binding.healthRiskView.visibility = View.VISIBLE
        } else {
            binding.healthRiskView.visibility = View.GONE
        }

        // 显示洞察
        binding.insightsView.setInsights(generateInsights(state))
    }

    private fun setupClickListeners() {
        binding.regularityCard.setOnClickListener {
            navigateToRegularityDetail()
        }

        binding.trendChartView.setOnClickListener {
            navigateToTrendDetail()
        }

        binding.symptomTrendView.setOnClickListener {
            navigateToSymptomTrend()
        }

        binding.refreshButton.setOnClickListener {
            viewModel.refreshAnalysis()
        }
    }

    private fun generateInsights(state: TrendAnalysisUiState.Success): List<String> {
        val insights = mutableListOf<String>()

        // 规律性洞察
        when (state.regularityScore.level) {
            RegularityLevel.VERY_REGULAR -> insights.add("您的月经周期非常规律")
            RegularityLevel.REGULAR -> insights.add("您的月经周期比较规律")
            RegularityLevel.SOMEWHAT_IRREGULAR -> insights.add("您的月经周期有些不规律")
            RegularityLevel.IRREGULAR -> insights.add("您的月经周期明显不规律")
        }

        // 趋势洞察
        when (state.trendMetrics.trendDirection) {
            TrendDirection.STABLE -> insights.add("周期长度保持稳定")
            TrendDirection.INCREASING -> insights.add("周期长度有增加趋势")
            TrendDirection.DECREASING -> insights.add("周期长度有减少趋势")
            TrendDirection.FLUCTUATING -> insights.add("周期长度波动较大")
        }

        // 异常洞察
        if (state.anomalies.isNotEmpty()) {
            insights.add("检测到${state.anomalies.size}个异常周期")
        }

        return insights
    }

    // ... 导航方法
}
```

---

#### 5. 图表工具类

```kotlin
object ChartUtils {
    fun setupCycleTrendChart(
        chart: LineChart,
        cycles: List<MenstrualCycle>,
        trendMetrics: CycleTrendMetrics
    ) {
        // 准备数据
        val entries = cycles.mapIndexed { index, cycle ->
            Entry(index.toFloat(), cycle.cycleLength.toFloat())
        }

        val dataSet = LineDataSet(entries, "周期长度").apply {
            color = Color.parseColor("#FF69B4")
            setCircleColor(Color.parseColor("#FF69B4"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = Color.parseColor("#33FF69B4")
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // 添加平均线
        val avgLine = cycles.map {
            Entry(it.startDate.toFloat(), trendMetrics.slope)
        }
        val avgDataSet = LineDataSet(avgLine, "趋势线").apply {
            color = Color.parseColor("#FF9800")
            setCircleColor(Color.parseColor("#FF9800"))
            lineWidth = 2f
            circleRadius = 3f
            enableDashedLine(10f, 5f, 0f)
        }

        val lineData = LineData(dataSet, avgDataSet)
        chart.data = lineData

        // 配置图表
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 20f
                axisMaximum = 45f
            }

            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.CIRCLE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
            }

            animateX(1000)
        }
    }

    fun setupSymptomIntensityChart(
        chart: BarChart,
        monthlyData: List<SymptomMonthData>
    ) {
        val entries = monthlyData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.avgIntensity)
        }

        val dataSet = BarDataSet(entries, "症状强度").apply {
            color = Color.parseColor("#FF69B4")
            setDrawValues(true)
        }

        val barData = BarData(dataSet)
        chart.data = barData

        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 10f
            }

            axisRight.isEnabled = false

            legend.isEnabled = false

            animateY(1000)
        }
    }

    fun setupSymptomFrequencyPieChart(
        chart: PieChart,
        symptomFrequencies: Map<SymptomType, Float>
    ) {
        val entries = symptomFrequencies.map { (type, frequency) ->
            PieEntry(frequency * 100, getSymptomTypeName(type))
        }

        val dataSet = PieDataSet(entries, "症状频率").apply {
            colors = listOf(
                Color.parseColor("#FF69B4"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#F44336"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#FFC107")
            )
            sliceSpace = 3f
            selectionShift = 5f
        }

        val pieData = PieData(dataSet).apply {
            setDrawValues(true)
            setValueFormatter(PercentFormatter(chart))
            setValueTextSize(12f)
        }

        chart.data = pieData

        chart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawHoleEnabled(true)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
            }
            animateY(1000)
        }
    }

    private fun getSymptomTypeName(type: SymptomType): String {
        return when (type) {
            SymptomType.DYSMENORRHEA -> "痛经"
            SymptomType.HEADACHE -> "头痛"
            SymptomType.BACK_PAIN -> "腰痛"
            SymptomType.BREAST_PAIN -> "乳房胀痛"
            SymptomType.BLOATING -> "腹胀"
            SymptomType.ACNE -> "痤疮"
            SymptomType.INSOMNIA -> "失眠"
            SymptomType.FATIGUE -> "疲劳"
        }
    }
}
```

---

## API设计

### Repository扩展接口

```kotlin
interface WomenHealthRepository {
    // 现有方法...

    // 新增趋势分析相关方法
    suspend fun getRecentCycles(count: Int): List<MenstrualCycle>
    suspend fun getRecentSymptoms(days: Int): List<SymptomRecord>
    suspend fun saveTrendAnalysis(analysis: CycleTrendAnalysis): Long
    suspend fun getLatestTrendAnalysis(type: String): CycleTrendAnalysis?
}

class WomenHealthRepositoryImpl(
    private val menstrualCycleDao: MenstrualCycleDao,
    private val symptomRecordDao: SymptomRecordDao,
    private val cycleTrendAnalysisDao: CycleTrendAnalysisDao
) : WomenHealthRepository {

    override suspend fun getRecentCycles(count: Int): List<MenstrualCycle> {
        return menstrualCycleDao.getAllCycles().first().take(count)
    }

    override suspend fun getRecentSymptoms(days: Int): List<SymptomRecord> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - days * 24 * 60 * 60 * 1000L
        return symptomRecordDao.getRecordsInRange(startTime, endTime)
    }

    override suspend fun saveTrendAnalysis(analysis: CycleTrendAnalysis): Long {
        return cycleTrendAnalysisDao.insert(analysis)
    }

    override suspend fun getLatestTrendAnalysis(type: String): CycleTrendAnalysis? {
        return cycleTrendAnalysisDao.getLatestByType(type)
    }
}
```

---

### DAO接口

```kotlin
@Dao
interface CycleTrendAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysis: CycleTrendAnalysis): Long

    @Query("SELECT * FROM cycle_trend_analysis WHERE analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByType(type: String): CycleTrendAnalysis?

    @Query("SELECT * FROM cycle_trend_analysis WHERE analysisDate BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    suspend fun getAnalysesInRange(startTime: Long, endTime: Long): List<CycleTrendAnalysis>

    @Query("DELETE FROM cycle_trend_analysis WHERE createdAt < :threshold")
    suspend fun deleteOldAnalyses(threshold: Long)
}
```

---

## 测试方案

### 单元测试

#### 1. 规律性分析器测试

```kotlin
class CycleRegularityAnalyzerTest {
    private val analyzer = CycleRegularityAnalyzer()

    @Test
    fun `analyze should return very regular when CV is less than 5%`() = runBlocking {
        val cycles = listOf(
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 29),
            createCycle(cycleLength = 27),
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 29)
        )

        val result = analyzer.analyze(cycles)

        assertTrue(result.isSuccess)
        val score = result.getOrThrow()
        assertEquals(RegularityLevel.VERY_REGULAR, score.level)
        assertTrue(score.score > 90)
    }

    @Test
    fun `analyze should return irregular when CV is greater than 15%`() = runBlocking {
        val cycles = listOf(
            createCycle(cycleLength = 21),
            createCycle(cycleLength = 35),
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 22),
            createCycle(cycleLength = 40),
            createCycle(cycleLength = 25)
        )

        val result = analyzer.analyze(cycles)

        assertTrue(result.isSuccess)
        val score = result.getOrThrow()
        assertEquals(RegularityLevel.IRREGULAR, score.level)
        assertTrue(score.score < 50)
    }

    @Test
    fun `analyze should fail when less than 3 cycles provided`() = runBlocking {
        val cycles = listOf(
            createCycle(cycleLength = 28),
            createCycle(cycleLength = 29)
        )

        val result = analyzer.analyze(cycles)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InsufficientDataException)
    }

    private fun createCycle(cycleLength: Int): MenstrualCycle {
        return MenstrualCycle(
            id = 0,
            startDate = System.currentTimeMillis(),
            endDate = null,
            cycleLength = cycleLength,
            flowIntensity = "medium",
            symptoms = null,
            notes = null
        )
    }
}
```

---

#### 2. 趋势分析器测试

```kotlin
class CycleTrendAnalyzerTest {
    private val analyzer = CycleTrendAnalyzer()

    @Test
    fun `analyze should detect stable trend when slope is near zero`() = runBlocking {
        val cycles = (0..5).map { index ->
            createCycle(cycleLength = 28, monthOffset = index)
        }

        val result = analyzer.analyze(cycles)

        assertTrue(result.isSuccess)
        val metrics = result.getOrThrow()
        assertEquals(TrendDirection.STABLE, metrics.trendDirection)
    }

    @Test
    fun `analyze should detect increasing trend when slope is positive`() = runBlocking {
        val cycles = (0..5).map { index ->
            createCycle(cycleLength = 26 + index, monthOffset = index)
        }

        val result = analyzer.analyze(cycles)

        assertTrue(result.isSuccess)
        val metrics = result.getOrThrow()
        assertEquals(TrendDirection.INCREASING, metrics.trendDirection)
        assertTrue(metrics.changeRate > 0)
    }

    @Test
    fun `analyze should calculate correct R-squared`() = runBlocking {
        val cycles = (0..5).map { index ->
            createCycle(cycleLength = 26 + index, monthOffset = index)
        }

        val result = analyzer.analyze(cycles)

        assertTrue(result.isSuccess)
        val metrics = result.getOrThrow()
        assertTrue(metrics.rSquared > 0.9) // 完美线性关系
    }

    private fun createCycle(cycleLength: Int, monthOffset: Int): MenstrualCycle {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthOffset)
        val startTime = calendar.timeInMillis

        return MenstrualCycle(
            id = 0,
            startDate = startTime,
            endDate = null,
            cycleLength = cycleLength,
            flowIntensity = "medium",
            symptoms = null,
            notes = null
        )
    }
}
```

---

### 集成测试

```kotlin
@RunWith(AndroidJUnit4::class)
class TrendAnalysisIntegrationTest {
    private lateinit var database: WomenHealthDatabase
    private lateinit var repository: WomenHealthRepository
    private lateinit var viewModel: TrendAnalysisViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WomenHealthDatabase::class.java).build()
        repository = WomenHealthRepositoryImpl(
            database.menstrualCycleDao(),
            database.symptomRecordDao(),
            database.cycleTrendAnalysisDao()
        )

        // 使用测试 ViewModelFactory
        viewModel = TrendAnalysisViewModel(
            repository,
            CycleRegularityAnalyzer(),
            CycleTrendAnalyzer(),
            SymptomTrendAnalyzer(),
            AnomalyDetector(),
            HealthRiskAssessor()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `trend analysis should work end-to-end`() = runTest {
        // 准备测试数据
        val cycles = (0..5).map { index ->
            MenstrualCycle(
                id = 0,
                startDate = System.currentTimeMillis() - (6 - index) * 30L * 24 * 60 * 60 * 1000,
                endDate = null,
                cycleLength = 28,
                flowIntensity = "medium",
                symptoms = null,
                notes = null
            )
        }

        cycles.forEach { cycle ->
            database.menstrualCycleDao().insertCycle(cycle)
        }

        // 触发分析
        viewModel.loadTrendAnalysis()

        // 等待结果
        val state = viewModel.uiState.first { it !is TrendAnalysisUiState.Loading }

        // 验证结果
        assertTrue(state is TrendAnalysisUiState.Success)
        val successState = state as TrendAnalysisUiState.Success
        assertNotNull(successState.regularityScore)
        assertNotNull(successState.trendMetrics)
    }
}
```

---

### UI测试

```kotlin
@RunWith(AndroidJUnit4::class)
class TrendAnalysisFragmentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `trend analysis screen should show regularity score`() {
        // 设置测试状态
        val testState = TrendAnalysisUiState.Success(
            regularityScore = CycleRegularityScore(
                score = 85f,
                level = RegularityLevel.REGULAR,
                avgCycleLength = 28f,
                stdDeviation = 1.5f,
                cv = 0.054f,
                range = 26..30,
                outliers = emptyList()
            ),
            trendMetrics = CycleTrendMetrics(
                slope = 0.1f,
                rSquared = 0.72f,
                trendDirection = TrendDirection.STABLE,
                changeRate = 0.1f,
                significance = TrendSignificance.MODERATE
            ),
            symptomTrends = emptyList(),
            anomalies = emptyList(),
            healthRisks = emptyList()
        )

        // 渲染界面
        composeTestRule.setContent {
            TrendAnalysisScreen(
                uiState = testState,
                onRefresh = {},
                onNavigateToRegularityDetail = {}
            )
        }

        // 验证UI元素
        composeTestRule.onNodeWithText("85").assertIsDisplayed()
        composeTestRule.onNodeWithText("比较规律").assertIsDisplayed()
    }

    @Test
    fun `trend analysis screen should show loading state`() {
        composeTestRule.setContent {
            TrendAnalysisScreen(
                uiState = TrendAnalysisUiState.Loading,
                onRefresh = {},
                onNavigateToRegularityDetail = {}
            )
        }

        composeTestRule.onNodeWithText("加载中...").assertIsDisplayed()
    }

    @Test
    fun `trend analysis screen should show insufficient data state`() {
        composeTestRule.setContent {
            TrendAnalysisScreen(
                uiState = TrendAnalysisUiState.InsufficientData,
                onRefresh = {},
                onNavigateToRegularityDetail = {}
            )
        }

        composeTestRule.onNodeWithText("数据不足").assertIsDisplayed()
    }
}
```

---

## 性能优化

### 1. 数据查询优化

#### 使用索引
```kotlin
@Entity(
    tableName = "menstrual_cycles",
    indices = [
        Index(value = ["startDate"]),
        Index(value = ["cycleLength"])
    ]
)
data class MenstrualCycle(...)
```

#### 使用Flow避免重复查询
```kotlin
// 在ViewModel中使用Flow
private val _recentCycles = MutableStateFlow<List<MenstrualCycle>>(emptyList())

init {
    viewModelScope.launch {
        repository.getRecentCyclesFlow(6).collect { cycles ->
            _recentCycles.value = cycles
        }
    }
}
```

---

### 2. 计算优化

#### 缓存分析结果
```kotlin
class CachedTrendAnalyzer(
    private val analyzer: CycleRegularityAnalyzer,
    private val cache: TrendAnalysisCache
) {
    suspend fun analyze(cycles: List<MenstrualCycle>): CycleRegularityScore {
        val cacheKey = generateCacheKey(cycles)

        // 检查缓存
        cache.get(cacheKey)?.let { return it }

        // 执行分析
        val result = analyzer.analyze(cycles).getOrThrow()

        // 缓存结果
        cache.put(cacheKey, result)

        return result
    }

    private fun generateCacheKey(cycles: List<MenstrualCycle>): String {
        return cycles.joinToString("|") { "${it.id}:${it.cycleLength}" }
    }
}
```

#### 并行执行分析
```kotlin
suspend fun performAllAnalyses(cycles: List<MenstrualCycle>): AnalysisResults = coroutineScope {
    val regularity = async { regularityAnalyzer.analyze(cycles) }
    val trend = async { trendAnalyzer.analyze(cycles) }
    val anomalies = async { anomalyDetector.detect(cycles) }

    AnalysisResults(
        regularity = regularity.await().getOrThrow(),
        trend = trend.await().getOrThrow(),
        anomalies = anomalies.await().getOrThrow()
    )
}
```

---

### 3. UI渲染优化

#### 使用LazyColumn列表
```kotlin
@Composable
fun AnomalyList(
    anomalies: List<CycleAnomaly>,
    onItemClick: (CycleAnomaly) -> Unit
) {
    LazyColumn {
        items(anomalies) { anomaly ->
            AnomalyItem(
                anomaly = anomaly,
                onClick = { onItemClick(anomaly) }
            )
        }
    }
}
```

#### 图表异步渲染
```kotlin
@Composable
fun TrendChart(
    cycles: List<MenstrualCycle>,
    metrics: CycleTrendMetrics
) {
    val context = LocalContext.current
    val chartView = remember { LineChart(context) }

    LaunchedEffect(cycles, metrics) {
        withContext(Dispatchers.Default) {
            ChartUtils.setupCycleTrendChart(chartView, cycles, metrics)
        }
    }

    AndroidView(
        factory = { chartView },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
```

---

### 4. 内存优化

#### 使用数据类减少对象创建
```kotlin
// 避免在循环中创建大量临时对象
fun processData(cycles: List<MenstrualCycle>): List<ProcessedData> {
    return cycles.map { cycle ->
        // 重用对象
        ProcessedData(
            id = cycle.id,
            length = cycle.cycleLength,
            intensity = cycle.flowIntensity
        )
    }
}
```

#### 使用对象池
```kotlin
class EntryPool {
    private val pool = ArrayDeque<Entry>()

    fun obtain(x: Float, y: Float): Entry {
        return pool.removeFirstOrNull()?.apply {
            this.x = x
            this.y = y
        } ?: Entry(x, y)
    }

    fun recycle(entry: Entry) {
        pool.addLast(entry)
    }
}
```

---

## 未来扩展

### 1. AI驱动的预测优化
- 使用机器学习模型优化周期预测
- 基于用户行为和症状数据个性化预测
- 集成外部数据源（天气、活动等）

### 2. 社交功能
- 匿名分享趋势数据
- 与同龄人对比（匿名）
- 社区健康报告

### 3. 医生集成
- 生成医疗报告
- 直接分享给医生
- 集成电子健康记录（EHR）

### 4. 多设备同步
- 跨设备数据同步
- 手表端趋势分析
- 桌面端高级分析

### 5. 国际化
- 多语言支持
- 地区化健康指南
- 本地化医疗资源

---

## 总结

本文档详细描述了周期变化趋势功能的设计和实现方案，包括：

1. **功能概述**：明确了功能定位和核心价值
2. **需求分析**：详细列出了用户故事和功能优先级
3. **数据模型**：设计了完整的数据模型和实体类
4. **算法设计**：提供了5个核心算法的详细实现
5. **UI/UX设计**：设计了6个主要界面和交互方案
6. **技术实现**：提供了完整的技术栈和代码示例
7. **API设计**：定义了Repository和DAO接口
8. **测试方案**：提供了单元测试、集成测试和UI测试示例
9. **性能优化**：提供了4个方面的优化建议
10. **未来扩展**：列出了5个可能的扩展方向

本文档为开发团队提供了完整的开发指南，可以按照文档逐步实现周期变化趋势功能。

---

## 附录

### A. 参考文献和资源
- [Mayo Clinic - Menstrual Cycle](https://www.mayoclinic.org/healthy-lifestyle/womens-health/in-depth/menstrual-cycle/art-20047186)
- [ACOG Guidelines](https://www.acog.org/)
- [MPAndroidChart Documentation](https://github.com/PhilJay/MPAndroidChart)
- [Material Design 3](https://m3.material.io/)

### B. 术语表
- **CV (Coefficient of Variation)**：变异系数，用于评估数据离散程度
- **R² (R-squared)**：决定系数，用于评估回归模型的拟合度
- **PCOS (Polycystic Ovary Syndrome)**：多囊卵巢综合征
- **Flow**：经血流量
- **Dysmenorrhea**：痛经

### C. 版本历史
- v1.0 (2026-02-15)：初始版本