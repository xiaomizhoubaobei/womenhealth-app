# AI模型集成技术规范文档

## 1. 文档概述

### 1.1 目标
本文档详细说明如何在LuminCore女性健康助手中集成Qwen模型、Intern模型和GPT模型，包括技术实现细节、API调用规范、数据处理流程和安全隐私保护措施。

### 1.2 背景
在现有的混合AI架构基础上，扩展支持更多的AI模型提供商，以提供更丰富的AI分析能力，满足不同用户的需求和偏好。

## 2. 技术架构

### 2.1 整体架构
```
应用层
├── UI层
│   ├── AI模型选择界面
│   └── AI分析结果展示
├── 业务逻辑层
│   ├── AI管理器
│   └── 云端API适配器
└── 数据层
    ├── 用户偏好设置
    └── AI分析结果缓存

云端服务层
├── Qwen API (阿里巴巴)
├── Intern API (上海AI实验室)
├── GPT API (OpenAI)
└── 现有AI模型API
```

### 2.2 核心组件

#### 2.2.1 AI管理器 (AIManager)
```kotlin
class AIManager @Inject constructor(
    private val localAIEngine: LocalAIEngine,
    private val cloudAPIProvider: CloudAPIProvider
) {
    suspend fun analyzeHealthData(
        data: HealthData, 
        useCloud: Boolean = false,
        preferredModel: AIModelType = AIModelType.DEFAULT
    ): AnalysisResult {
        return if (useCloud && isNetworkAvailable()) {
            // 使用指定的云端API进行增强分析
            when (preferredModel) {
                AIModelType.QWEN -> cloudAPIProvider.analyzeWithQwen(data.toAnonymizedFeatures())
                AIModelType.INTERN -> cloudAPIProvider.analyzeWithIntern(data.toAnonymizedFeatures())
                AIModelType.GPT -> cloudAPIProvider.analyzeWithGPT(data.toAnonymizedFeatures())
                else -> cloudAPIProvider.analyze(data.toAnonymizedFeatures())
            }
        } else {
            // 使用本地AI进行基础分析
            localAIEngine.analyze(data)
        }
    }
}
```

#### 2.2.2 云端API提供商 (CloudAPIProvider)
```kotlin
class CloudAPIProvider @Inject constructor(
    private val qwenAPI: QwenAPI,
    private val internAPI: InternAPI,
    private val gptAPI: GPTAPI,
    // 现有API...
) {
    suspend fun analyzeWithQwen(features: Map<String, Any>): AnalysisResult {
        try {
            val result = qwenAPI.healthAnalysis(formatFeaturesForQwen(features))
            return AnalysisResult(result)
        } catch (e: Exception) {
            // 错误处理和降级
            throw AIModelException("Qwen API调用失败", e)
        }
    }
    
    suspend fun analyzeWithIntern(features: Map<String, Any>): AnalysisResult {
        try {
            val result = internAPI.symptomAnalysis(formatFeaturesForIntern(features))
            return AnalysisResult(result)
        } catch (e: Exception) {
            // 错误处理和降级
            throw AIModelException("Intern API调用失败", e)
        }
    }
    
    suspend fun analyzeWithGPT(features: Map<String, Any>): AnalysisResult {
        try {
            val result = gptAPI.advancedHealthAnalysis(formatFeaturesForGPT(features))
            return AnalysisResult(result)
        } catch (e: Exception) {
            // 错误处理和降级
            throw AIModelException("GPT API调用失败", e)
        }
    }
}
```

## 3. 新增AI模型API实现

### 3.1 Qwen API适配器
```kotlin
class QwenAPI @Inject constructor(
    private val apiClient: SecureAPIClient,
    private val config: AIConfig
) {
    suspend fun healthAnalysis(prompt: String): String {
        val request = QwenRequest(
            model = config.qwenModel,
            prompt = prompt,
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )
        
        val response = apiClient.post<QwenRequest, QwenResponse>(
            url = config.qwenApiUrl,
            data = request,
            headers = mapOf(
                "Authorization" to "Bearer ${config.qwenApiKey}",
                "Content-Type" to "application/json"
            )
        )
        
        return response.choices.firstOrNull()?.text ?: ""
    }
    
    suspend fun personalizedRecommendation(userData: HealthDataSummary): String {
        val prompt = buildRecommendationPrompt(userData)
        return healthAnalysis(prompt)
    }
    
    private fun buildRecommendationPrompt(userData: HealthDataSummary): String {
        return """
            基于以下健康数据，请提供个性化的健康建议：
            - 周期规律性: ${userData.cycleRegularity}
            - 平均周期长度: ${userData.averageCycleLength}天
            - 症状频率: ${userData.symptomFrequency}
            
            请以关怀和专业的语调提供3条具体可行的健康建议。
        """.trimIndent()
    }
}

data class QwenRequest(
    val model: String,
    val prompt: String,
    val maxTokens: Int,
    val temperature: Float
)

data class QwenResponse(
    val choices: List<QwenChoice>
)

data class QwenChoice(
    val text: String
)
```

### 3.2 Intern API适配器
```kotlin
class InternAPI @Inject constructor(
    private val apiClient: SecureAPIClient,
    private val config: AIConfig
) {
    suspend fun symptomAnalysis(symptoms: List<String>): SymptomInsight {
        val request = InternRequest(
            model = config.internModel,
            symptoms = symptoms,
            task = "symptom_analysis"
        )
        
        val response = apiClient.post<InternRequest, InternResponse>(
            url = config.internApiUrl,
            data = request,
            headers = mapOf(
                "Authorization" to "Bearer ${config.internApiKey}",
                "Content-Type" to "application/json"
            )
        )
        
        return SymptomInsight(
            insights = response.results.map { it.insight },
            confidence = response.confidence
        )
    }
    
    suspend fun healthRiskAssessment(healthProfile: HealthProfile): RiskAssessment {
        val request = InternRiskRequest(
            model = config.internModel,
            profile = healthProfile,
            task = "risk_assessment"
        )
        
        val response = apiClient.post<InternRiskRequest, InternRiskResponse>(
            url = config.internApiUrl,
            data = request,
            headers = mapOf(
                "Authorization" to "Bearer ${config.internApiKey}",
                "Content-Type" to "application/json"
            )
        )
        
        return RiskAssessment(
            risks = response.risks,
            recommendations = response.recommendations,
            confidence = response.confidence
        )
    }
}

data class InternRequest(
    val model: String,
    val symptoms: List<String>,
    val task: String
)

data class InternResponse(
    val results: List<InternResult>,
    val confidence: Float
)

data class InternResult(
    val insight: String
)

data class InternRiskRequest(
    val model: String,
    val profile: HealthProfile,
    val task: String
)

data class InternRiskResponse(
    val risks: List<String>,
    val recommendations: List<String>,
    val confidence: Float
)
```

### 3.3 GPT API适配器
```kotlin
class GPTAPI @Inject constructor(
    private val apiClient: SecureAPIClient,
    private val config: AIConfig
) {
    suspend fun advancedHealthAnalysis(healthData: HealthDataSummary): String {
        val messages = listOf(
            ChatMessage(
                role = "system",
                content = "你是一个专业的女性健康顾问，基于提供的健康数据提供专业、准确的分析和建议。"
            ),
            ChatMessage(
                role = "user",
                content = buildHealthAnalysisPrompt(healthData)
            )
        )
        
        val request = GPTRequest(
            model = config.gptModel,
            messages = messages,
            maxTokens = config.maxTokens,
            temperature = config.temperature
        )
        
        val response = apiClient.post<GPTRequest, GPTResponse>(
            url = config.gptApiUrl,
            data = request,
            headers = mapOf(
                "Authorization" to "Bearer ${config.gptApiKey}",
                "Content-Type" to "application/json"
            )
        )
        
        return response.choices.firstOrNull()?.message?.content ?: ""
    }
    
    suspend fun multilingualSupport(query: String, language: String): String {
        val messages = listOf(
            ChatMessage(
                role = "system",
                content = "请将以下内容翻译成$language，并保持专业和关怀的语调。"
            ),
            ChatMessage(
                role = "user",
                content = query
            )
        )
        
        val request = GPTRequest(
            model = config.gptTranslationModel,
            messages = messages,
            maxTokens = config.maxTokens,
            temperature = 0.3f // 翻译时使用较低的温度以保持准确性
        )
        
        val response = apiClient.post<GPTRequest, GPTResponse>(
            url = config.gptApiUrl,
            data = request,
            headers = mapOf(
                "Authorization" to "Bearer ${config.gptApiKey}",
                "Content-Type" to "application/json"
            )
        )
        
        return response.choices.firstOrNull()?.message?.content ?: ""
    }
    
    private fun buildHealthAnalysisPrompt(healthData: HealthDataSummary): String {
        return """
            请基于以下健康数据分析用户的健康状况，并提供专业建议：
            
            用户健康数据摘要：
            - 周期规律性评分: ${healthData.cycleRegularity}/10
            - 平均周期长度: ${healthData.averageCycleLength}天
            - 主要症状及频率: ${healthData.symptomFrequency}
            
            请从以下几个方面进行分析：
            1. 周期规律性评估
            2. 症状模式分析
            3. 可能的健康风险提示
            4. 个性化健康建议（3-5条具体可行的建议）
            
            请以专业、关怀和易懂的语言进行回复。
        """.trimIndent()
    }
}

data class GPTRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val maxTokens: Int,
    val temperature: Float
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class GPTResponse(
    val choices: List<GPTChoice>
)

data class GPTChoice(
    val message: ChatMessage
)
```

## 4. 数据处理与安全

### 4.1 数据匿名化处理
```kotlin
data class HealthDataSummary(
    val cycleRegularity: Double,
    val symptomFrequency: Map<String, Int>,
    val averageCycleLength: Double,
    val userId: String = generateAnonymousId()
) {
    private fun generateAnonymousId(): String {
        // 生成匿名用户ID，不包含真实身份信息
        return UUID.randomUUID().toString()
    }
    
    fun toAnonymizedFeatures(): Map<String, Any> {
        // 转换为匿名化特征数据
        return mapOf(
            "cycle_regularity" to cycleRegularity,
            "symptom_frequency" to symptomFrequency,
            "avg_cycle_length" to averageCycleLength
        )
    }
    
    companion object {
        fun fromMap(features: Map<String, Any>): HealthDataSummary {
            return HealthDataSummary(
                cycleRegularity = features["cycle_regularity"] as? Double ?: 0.0,
                symptomFrequency = features["symptom_frequency"] as? Map<String, Int> ?: emptyMap(),
                averageCycleLength = features["avg_cycle_length"] as? Double ?: 0.0
            )
        }
    }
}
```

### 4.2 安全传输
```kotlin
class SecureAPIClient @Inject constructor(
    private val encryptionManager: EncryptionManager
) {
    suspend inline fun <reified T, reified R> post(
        url: String,
        data: T,
        headers: Map<String, String> = emptyMap()
    ): R {
        // 1. 数据加密
        val encryptedData = encryptionManager.encrypt(Json.encodeToString(data))
        
        // 2. 添加安全头
        val secureHeaders = headers + mapOf(
            "X-Encrypted" to "true",
            "X-Timestamp" to System.currentTimeMillis().toString(),
            "X-Signature" to generateSignature(encryptedData)
        )
        
        // 3. 发送请求
        val response = httpClient.post(url) {
            headers {
                secureHeaders.forEach { (key, value) -> append(key, value) }
            }
            setBody(encryptedData)
        }
        
        // 4. 解密响应
        val decryptedResponse = encryptionManager.decrypt(response.bodyAsText())
        return Json.decodeFromString<R>(decryptedResponse)
    }
    
    private fun generateSignature(data: String): String {
        // 生成数据签名以验证完整性
        return HMACSHA256.sign(data, getSigningKey())
    }
}
```

## 5. 用户界面实现

### 5.1 AI模型选择界面
```kotlin
class AIModelSelectionFragment : Fragment() {
    private lateinit var binding: FragmentAiModelSelectionBinding
    private lateinit var viewModel: AIModelSelectionViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAiModelSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModelSelection()
        observeViewModel()
    }
    
    private fun setupModelSelection() {
        val adapter = AIModelAdapter { model ->
            viewModel.selectModel(model)
        }
        
        binding.recyclerViewModels.adapter = adapter
        binding.recyclerViewModels.layoutManager = LinearLayoutManager(context)
        
        // 获取可用的AI模型列表
        viewModel.availableModels.observe(viewLifecycleOwner) { models ->
            adapter.submitList(models)
        }
    }
    
    private fun observeViewModel() {
        viewModel.selectedModel.observe(viewLifecycleOwner) { model ->
            updateSelectionUI(model)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
    }
    
    private fun updateSelectionUI(model: AIModelType) {
        // 更新UI以反映当前选择的模型
        binding.textViewSelectedModel.text = getStringForModel(model)
    }
}
```

### 5.2 AI模型适配器
```kotlin
class AIModelAdapter(
    private val onModelSelected: (AIModelType) -> Unit
) : ListAdapter<AIModelInfo, AIModelAdapter.ModelViewHolder>(ModelDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemAiModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModelViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ModelViewHolder(
        private val binding: ItemAiModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(modelInfo: AIModelInfo) {
            binding.textViewModelName.text = modelInfo.displayName
            binding.textViewModelDescription.text = modelInfo.description
            binding.imageViewModelIcon.setImageResource(modelInfo.iconRes)
            
            binding.root.setOnClickListener {
                onModelSelected(modelInfo.modelType)
            }
            
            // 显示模型的当前状态（如是否可用）
            binding.chipStatus.apply {
                text = if (modelInfo.isAvailable) {
                    context.getString(R.string.model_available)
                } else {
                    context.getString(R.string.model_unavailable)
                }
                setChipBackgroundColorResource(
                    if (modelInfo.isAvailable) R.color.green else R.color.red
                )
            }
        }
    }
}

data class AIModelInfo(
    val modelType: AIModelType,
    val displayName: String,
    val description: String,
    val iconRes: Int,
    val isAvailable: Boolean
)

enum class AIModelType {
    DEFAULT,    // 默认模型（现有模型）
    QWEN,       // 通义千问
    INTERN,     // InternLM
    GPT         // GPT模型
}
```

## 6. 配置管理

### 6.1 AI配置管理
```kotlin
data class AIConfig(
    val qwenApiUrl: String = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
    val qwenApiKey: String = "",
    val qwenModel: String = "qwen-plus",
    
    val internApiUrl: String = "https://internlm.example.com/api/v1/chat",
    val internApiKey: String = "",
    val internModel: String = "internlm-chat-7b",
    
    val gptApiUrl: String = "https://api.openai.com/v1/chat/completions",
    val gptApiKey: String = "",
    val gptModel: String = "gpt-3.5-turbo",
    val gptTranslationModel: String = "gpt-3.5-turbo",
    
    val maxTokens: Int = 1000,
    val temperature: Float = 0.7f
) {
    companion object {
        fun fromPreferences(preferences: UserPreferences): AIConfig {
            return AIConfig(
                qwenApiKey = preferences.getQwenApiKey(),
                internApiKey = preferences.getInternApiKey(),
                gptApiKey = preferences.getGptApiKey(),
                maxTokens = preferences.getAIMaxTokens(),
                temperature = preferences.getAITemperature()
            )
        }
    }
}
```

### 6.2 用户偏好设置
```kotlin
class UserPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun setPreferredAIModel(model: AIModelType) {
        sharedPreferences.edit()
            .putString(PREF_KEY_AI_MODEL, model.name)
            .apply()
    }
    
    fun getPreferredAIModel(): AIModelType {
        val modelName = sharedPreferences.getString(PREF_KEY_AI_MODEL, AIModelType.DEFAULT.name)
        return try {
            AIModelType.valueOf(modelName ?: AIModelType.DEFAULT.name)
        } catch (e: IllegalArgumentException) {
            AIModelType.DEFAULT
        }
    }
    
    fun setQwenApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString(PREF_KEY_QWEN_API_KEY, apiKey)
            .apply()
    }
    
    fun getQwenApiKey(): String {
        return sharedPreferences.getString(PREF_KEY_QWEN_API_KEY, "") ?: ""
    }
    
    // 类似的方法用于Intern和GPT API密钥
    
    companion object {
        private const val PREF_KEY_AI_MODEL = "preferred_ai_model"
        private const val PREF_KEY_QWEN_API_KEY = "qwen_api_key"
        private const val PREF_KEY_INTERN_API_KEY = "intern_api_key"
        private const val PREF_KEY_GPT_API_KEY = "gpt_api_key"
        private const val PREF_KEY_AI_MAX_TOKENS = "ai_max_tokens"
        private const val PREF_KEY_AI_TEMPERATURE = "ai_temperature"
    }
}
```

## 7. 错误处理与降级

### 7.1 AI模型异常处理
```kotlin
sealed class AIException : Exception() {
    data class ModelUnavailableException(val model: AIModelType) : AIException()
    data class NetworkException(val cause: Throwable) : AIException()
    data class AuthenticationException(val message: String) : AIException()
    data class RateLimitException(val message: String) : AIException()
    data class InvalidRequestException(val message: String) : AIException()
}

class AIModelException(
    message: String,
    cause: Throwable? = null
) : AIException() {
    init {
        // 记录错误日志
        Log.e("AIModelException", message, cause)
    }
}
```

### 7.2 智能降级机制
```kotlin
class AIManager {
    suspend fun analyzeHealthDataWithFallback(
        data: HealthData,
        preferredModel: AIModelType
    ): AnalysisResult {
        return try {
            // 首先尝试使用首选模型
            analyzeHealthData(data, useCloud = true, preferredModel = preferredModel)
        } catch (e: AIException.ModelUnavailableException) {
            // 模型不可用，尝试下一个可用模型
            tryNextAvailableModel(data, preferredModel)
        } catch (e: AIException.NetworkException) {
            // 网络问题，降级到本地AI
            localAIEngine.analyze(data)
        } catch (e: Exception) {
            // 其他错误，尝试默认模型
            try {
                analyzeHealthData(data, useCloud = true, preferredModel = AIModelType.DEFAULT)
            } catch (fallbackException: Exception) {
                // 最后降级到本地AI
                localAIEngine.analyze(data)
            }
        }
    }
    
    private suspend fun tryNextAvailableModel(
        data: HealthData,
        failedModel: AIModelType
    ): AnalysisResult {
        val fallbackModels = when (failedModel) {
            AIModelType.QWEN -> listOf(AIModelType.INTERN, AIModelType.GPT, AIModelType.DEFAULT)
            AIModelType.INTERN -> listOf(AIModelType.QWEN, AIModelType.GPT, AIModelType.DEFAULT)
            AIModelType.GPT -> listOf(AIModelType.QWEN, AIModelType.INTERN, AIModelType.DEFAULT)
            else -> listOf(AIModelType.DEFAULT)
        }
        
        for (model in fallbackModels) {
            try {
                return analyzeHealthData(data, useCloud = true, preferredModel = model)
            } catch (e: Exception) {
                // 继续尝试下一个模型
                continue
            }
        }
        
        // 所有云端模型都失败，降级到本地AI
        return localAIEngine.analyze(data)
    }
}
```

## 8. 性能优化

### 8.1 结果缓存机制
```kotlin
class AIResultCache @Inject constructor() {
    private val cache = LruCache<String, CachedAnalysisResult>(MAX_CACHE_SIZE)
    
    fun getCachedResult(key: String): AnalysisResult? {
        val cached = cache.get(key)
        return if (cached != null && !cached.isExpired()) {
            cached.result
        } else {
            null
        }
    }
    
    fun cacheResult(key: String, result: AnalysisResult, ttlMillis: Long = DEFAULT_TTL) {
        val cachedResult = CachedAnalysisResult(
            result = result,
            timestamp = System.currentTimeMillis(),
            ttlMillis = ttlMillis
        )
        cache.put(key, cachedResult)
    }
    
    private companion object {
        const val MAX_CACHE_SIZE = 50
        const val DEFAULT_TTL = 30 * 60 * 1000L // 30分钟
    }
}

data class CachedAnalysisResult(
    val result: AnalysisResult,
    val timestamp: Long,
    val ttlMillis: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > ttlMillis
    }
}
```

### 8.2 异步处理
```kotlin
class AIManager {
    fun analyzeAsync(
        data: HealthData,
        preferredModel: AIModelType
    ): Deferred<AnalysisResult> {
        return CoroutineScope(Dispatchers.IO).async {
            analyzeHealthDataWithFallback(data, preferredModel)
        }
    }
}
```

## 9. 测试策略

### 9.1 单元测试
```kotlin
class QwenAPITest {
    private lateinit var qwenAPI: QwenAPI
    private lateinit var apiClient: MockAPIClient
    
    @Before
    fun setup() {
        apiClient = MockAPIClient()
        qwenAPI = QwenAPI(apiClient, createTestConfig())
    }
    
    @Test
    fun `healthAnalysis should return valid response`() = runTest {
        // Given
        val prompt = "分析以下健康数据..."
        val expectedResponse = "根据您的健康数据，建议..."
        
        apiClient.mockResponse = QwenResponse(
            choices = listOf(QwenChoice(text = expectedResponse))
        )
        
        // When
        val result = qwenAPI.healthAnalysis(prompt)
        
        // Then
        assertEquals(expectedResponse, result)
    }
    
    @Test
    fun `should handle API errors gracefully`() = runTest {
        // Given
        apiClient.shouldThrowException = true
        
        // When & Then
        assertThrows<AIModelException> {
            qwenAPI.healthAnalysis("test prompt")
        }
    }
}
```

### 9.2 集成测试
```kotlin
class AIManagerIntegrationTest {
    private lateinit var aiManager: AIManager
    private lateinit var cloudAPIProvider: CloudAPIProvider
    
    @Test
    fun `should use preferred model when available`() = runTest {
        // Given
        val healthData = createTestHealthData()
        val preferredModel = AIModelType.QWEN
        
        // When
        val result = aiManager.analyzeHealthData(
            data = healthData,
            useCloud = true,
            preferredModel = preferredModel
        )
        
        // Then
        verify(cloudAPIProvider).analyzeWithQwen(any())
        assertNotNull(result)
    }
}
```

## 10. 部署与监控

### 10.1 配置部署
```kotlin
class DeploymentConfig {
    companion object {
        fun getEnvironmentConfig(): AIConfig {
            return when (BuildConfig.BUILD_TYPE) {
                "debug" -> createDebugConfig()
                "release" -> createReleaseConfig()
                else -> createDefaultConfig()
            }
        }
        
        private fun createDebugConfig(): AIConfig {
            return AIConfig(
                qwenApiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
                qwenModel = "qwen-plus",
                // 测试环境的API密钥
                qwenApiKey = BuildConfig.DEBUG_QWEN_API_KEY
            )
        }
        
        private fun createReleaseConfig(): AIConfig {
            return AIConfig(
                qwenApiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
                qwenModel = "qwen-plus",
                // 生产环境的API密钥通过安全方式注入
                qwenApiKey = getSecureApiKey("qwen")
            )
        }
    }
}
```

### 10.2 性能监控
```kotlin
class AIPerformanceMonitor {
    fun recordModelCall(model: AIModelType, duration: Long, success: Boolean) {
        // 记录模型调用性能数据
        Analytics.logEvent("ai_model_call", bundleOf(
            "model" to model.name,
            "duration_ms" to duration,
            "success" to success
        ))
    }
    
    fun recordError(model: AIModelType, error: String) {
        // 记录模型调用错误
        Analytics.logEvent("ai_model_error", bundleOf(
            "model" to model.name,
            "error" to error
        ))
    }
}
```

---
文档版本: 1.0  
创建日期: 2025-11-10  
技术负责人: AI架构团队  
审核状态: 待审核