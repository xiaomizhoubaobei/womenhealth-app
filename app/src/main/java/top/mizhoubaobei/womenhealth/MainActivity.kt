package top.mizhoubaobei.womenhealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.mizhoubaobei.womenhealth.data.database.AppDatabase
import top.mizhoubaobei.womenhealth.data.database.PeriodRecord
import top.mizhoubaobei.womenhealth.data.repository.PeriodRepository
import top.mizhoubaobei.womenhealth.ui.components.AddRecordSheet
import top.mizhoubaobei.womenhealth.ui.components.AiAnalysisCard
import top.mizhoubaobei.womenhealth.ui.components.CalendarSection
import top.mizhoubaobei.womenhealth.ui.components.CycleWheel
import top.mizhoubaobei.womenhealth.ui.components.WellnessTrackerCard
import top.mizhoubaobei.womenhealth.ui.components.CycleAnalyticsCard
import top.mizhoubaobei.womenhealth.ui.components.HealingGuideCard
import top.mizhoubaobei.womenhealth.ui.components.FuturePredictionsCard
import top.mizhoubaobei.womenhealth.ui.components.MythBusterCard
import top.mizhoubaobei.womenhealth.ui.components.BbtWeightTrackerCard
import top.mizhoubaobei.womenhealth.ui.components.CycleEncyclopediaCard
import top.mizhoubaobei.womenhealth.ui.components.NextPeriodPredictionCard
import top.mizhoubaobei.womenhealth.ui.components.CycleTrendChartCard
import top.mizhoubaobei.womenhealth.ui.theme.LuminCoreTheme
import top.mizhoubaobei.womenhealth.ui.viewmodel.PeriodViewModel
import top.mizhoubaobei.womenhealth.ui.viewmodel.PeriodViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize DB, Repository & ViewModel via standard Provider
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PeriodRepository(database.periodDao())
        val factory = PeriodViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[PeriodViewModel::class.java]

        setContent {
            LuminCoreTheme {
                val records by viewModel.records.collectAsStateWithLifecycle()
                val analysis by viewModel.analysis.collectAsStateWithLifecycle()
                val aiReportState by viewModel.aiReportState.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

                val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
                val waterCups by viewModel.waterCups.collectAsStateWithLifecycle()
                val beverageType by viewModel.beverageType.collectAsStateWithLifecycle()
                val sleepQuality by viewModel.sleepQuality.collectAsStateWithLifecycle()
                val warmthLevel by viewModel.warmthLevel.collectAsStateWithLifecycle()
                val moxaFootBath by viewModel.moxaFootBath.collectAsStateWithLifecycle()
                val acupointMassage by viewModel.acupointMassage.collectAsStateWithLifecycle()
                val herbalDiet by viewModel.herbalDiet.collectAsStateWithLifecycle()

                val bbtWeightHistory by viewModel.bbtWeightHistory.collectAsStateWithLifecycle()
                val todayBbt by viewModel.todayBbt.collectAsStateWithLifecycle()
                val todayWeight by viewModel.todayWeight.collectAsStateWithLifecycle()

                var isAddRecordSheetVisible by remember { mutableStateOf(false) }
                var selectedCalendarDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { isAddRecordSheetVisible = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .padding(16.dp)
                                .testTag("add_record_fab"),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "记一笔")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("登记经期", fontWeight = FontWeight.Bold)
                        }
                    }
                ) { innerPadding ->
                    // Main Scrollable Screen Workspace Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // 1. Beautiful Header Title Banner
                            AppHeaderBanner()

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1b. Mode Switcher bar
                            ModeSwitcherBar(
                                currentMode = currentMode,
                                onModeChanged = { viewModel.updateMode(it) }
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 2. Centered Cycle Wheel Countdown
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CycleWheel(analysis = analysis)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // New Feature: Next Period Prediction Card with confidence levels & detailed timeline
                            NextPeriodPredictionCard(
                                records = records,
                                analysis = analysis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3. Dynamic Phase Suggestion Tips Box
                            PhaseAdviceBox(
                                currentPhase = analysis.currentPhase,
                                description = if (currentMode == "CONCEPTION" && analysis.currentPhase in listOf("易孕期", "排卵期")) {
                                    "✨ 黄金备孕提醒：您当前正处于【黄金排卵准备期】！体温可能轻微上升，宫颈白带拉丝。正是受孕概率的巅峰，保持心情舒畅、注意腹部温敷、补充叶酸，爱人共同配合，好孕连连哦！👶"
                                } else if (currentMode == "CONCEPTION") {
                                    "${analysis.phaseDescription} 💡 温馨备孕推荐：补充优质蛋白质，每天温水泡脚20分钟暖宫，为下一次新周期积蓄饱满生命力量。"
                                } else {
                                    analysis.phaseDescription
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3b. Daily self-care hydration tracker Card
                            WellnessTrackerCard(
                                waterCups = waterCups,
                                beverageType = beverageType,
                                sleepQuality = sleepQuality,
                                warmthLevel = warmthLevel,
                                onUpdateWater = { viewModel.updateWaterCups(it) },
                                onUpdateBeverage = { viewModel.updateBeverageType(it) },
                                onUpdateSleep = { viewModel.updateSleepQuality(it) },
                                onUpdateWarmth = { viewModel.updateWarmthLevel(it) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // New Feature 1: Basal Body Temperature & Weight Trends Tracker
                            BbtWeightTrackerCard(
                                history = bbtWeightHistory,
                                todayBbt = todayBbt,
                                todayWeight = todayWeight,
                                onUpdateBbt = { viewModel.updateBbt(it) },
                                onUpdateWeight = { viewModel.updateWeight(it) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3c. Interactive Healing, Massage, and herbal diet tracker Card
                            HealingGuideCard(
                                herbCompleted = herbalDiet,
                                footBathCompleted = moxaFootBath,
                                massageCompleted = acupointMassage,
                                onToggleHerb = { viewModel.toggleHerbalDiet() },
                                onToggleFootBath = { viewModel.toggleMoxaFootBath() },
                                onToggleMassage = { viewModel.toggleAcupointMassage() },
                                currentPhase = analysis.currentPhase
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. Calendar section
                            Text(
                                text = "📅 生理周期日历",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            CalendarSection(
                                records = records,
                                analysis = analysis,
                                onDateSelected = { date ->
                                    selectedCalendarDate = date
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4b. Dynamic stats analytics dashboard Card
                            CycleAnalyticsCard(
                                records = records
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Past 6 Months Cycle length trend visualization chart
                            CycleTrendChartCard(
                                records = records
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4c. Future Predictions Timeline Card
                            FuturePredictionsCard(
                                analysis = analysis
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // New Feature 3: Menstrual Cycle Biological Phases Encyclopedia
                            CycleEncyclopediaCard()

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4d. Myth Buster Carousel Card
                            MythBusterCard()

                            Spacer(modifier = Modifier.height(24.dp))

                            // 5. Intelligent AI Analyser Hub (Diagnostic report generation and Q&A chat)
                            AiAnalysisCard(
                                aiState = aiReportState,
                                chatMessages = chatMessages,
                                onGenerateReport = { viewModel.generateAiReport() },
                                onSendMessage = { text -> viewModel.askAiQuestion(text) },
                                onClearChat = { viewModel.clearChat() }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 6. Local Historical Record Log Listing (CRUD list)
                            Text(
                                text = "🩸 经期历史记录 (共 ${records.size} 次)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HistoryRecordsList(
                                records = records,
                                onDeleteRecord = { record -> viewModel.deletePeriodRecord(record) }
                            )

                            Spacer(modifier = Modifier.height(100.dp)) // Floating button clearance padding
                        }

                        // Overlying dialog sheet (Modal Trigger)
                        if (isAddRecordSheetVisible) {
                            AddRecordSheet(
                                onDismissRequest = { isAddRecordSheetVisible = false },
                                onSaveRecord = { startDateStr, endDateStr, flowVal, symps, mds, note ->
                                    viewModel.addPeriodRecord(startDateStr, endDateStr, flowVal, symps, mds, note)
                                    isAddRecordSheetVisible = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub-components for modern look

@Composable
fun AppHeaderBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(20.dp)
            .testTag("app_header_banner")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PHYSIO CARE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "生理期生理健康管理分析",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "您贴心温柔的周期规律管理与 AI 调护顾问",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun PhaseAdviceBox(
    currentPhase: String,
    description: String
) {
    val phaseColor = when (currentPhase) {
        "月经期" -> Color(0xFFF48FB1)
        "易孕期", "排卵期" -> Color(0xFF80CBC4)
        "黄体期" -> Color(0xFFFFCC80)
        else -> Color(0xFFCE93D8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = phaseColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = phaseColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(phaseColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "💡 生理黄金阶段指导 (${currentPhase})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun HistoryRecordsList(
    records: List<PeriodRecord>,
    onDeleteRecord: (PeriodRecord) -> Unit
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "还没有登记过生理卡片哦~ 赶紧点击右下角登记一个吧！",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            records.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_record_card_${record.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📅 ${record.startDate} " + if (record.endDate != null) "至 ${record.endDate}" else "(持续进行中)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { onDeleteRecord(record) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_record_btn_${record.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "删除记录",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Attributes row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Flow volume Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "流量: ${record.flow}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Symptoms list
                            if (record.symptoms.isNotBlank() && record.symptoms != "无症状") {
                                record.symptoms.split(",").forEach { s ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x15AB47BC), // purple-transparent
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = s,
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E24AA)
                                        )
                                    }
                                }
                            }

                            // Moods list
                            if (record.mood.isNotBlank() && record.mood != "平静") {
                                record.mood.split(",").forEach { m ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x15FF8A80), // peach-transparent
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = m,
                                            fontSize = 10.sp,
                                            color = Color(0xFFD84315)
                                        )
                                    }
                                }
                            }
                        }

                        // Notes if available
                        if (record.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "📝 备忘：${record.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSwitcherBar(
    currentMode: String,
    onModeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp)
    ) {
        val options = listOf(
            "CONTRACEPTION" to "贴心避孕 🛡️",
            "CONCEPTION" to "黄金备孕 🌸"
        )
        options.forEach { (modeVal, label) ->
            val isSelected = currentMode == modeVal
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(21.dp))
                    .background(bg)
                    .clickable { onModeChanged(modeVal) }
                    .testTag("mode_btn_$modeVal"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
