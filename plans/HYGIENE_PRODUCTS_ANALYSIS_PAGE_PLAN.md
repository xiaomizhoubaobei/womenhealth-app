# 卫生用品分析页面需求文档

> **关联开发计划**: [女性健康管理系统APP开发计划](./WOMEN_HEALTH_APP_DEVELOPMENT_PLAN.md) - 卫生巾分析模块 + 卫生棉条分析模块
> **开发周期**: Week 10-11 (专项功能开发阶段)
> **关联Issue**: #128

## 1. 页面概述

### 1.1 功能目标
卫生用品分析页面专注于女性卫生用品（卫生巾和卫生棉条）的使用情况追踪与分析，通过记录使用量、更换频率、舒适度等关键指标，结合月经流量数据，帮助用户选择最适合自己的卫生用品，优化使用体验，并监测可能的健康问题。

### 1.2 核心价值
- **品牌推荐**：基于肤质和使用体验推荐合适品牌
- **舒适度评估**：记录透气性、吸收性、过敏情况
- **过敏反应记录**：追踪过敏症状和品牌关联
- **成本效益分析**：不同品牌的性价比分析
- **材质选择**：棉质、人造纤维等不同材质对比
- **安全性评估**：TSS风险评估和安全使用指导

### 1.3 开发计划关联
本页面对应开发计划第三阶段（专项功能开发 Week 10-11）中的卫生用品分析模块。

## 2. 功能需求

### 2.1 卫生用品记录功能
- **用品类型记录**：记录使用的卫生用品类型（卫生巾/卫生棉条）
- **品牌型号记录**：记录用品的品牌和具体型号
- **使用量追踪**：记录每日使用数量和更换频率
- **舒适度评价**：评价用品的舒适度和使用体验
- **漏液情况记录**：记录是否有漏液情况发生

### 2.2 品牌推荐功能（核心功能）
- **肤质匹配**：根据用户肤质推荐合适的品牌
- **使用体验推荐**：基于历史使用体验推荐品牌
- **相似用户推荐**：基于相似用户的选择推荐品牌
- **新品推荐**：推荐适合用户的新品牌或新产品

### 2.3 舒适度评估功能（核心功能）
- **透气性评估**：评估用品的透气性能
- **吸收性评估**：评估用品的吸收能力
- **过敏情况记录**：记录使用过程中的过敏反应
- **整体舒适度评分**：综合评估用品舒适度

### 2.4 过敏反应记录功能（核心功能）
- **过敏症状记录**：记录皮肤过敏、瘙痒等症状
- **品牌关联分析**：分析过敏症状与品牌的关联性
- **过敏预警**：预警可能引起过敏的产品
- **替代产品推荐**：推荐不易引起过敏的替代产品

### 2.5 成本效益分析功能（核心功能）
- **使用成本计算**：计算不同用品的月度和年度使用成本
- **性价比评估**：评估不同用品的性价比
- **环保影响分析**：分析不同用品对环境的影响
- **购买建议**：根据分析结果提供购买建议

### 2.6 材质选择功能（卫生棉条核心功能）
- **材质对比**：棉质、人造纤维等不同材质对比
- **材质推荐**：根据用户需求推荐合适材质
- **材质敏感性分析**：分析用户对不同材质的敏感性

### 2.7 安全性评估功能（卫生棉条核心功能）
- **TSS风险评估**：评估中毒性休克综合征(TSS)风险
- **安全使用指导**：提供安全使用棉条的指导
- **使用时长提醒**：提醒用户及时更换棉条
- **风险预警**：预警潜在的安全风险

### 2.8 使用习惯分析
- **更换频率分析**：分析不同流量下的更换频率是否合理
- **用量统计**：统计各类型用品的使用量和消耗速度
- **时间段分布**：分析用品使用在一天中的分布情况
- **个体偏好分析**：分析用户对不同用品的偏好

### 2.9 AI智能分析
- **用品推荐系统**：基于AI算法为用户推荐最适合的卫生用品
- **使用习惯优化**：通过AI分析提供使用习惯优化建议
- **健康风险预测**：预测使用习惯可能带来的健康风险
- **个性化成本优化**：根据用户需求提供成本优化建议

### 2.10 健康关联分析
- **流量匹配度**：分析用品吸收能力与实际流量的匹配度
- **皮肤敏感性**：监测是否出现皮肤过敏或不适反应
- **感染风险评估**：评估使用习惯对感染风险的影响
- **异味情况追踪**：记录和分析异味相关情况

## 3. 页面设计

### 3.1 页面布局
```
+----------------------------------------------------+
| 顶部状态栏                                         |
| [用户头像]  [通知]  [设置]                         |
+----------------------------------------------------+
| 今日使用情况                                       |
| 卫生巾: 3片      更换频率: 4小时    舒适度: 良好   |
| [记录今日使用] [查看历史]                          |
+----------------------------------------------------+
| 用品类型分布                                       |
| [卫生巾使用统计]  [卫生棉条使用统计]               |
| 饼图显示各类型使用比例                             |
+----------------------------------------------------+
| 数据记录区域                                       |
| 日期      类型      品牌      数量    舒适度      |
| 5月1日    卫生巾    ABC       4片     良好        |
| 5月2日    卫生巾    Always    5片     一般        |
| 5月3日    卫生棉条  Tampax    6根     良好        |
+----------------------------------------------------+
| 使用习惯分析                                       |
| [更换频率分析]  [用量统计]  [时间段分布]           |
+----------------------------------------------------+
| 健康关联分析                                       |
| [流量匹配度]  [皮肤敏感性]  [感染风险评估]         |
+----------------------------------------------------+
| 成本效益分析                                       |
| 月度成本: ¥45    性价比评分: 8.2分                |
| [成本详情]  [购买建议]  [环保影响]                 |
+----------------------------------------------------+
| 底部导航栏                                         |
| [生理期] [孕期] [宫颈] [生理用品] [我的]           |
+----------------------------------------------------+

导航项说明:
- **生理期**：跳转至生理期管理页面，进行月经周期记录和症状追踪
- **孕期**：跳转至孕期管理页面，进行备孕和孕期健康管理
- **宫颈**：跳转至宫颈分析页面，查看宫颈健康分析
- **生理用品**：当前页面，分析卫生用品使用情况
- **我的**：跳转至个人中心页面，查看个人设置和数据管理

```

### 3.2 交互设计
- **快速记录**：提供便捷的用品使用记录界面
- **详细记录**：支持详细记录各项使用信息
- **图表交互**：支持点击查看图表详细信息
- **数据筛选**：支持按时间范围和用品类型筛选数据

## 4. 技术实现

### 4.1 技术架构（统一技术栈）
- **开发语言**：Kotlin (主语言) + Java (兼容支持)
- **UI框架**：Jetpack Compose + Material Design 3
- **架构模式**：MVVM + Repository + Clean Architecture
- **依赖注入**：Hilt
- **异步处理**：Kotlin Coroutines + Flow
- **数据存储**：Room Database + SQLCipher 加密
- **数据可视化**：MPAndroidChart + Jetpack Compose Charts

### 4.2 数据模型
```kotlin
data class HygieneProductRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Date, // 记录日期
    val productType: HygieneProductType, // 用品类型
    val brand: String, // 品牌
    val model: String?, // 型号/系列
    val quantity: Int, // 使用数量
    val changeFrequency: Int, // 更换频率(小时)
    val comfortLevel: ComfortLevel, // 舒适度
    val leakage: LeakageLevel, // 漏液情况
    val skinReaction: SkinReaction?, // 皮肤反应
    val odor: OdorLevel?, // 异味情况
    val notes: String?, // 备注
    val cost: Double?, // 单次使用成本
    val flowLevel: FlowLevel, // 对应流量等级
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class HygieneProductType(val displayName: String) {
    SANITARY_PAD("卫生巾"),
    TAMPON("卫生棉条"),
    MENSTRUAL_CUP("月经杯"),
    PANTY_LINER("护垫")
}

data class SanitaryPad(
    val absorptionLevel: AbsorptionLevel, // 吸收能力等级
    val size: PadSize, // 尺寸
    val wings: Boolean, // 是否有护翼
    val material: PadMaterial // 材质
) : HygieneProduct()

data class Tampon(
    val absorbency: TamponAbsorbency, // 吸收能力
    val applicatorType: ApplicatorType, // 导入器类型
    val material: TamponMaterial // 材质
) : HygieneProduct()

enum class ComfortLevel(val displayName: String, val score: Int) {
    VERY_UNCOMFORTABLE("很不舒适", 1),
    UNCOMFORTABLE("不舒适", 2),
    FAIR("一般", 3),
    COMFORTABLE("舒适", 4),
    VERY_COMFORTABLE("很舒适", 5)
}

enum class LeakageLevel(val displayName: String) {
    NONE("无漏液"),
    MINIMAL("轻微漏液"),
    MODERATE("中等漏液"),
    SEVERE("严重漏液")
}

data class HygieneProductAnalysisResult(
    val usagePattern: UsagePattern, // 使用模式
    val comfortAnalysis: ComfortAnalysis, // 舒适度分析
    val healthAssessment: HealthAssessment, // 健康评估
    val costAnalysis: CostAnalysis, // 成本分析
    val recommendations: List<ProductRecommendation>, // 用品推荐
    val healthInsights: List<HealthInsight>, // 健康洞察
    val optimizationSuggestions: List<OptimizationSuggestion> // 优化建议
)

data class UsagePattern(
    val productDistribution: Map<HygieneProductType, Int>, // 用品分布
    val dailyUsage: Map<Date, Int>, // 日使用量
    val frequencyPattern: Map<FlowLevel, Int>, // 频率模式
    val preferenceTrend: PreferenceTrend // 偏好趋势
)

data class CostAnalysis(
    val monthlyCost: Double, // 月度成本
    val annualCost: Double, // 年度成本
    val costPerDay: Double, // 日均成本
    val性价比Ranking: List<ProductCostEffectiveness>, // 性价比排名
    val environmentalImpact: EnvironmentalImpact // 环保影响
)
```

### 4.3 核心算法
- **使用模式识别算法**：识别用户的用品使用模式
- **舒适度分析算法**：分析不同用品的舒适度差异
- **健康风险评估算法**：评估使用习惯对健康的影响
- **成本效益计算模型**：计算不同用品的成本效益比
- **TSS风险评估算法**：评估中毒性休克综合征风险
- **品牌推荐算法**：基于多维度数据推荐合适品牌

### 4.5 TSS风险评估引擎
```kotlin
/**
 * 卫生棉条TSS风险评估引擎
 */
class TSSRiskAssessmentEngine {
    /**
     * 评估TSS风险等级
     * 风险因素：使用时长、吸收能力、材质等
     */
    fun assessRisk(record: TamponUsageRecord): TSSRiskLevel

    /**
     * 生成安全使用建议
     */
    fun generateSafetyAdvice(riskLevel: TSSRiskLevel): SafetyAdvice

    /**
     * 计算建议更换时间
     */
    fun calculateRecommendedChangeTime(
        insertionTime: Date,
        absorbency: TamponAbsorbency
    ): Date
}
```

## 5. 性能要求

### 5.1 响应时间
- 页面加载时间：≤ 1.5秒
- 数据保存时间：≤ 0.5秒
- 分析计算时间：≤ 2秒

### 5.2 内存使用
- 页面内存占用：≤ 30MB
- 图表渲染内存：≤ 15MB

## 6. 安全与隐私

### 6.1 数据保护
- 所有数据本地存储，采用AES-256加密
- 敏感数据（健康相关信息）加强保护
- 数据导出时进行加密处理

### 6.2 权限要求
- 存储权限：用于数据备份和导出
- 相机权限（可选）：用于扫描用品包装条码

## 7. 测试计划

### 7.1 功能测试
- 验证数据记录功能的完整性和准确性
- 验证图表显示正确性
- 验证分析算法准确性
- 验证推荐功能合理性

### 7.2 性能测试
- 大量数据记录性能测试
- 分析算法性能测试
- 页面响应速度测试

### 7.3 兼容性测试
- 不同Android版本兼容性测试
- 不同屏幕尺寸适配测试

## 8. 项目计划

### 8.1 开发周期（与开发计划对齐）
| 阶段 | 周期 | 内容 | 关联开发计划 |
|------|------|------|--------------|
| 需求分析 | Week 10 第1周 | 功能细化、UI原型设计 | 第三阶段 Week 10-11 |
| 核心开发 | Week 10-11 | 品牌推荐、舒适度评估、过敏记录、成本分析 | 第三阶段 Week 10-11 |
| 集成测试 | Week 13-14 | 功能测试、性能优化 | 第四阶段 Week 13-14 |

### 8.2 里程碑
- [x] 完成需求文档更新
- [ ] 完成页面UI设计（Week 10）
- [ ] 实现卫生用品记录功能（Week 10）
- [ ] 完成品牌推荐系统（Week 10）
- [ ] 实现过敏反应追踪功能（Week 10）
- [ ] 完成卫生棉条安全性评估功能（Week 11）
- [ ] 完成测试与优化（Week 13-14）

### 8.3 验收标准
- 品牌推荐准确率 > 80%
- TSS风险预警及时率 100%
- 页面加载时间 < 1.5秒
- 数据保存时间 < 0.5秒

