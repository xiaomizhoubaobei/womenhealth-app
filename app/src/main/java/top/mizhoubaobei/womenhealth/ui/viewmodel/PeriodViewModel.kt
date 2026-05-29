package top.mizhoubaobei.womenhealth.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import top.mizhoubaobei.womenhealth.data.database.AppDatabase
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import top.mizhoubaobei.womenhealth.data.repository.CycleAnalysis
import top.mizhoubaobei.womenhealth.data.repository.PeriodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface AiState {
    object Idle : AiState
    object Loading : AiState
    data class Success(val response: String) : AiState
    data class Error(val error: String) : AiState
}

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val isPlaceholder: Boolean = false
)

class PeriodViewModel(
    application: Application,
    private val repository: PeriodRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("lumin_core_prefs", android.content.Context.MODE_PRIVATE)
    private val todayStr = LocalDate.now().toString()

    private val _currentMode = MutableStateFlow(prefs.getString("current_mode", "CONTRACEPTION") ?: "CONTRACEPTION")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    private val _waterCups = MutableStateFlow(prefs.getInt("water_cups_$todayStr", 0))
    val waterCups: StateFlow<Int> = _waterCups.asStateFlow()

    private val _beverageType = MutableStateFlow(prefs.getString("beverage_type_$todayStr", "温开水 💧") ?: "温开水 💧")
    val beverageType: StateFlow<String> = _beverageType.asStateFlow()

    private val _sleepQuality = MutableStateFlow(prefs.getString("sleep_quality_$todayStr", "一般") ?: "一般")
    val sleepQuality: StateFlow<String> = _sleepQuality.asStateFlow()

    private val _warmthLevel = MutableStateFlow(prefs.getString("warmth_level_$todayStr", "适中") ?: "适中")
    val warmthLevel: StateFlow<String> = _warmthLevel.asStateFlow()

    private val _moxaFootBath = MutableStateFlow(prefs.getBoolean("moxa_foot_bath_$todayStr", false))
    val moxaFootBath: StateFlow<Boolean> = _moxaFootBath.asStateFlow()

    private val _acupointMassage = MutableStateFlow(prefs.getBoolean("acupoint_massage_$todayStr", false))
    val acupointMassage: StateFlow<Boolean> = _acupointMassage.asStateFlow()

    private val _herbalDiet = MutableStateFlow(prefs.getBoolean("herbal_diet_$todayStr", false))
    val herbalDiet: StateFlow<Boolean> = _herbalDiet.asStateFlow()

    fun updateMode(mode: String) {
        prefs.edit().putString("current_mode", mode).apply()
        _currentMode.value = mode
    }

    fun updateWaterCups(cups: Int) {
        val nonNegativeCups = cups.coerceAtLeast(0)
        prefs.edit().putInt("water_cups_$todayStr", nonNegativeCups).apply()
        _waterCups.value = nonNegativeCups
    }

    fun updateBeverageType(beverage: String) {
        prefs.edit().putString("beverage_type_$todayStr", beverage).apply()
        _beverageType.value = beverage
    }

    fun updateSleepQuality(quality: String) {
        prefs.edit().putString("sleep_quality_$todayStr", quality).apply()
        _sleepQuality.value = quality
    }

    fun updateWarmthLevel(warmth: String) {
        prefs.edit().putString("warmth_level_$todayStr", warmth).apply()
        _warmthLevel.value = warmth
    }

    fun toggleMoxaFootBath() {
        val nextVal = !_moxaFootBath.value
        prefs.edit().putBoolean("moxa_foot_bath_$todayStr", nextVal).apply()
        _moxaFootBath.value = nextVal
    }

    fun toggleAcupointMassage() {
        val nextVal = !_acupointMassage.value
        prefs.edit().putBoolean("acupoint_massage_$todayStr", nextVal).apply()
        _acupointMassage.value = nextVal
    }

    fun toggleHerbalDiet() {
        val nextVal = !_herbalDiet.value
        prefs.edit().putBoolean("herbal_diet_$todayStr", nextVal).apply()
        _herbalDiet.value = nextVal
    }

    // Period History records from local storage
    val records: StateFlow<List<PeriodRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived period analysis (averages, predictions, phases)
    val analysis: StateFlow<CycleAnalysis> = records
        .map { recordList ->
            repository.calculateCycleAnalysis(recordList, LocalDate.now())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.calculateCycleAnalysis(emptyList(), LocalDate.now())
        )

    // Current AI cycle report state
    private val _aiReportState = MutableStateFlow<AiState>(AiState.Idle)
    val aiReportState: StateFlow<AiState> = _aiReportState.asStateFlow()

    // Conversational Chat Bot Messages with Gemini
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "你好呀！我是你的女性生理健康顾问。今天感觉怎么样呢？如果你有痛经不适、经期调理、或者周期变化的疑惑，随时可以在这里问我哦！💜",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        // Seed some highly realistic mock periods on very first boot so the visual chart elements are active!
        viewModelScope.launch {
            val directRecords = repository.getAllRecordsDirect()
            if (directRecords.isEmpty()) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val today = LocalDate.now()

                // Register 1: A period starting 10 days ago, lasting 5 days (currently day 11 of cycle, in follicar phase!)
                val start1 = today.minusDays(10).format(formatter)
                val end1 = today.minusDays(6).format(formatter)
                val record1 = PeriodRecord(
                    startDate = start1,
                    endDate = end1,
                    flow = "正常",
                    symptoms = "痛经,腹胀",
                    mood = "敏感,疲惫",
                    notes = "这个月肚子有点胀，第一天有点轻微痛经，后面喝了红糖水好了很多。"
                )

                // Register 2: A past period starting 38 days ago, lasting 5 days
                val start2 = today.minusDays(38).format(formatter)
                val end2 = today.minusDays(34).format(formatter)
                val record2 = PeriodRecord(
                    startDate = start2,
                    endDate = end2,
                    flow = "较多",
                    symptoms = "乳房胀痛,粉刺",
                    mood = "郁闷,平静",
                    notes = "工作有点忙，心情比较郁闷，皮肤稍微爆了一两颗痘痘，整体经量比较多。"
                )

                repository.insert(record2)
                repository.insert(record1)
            }
        }
    }

    fun addPeriodRecord(startDate: String, endDate: String?, flow: String, symptoms: List<String>, moods: List<String>, notes: String) {
        viewModelScope.launch {
            val record = PeriodRecord(
                startDate = startDate,
                endDate = endDate,
                flow = flow,
                symptoms = symptoms.joinToString(","),
                mood = moods.joinToString(","),
                notes = notes
            )
            repository.insert(record)
        }
    }

    fun deletePeriodRecord(record: PeriodRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }

    fun generateAiReport() {
        viewModelScope.launch {
            _aiReportState.value = AiState.Loading
            try {
                val recordsList = records.value
                val currentAnalysis = analysis.value
                
                val prompt = buildString {
                    append("请基于以下女性生理期历史数据进行一期深度的‘生理周期规律性与健康分析报告’：\n")
                    if (recordsList.isEmpty()) {
                        append("- 目前尚无历史追踪记录。\n")
                    } else {
                        recordsList.take(5).forEach { record ->
                            append("- 历史经期：${record.startDate} 至 ${record.endDate ?: "进行中"} (经量强度：${record.flow}, 伴随症状：${record.symptoms}, 情绪波动：${record.mood})\n")
                        }
                    }
                    append("\n【当前周期指标数据】：\n")
                    append("- 平均周期：${currentAnalysis.avgCycleLength}天\n")
                    append("- 平均经期：${currentAnalysis.avgPeriodLength}天\n")
                    append("- 当前处于周期第：${currentAnalysis.currentCycleDay}天\n")
                    append("- 当前生理相位：${currentAnalysis.currentPhase} (${currentAnalysis.phaseDescription})\n")
                    append("- 下次预测经期：${currentAnalysis.nextPeriodDate}\n")
                    append("- 下次预测排卵：${currentAnalysis.ovulationDate}\n")
                    
                    val modeText = if (currentMode.value == "CONCEPTION") "【备孕管理模式 🌸】" else "【贴心安全避孕模式 🛡️】"
                    append("\n【当前使用模式】：$modeText\n")
                    if (currentMode.value == "CONCEPTION") {
                        append("- 请特别分析其排卵预测日（${currentAnalysis.ovulationDate}）和前后5天的黄金受孕时间窗，引导备孕知识、补充叶酸建议、以及子宫温养法。\n")
                    } else {
                        append("- 请特别分析易孕危险期时段，提供高安全性提醒，以及防范体寒和经前综合征（PMS）的调护对策。\n")
                    }

                    append("\n请生成一份专业的健康分析，必须包含以下内容（使用精美 Markdown 格式和可爱女性化的话符，多用标题与列点，中文回答）：\n")
                    append("1. **当前模式定制分析**：结合当前使用的模式给予专业方向解释（备孕受孕黄金期 vs 避孕安全期防护）。\n")
                    append("2. **周期特征诊断**：评估周期的波动情况（如短或长，以及流速正常度）。\n")
                    append("3. **今日生理相位贴心调理**：针对处于‘${currentAnalysis.currentPhase}’，从温热饮食（温水、红糖、泡脚、艾灸等）、运动限制与优质睡眠给出精确养生建议。\n")
                    append("4. **历史合并症状专门舒缓**：针对历史合并症状（${recordsList.flatMap { it.symptoms.split(",") }.filter { it.isNotBlank() && it != "无症状" }.distinct().joinToString(" & ")}），给出舒缓止痛改善建议。\n")
                    append("最后的落款写上：‘来自您的 AI 智能健康管家 LuminCore’。")
                }

                // Call Gemini Direct REST Api via retrofitted service
                val response = top.mizhoubaobei.womenhealth.data.api.RetrofitClient.service.generateContent(
                    apiKey = top.mizhoubaobei.womenhealth.BuildConfig.GEMINI_API_KEY,
                    request = top.mizhoubaobei.womenhealth.data.api.GenerateContentRequest(
                        contents = listOf(
                            top.mizhoubaobei.womenhealth.data.api.Content(
                                parts = listOf(top.mizhoubaobei.womenhealth.data.api.Part(text = prompt))
                            )
                        ),
                        systemInstruction = top.mizhoubaobei.womenhealth.data.api.SystemInstruction(
                            parts = listOf(top.mizhoubaobei.womenhealth.data.api.Part(text = "你是一位极度温柔、贴心、具备医学理智但也温情满满的女性生理期健康调理助理。"))
                        )
                    )
                )

                val contentText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!contentText.isNullOrBlank()) {
                    _aiReportState.value = AiState.Success(contentText)
                } else {
                    _aiReportState.value = AiState.Error("未能生成分析报告，可能由于模型拒绝响应。")
                }
            } catch (e: Exception) {
                _aiReportState.value = AiState.Error("分析报告生成失败，请检查网络：${e.localizedMessage}")
            }
        }
    }

    fun askAiQuestion(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(text = question, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val placeholderId = System.nanoTime()
            val botLoadingMsg = ChatMessage(id = placeholderId, text = "正在认真思索，为你定制健康建议...", isUser = false, isPlaceholder = true)
            _chatMessages.value = _chatMessages.value + botLoadingMsg

            try {
                val currentAnalysis = analysis.value
                val context = "用户当前生理档案：处于 ${currentAnalysis.currentPhase}（周期第 ${currentAnalysis.currentCycleDay} 天，下次预测经期 ${currentAnalysis.nextPeriodDate}），平均周期 ${currentAnalysis.avgCycleLength} 天，经期 ${currentAnalysis.avgPeriodLength} 天。"
                val prompt = "$context\n用户提出一个生理/情绪方面的疑惑：\n“$question”\n请以无比温柔专业的声音安慰对方并给出科学调理办法（350字以内，重点突出点句）："

                val response = top.mizhoubaobei.womenhealth.data.api.RetrofitClient.service.generateContent(
                    apiKey = top.mizhoubaobei.womenhealth.BuildConfig.GEMINI_API_KEY,
                    request = top.mizhoubaobei.womenhealth.data.api.GenerateContentRequest(
                        contents = listOf(
                            top.mizhoubaobei.womenhealth.data.api.Content(
                                parts = listOf(top.mizhoubaobei.womenhealth.data.api.Part(text = prompt))
                            )
                        ),
                        systemInstruction = top.mizhoubaobei.womenhealth.data.api.SystemInstruction(
                            parts = listOf(top.mizhoubaobei.womenhealth.data.api.Part(text = "你是一位贴心可爱的专业女性助理，解答调理、止痛、情绪不佳问题。"))
                        )
                    )
                )

                val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "抱歉，没有搜寻到优质建议，可以换种问法试试~"

                _chatMessages.value = _chatMessages.value.filter { it.id != placeholderId } + ChatMessage(text = answer, isUser = false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value.filter { it.id != placeholderId } + ChatMessage(text = "网络连接不太顺畅，稍微重试一下哦 (${e.localizedMessage})", isUser = false)
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "已为您开启新的健康话题对话！💜 有什么最新的生理疑难，直接在这里抛出吧！",
                isUser = false
            )
        )
    }
}

class PeriodViewModelFactory(
    private val application: Application,
    private val repository: PeriodRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeriodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeriodViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
