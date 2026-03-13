# PMS 症状记录功能开发文档

## 1. 功能概述

PMS（经前综合征）症状记录功能允许用户记录和管理经前期的各种生理和心理症状，帮助用户追踪症状模式，识别周期性变化，并提供数据分析和健康建议。

### 核心功能
- 症状类型管理（生理症状、心理症状、行为症状）
- 症状严重程度记录（1-5级）
- 症状出现时间记录
- 症状日历视图
- 症状趋势分析
- 症状模式识别
- 个性化健康建议

---

## 2. 数据库设计

### 2.1 PMS 症状记录表（pms_symptoms）

```kotlin
@Entity(tableName = "pms_symptoms")
data class PMSSymptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val recordDate: Long,                    // 记录日期（时间戳）
    val cycleId: Long?,                      // 关联的月经周期ID（可选）
    
    // 生理症状
    val breastTenderness: Int?,              // 乳房胀痛（0-5，0=无，5=严重）
    val abdominalPain: Int?,                 // 腹痛/痛经（0-5）
    val headache: Int?,                      // 头痛（0-5）
    val backache: Int?,                      // 背痛（0-5）
    val jointPain: Int?,                     // 关节痛（0-5）
    val bloating: Int?,                      // 腹胀（0-5）
    const val constipation: Int?,            // 便秘（0-5）
    const val diarrhea: Int?,                // 腹泻（0-5）
    const val nausea: Int?,                  // 恶心（0-5）
    const val appetiteChange: Int?,          // 食欲变化（0-5）
    const val foodCravings: Int?,            // 食物渴望（0-5）
    const val weightGain: Float?,            // 体重增加（kg）
    const val skinChanges: Int?,             // 皮肤变化（痘痘）（0-5）
    const val fatigue: Int?,                 // 疲劳（0-5）
    const val sleepDisturbance: Int?,        // 睡眠障碍（0-5）
    const val hotFlashes: Int?,              // 潮热（0-5）
    const val coldSweats: Int?,              // 冷汗（0-5）
    
    // 心理症状
    const val moodSwings: Int?,              // 情绪波动（0-5）
    const val irritability: Int?,            // 易怒（0-5）
    const val anxiety: Int?,                 // 焦虑（0-5）
    const val depression: Int?,              // 抑郁（0-5）
    const val sadness: Int?,                 // 悲伤（0-5）
    const val anger: Int?,                   // 愤怒（0-5）
    const val cryingSpells: Int?,            // 想哭（0-5）
    const val socialWithdrawal: Int?,        // 社交退缩（0-5）
    const val concentrationIssues: Int?,     // 注意力不集中（0-5）
    const val memoryIssues: Int?,            // 记忆力问题（0-5）
    const val lowLibido: Int?,               // 性欲低下（0-5）
    
    // 行为症状
    const val aggression: Int?,              // 攻击性（0-5）
    const val restlessness: Int?,            // 焦躁不安（0-5）
    const val decreasedEnergy: Int?,         // 精力下降（0-5）
    
    // 附加信息
    const val notes: String?,                // 备注
    const val createdAt: Long = System.currentTimeMillis(),
    const val updatedAt: Long = System.currentTimeMillis()
)
```

### 2.2 症严重程度说明

| 等级 | 描述               |
|----|------------------|
| 0  | 无症状              |
| 1  | 轻微 - 不影响日常生活     |
| 2  | 轻度 - 略有影响，可忽略    |
| 3  | 中度 - 影响日常生活，需要关注 |
| 4  | 重度 - 明显影响，需要采取措施 |
| 5  | 严重 - 严重影响，需要就医   |

---

## 3. DAO 接口设计

```kotlin
@Dao
interface PMSSymptomDao {
    
    // 基础 CRUD 操作
    @Query("SELECT * FROM pms_symptoms ORDER BY recordDate DESC")
    fun getAllSymptoms(): Flow<List<PMSSymptom>>

    @Query("SELECT * FROM pms_symptoms WHERE id = :id")
    suspend fun getSymptomById(id: Long): PMSSymptom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: PMSSymptom): Long

    @Update
    suspend fun updateSymptom(symptom: PMSSymptom)

    @Delete
    suspend fun deleteSymptom(symptom: PMSSymptom)

    @Query("DELETE FROM pms_symptoms WHERE id = :id")
    suspend fun deleteSymptomById(id: Long)
    
    // 按日期范围查询
    @Query("SELECT * FROM pms_symptoms WHERE recordDate >= :startDate AND recordDate <= :endDate ORDER BY recordDate DESC")
    fun getSymptomsByDateRange(startDate: Long, endDate: Long): Flow<List<PMSSymptom>>
    
    // 按日期查询（精确）
    @Query("SELECT * FROM pms_symptoms WHERE recordDate = :date")
    suspend fun getSymptomByDate(date: Long): PMSSymptom?
    
    // 获取最近的记录
    @Query("SELECT * FROM pms_symptoms ORDER BY recordDate DESC LIMIT 1")
    suspend fun getLatestSymptom(): PMSSymptom?
    
    // 关联特定周期的症状
    @Query("SELECT * FROM pms_symptoms WHERE cycleId = :cycleId ORDER BY recordDate DESC")
    fun getSymptomsByCycle(cycleId: Long): Flow<List<PMSSymptom>>
    
    // 统计特定症状的平均严重程度
    @Query("SELECT AVG(moodSwings) FROM pms_symptoms WHERE moodSwings IS NOT NULL")
    suspend fun getAverageMoodSwings(): Double?
    
    @Query("SELECT AVG(abdominalPain) FROM pms_symptoms WHERE abdominalPain IS NOT NULL")
    suspend fun getAverageAbdominalPain(): Double?
    
    // 获取最近N天的症状趋势
    @Query("SELECT * FROM pms_symptoms ORDER BY recordDate DESC LIMIT :days")
    suspend fun getRecentSymptoms(days: Int): List<PMSSymptom>
    
    // 删除指定日期之前的所有记录
    @Query("DELETE FROM pms_symptoms WHERE recordDate < :date")
    suspend fun deleteSymptomsBeforeDate(date: Long)
    
    // 获取特定症状的严重程度分布
    @Query("SELECT moodSwings, COUNT(*) as count FROM pms_symptoms WHERE moodSwings IS NOT NULL GROUP BY moodSwings")
    suspend fun getMoodSwingsDistribution(): List<SymptomDistribution>
    
    // 获取症状记录数量
    @Query("SELECT COUNT(*) FROM pms_symptoms")
    suspend fun getSymptomCount(): Int
}

// 用于存储症状分布的辅助类
data class SymptomDistribution(
    val severity: Int?,
    val count: Int
)
```

---

## 4. Repository 层实现

```kotlin
class PMSSymptomRepository(private val pmsSymptomDao: PMSSymptomDao) {
    
    // 获取所有症状记录
    fun getAllSymptoms(): Flow<List<PMSSymptom>> = pmsSymptomDao.getAllSymptoms()
    
    // 获取特定日期的症状
    suspend fun getSymptomByDate(date: Long): PMSSymptom? = pmsSymptomDao.getSymptomByDate(date)
    
    // 获取日期范围内的症状
    fun getSymptomsByDateRange(startDate: Long, endDate: Long): Flow<List<PMSSymptom>> = 
        pmsSymptomDao.getSymptomsByDateRange(startDate, endDate)
    
    // 获取关联周期的症状
    fun getSymptomsByCycle(cycleId: Long): Flow<List<PMSSymptom>> = 
        pmsSymptomDao.getSymptomsByCycle(cycleId)
    
    // 插入症状记录
    suspend fun insertSymptom(symptom: PMSSymptom): Long = pmsSymptomDao.insertSymptom(symptom)
    
    // 更新症状记录
    suspend fun updateSymptom(symptom: PMSSymptom) = pmsSymptomDao.updateSymptom(symptom)
    
    // 删除症状记录
    suspend fun deleteSymptom(symptom: PMSSymptom) = pmsSymptomDao.deleteSymptom(symptom)
    
    // 获取最近记录
    suspend fun getLatestSymptom(): PMSSymptom? = pmsSymptomDao.getLatestSymptom()
    
    // 获取症状趋势数据
    suspend fun getRecentSymptoms(days: Int = 30): List<PMSSymptom> = 
        pmsSymptomDao.getRecentSymptoms(days)
    
    // 获取平均症状严重程度
    suspend fun getAverageMoodSwings(): Double? = pmsSymptomDao.getAverageMoodSwings()
    
    suspend fun getAverageAbdominalPain(): Double? = pmsSymptomDao.getAverageAbdominalPain()
    
    // 获取症状记录总数
    suspend fun getSymptomCount(): Int = pmsSymptomDao.getSymptomCount()
}
```

---

## 5. ViewModel 层实现

```kotlin
class PMSSymptomViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: PMSSymptomRepository
    private val womenHealthRepository: WomenHealthRepository
    
    // 症状列表
    private val _symptoms = MutableStateFlow<List<PMSSymptom>>(emptyList())
    val symptoms: StateFlow<List<PMSSymptom>> = _symptoms.asStateFlow()
    
    // 当前编辑的症状
    private val _currentSymptom = MutableStateFlow<PMSSymptom?>(null)
    val currentSymptom: StateFlow<PMSSymptom?> = _currentSymptom.asStateFlow()
    
    // 选中的日期
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()
    
    // 症状统计数据
    private val _symptomStats = MutableStateFlow<SymptomStats?>(null)
    val symptomStats: StateFlow<SymptomStats?> = _symptomStats.asStateFlow()
    
    // UI 状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        val database = WomenHealthDatabase.getDatabase(application)
        // 注意：需要更新 WomenHealthDatabase 以包含 PMSSymptom
        // repository = PMSSymptomRepository(database.pmsSymptomDao())
        // womenHealthRepository = WomenHealthRepository(database.menstrualCycleDao())
        loadSymptoms()
    }
    
    private fun loadSymptoms() {
        viewModelScope.launch {
            repository.getAllSymptoms().collect { symptomList ->
                _symptoms.value = symptomList
                calculateStats(symptomList)
            }
        }
    }
    
    // 计算症状统计
    private fun calculateStats(symptomList: List<PMSSymptom>) {
        if (symptomList.isEmpty()) {
            _symptomStats.value = null
            return
        }
        
        viewModelScope.launch {
            val moodAvg = repository.getAverageMoodSwings()
            val painAvg = repository.getAverageAbdominalPain()
            
            _symptomStats.value = SymptomStats(
                totalRecords = symptomList.size,
                averageMoodSwings = moodAvg,
                averageAbdominalPain = painAvg,
                mostCommonSeverity = calculateMostCommonSeverity(symptomList)
            )
        }
    }
    
    // 计算最常见的症状严重程度
    private fun calculateMostCommonSeverity(symptoms: List<PMSSymptom>): Int {
        val severityMap = mutableMapOf<Int, Int>()
        symptoms.forEach { symptom ->
            // 统计所有有值的症状的平均严重程度
            symptom.moodSwings?.let { severityMap[it] = (severityMap[it] ?: 0) + 1 }
            symptom.abdominalPain?.let { severityMap[it] = (severityMap[it] ?: 0) + 1 }
            symptom.headache?.let { severityMap[it] = (severityMap[it] ?: 0) + 1 }
            symptom.fatigue?.let { severityMap[it] = (severityMap[it] ?: 0) + 1 }
        }
        return severityMap.maxByOrNull { it.value }?.key ?: 0
    }
    
    // 保存症状
    fun saveSymptom(symptom: PMSSymptom) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (symptom.id == 0L) {
                    repository.insertSymptom(symptom)
                } else {
                    repository.updateSymptom(symptom)
                }
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }
    
    // 删除症状
    fun deleteSymptom(symptom: PMSSymptom) {
        viewModelScope.launch {
            try {
                repository.deleteSymptom(symptom)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // 选择日期
    fun selectDate(date: Long) {
        _selectedDate.value = date
        viewModelScope.launch {
            val symptom = repository.getSymptomByDate(date)
            _currentSymptom.value = symptom
        }
    }
    
    // 加载指定日期范围的症状
    fun loadSymptomsByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            repository.getSymptomsByDateRange(startDate, endDate).collect { symptomList ->
                _symptoms.value = symptomList
                calculateStats(symptomList)
            }
        }
    }
    
    // 重置当前症状
    fun resetCurrentSymptom() {
        _currentSymptom.value = null
    }
    
    // 清除错误
    fun clearError() {
        _error.value = null
    }
}

// 症状统计数据类
data class SymptomStats(
    val totalRecords: Int,
    val averageMoodSwings: Double?,
    val averageAbdominalPain: Double?,
    val mostCommonSeverity: Int
)
```

---

## 6. UI 层实现

### 6.1 Fragment 实现

```kotlin
class PMSSymptomsFragment : Fragment() {
    
    private var _binding: FragmentPmsSymptomsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: PMSSymptomViewModel
    private lateinit var adapter: PMSSymptomsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPmsSymptomsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[PMSSymptomViewModel::class.java]
        
        setupUI()
        observeViewModel()
        
        // 加载今天的日期
        val today = Calendar.getInstance().timeInMillis
        viewModel.selectDate(today)
    }
    
    private fun setupUI() {
        // 设置 RecyclerView
        adapter = PMSSymptomsAdapter { symptom ->
            viewModel.selectDate(symptom.recordDate)
        }
        binding.symptomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.symptomsRecyclerView.adapter = adapter
        
        // 设置日历视图
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            viewModel.selectDate(calendar.timeInMillis)
        }
        
        // 设置添加按钮
        binding.addSymptomButton.setOnClickListener {
            showAddSymptomDialog()
        }
        
        // 设置筛选按钮
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
    }
    
    private fun observeViewModel() {
        // 观察症状列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.symptoms.collect { symptoms ->
                adapter.submitList(symptoms)
            }
        }
        
        // 观察当前编辑的症状
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentSymptom.collect { symptom ->
                if (symptom != null) {
                    showEditSymptomDialog(symptom)
                }
            }
        }
        
        // 观察加载状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        // 观察错误信息
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
        
        // 观察统计数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.symptomStats.collect { stats ->
                if (stats != null) {
                    updateStatsView(stats)
                }
            }
        }
    }
    
    private fun showAddSymptomDialog() {
        val dialog = PMSSymptomDialog.newInstance(null) { symptom ->
            viewModel.saveSymptom(symptom)
        }
        dialog.show(childFragmentManager, "PMSSymptomDialog")
    }
    
    private fun showEditSymptomDialog(symptom: PMSSymptom) {
        val dialog = PMSSymptomDialog.newInstance(symptom) { updatedSymptom ->
            viewModel.saveSymptom(updatedSymptom)
            viewModel.resetCurrentSymptom()
        }
        dialog.show(childFragmentManager, "PMSSymptomDialog")
    }
    
    private fun showFilterDialog() {
        // 实现筛选对话框
    }
    
    private fun updateStatsView(stats: SymptomStats) {
        binding.totalRecordsTextView.text = "总记录: ${stats.totalRecords}"
        binding.avgMoodTextView.text = "平均情绪波动: ${"%.1f".format(stats.averageMoodSwings ?: 0.0)}"
        binding.avgPainTextView.text = "平均腹痛: ${"%.1f".format(stats.averageAbdominalPain ?: 0.0)}"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### 6.2 布局文件（fragment_pms_symptoms.xml）

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F5F5F5">

    <!-- 顶部统计卡片 -->
    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="症状统计"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#333333" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/totalRecordsTextView"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="总记录: 0"
                    android:textSize="14sp"
                    android:textColor="#666666" />

                <TextView
                    android:id="@+id/avgMoodTextView"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="平均情绪: 0"
                    android:textSize="14sp"
                    android:textColor="#666666" />

                <TextView
                    android:id="@+id/avgPainTextView"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="平均腹痛: 0"
                    android:textSize="14sp"
                    android:textColor="#666666" />
            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- 日历视图 -->
    <CalendarView
        android:id="@+id/calendarView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp" />

    <!-- 操作按钮 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:layout_marginHorizontal="16dp"
        android:orientation="horizontal"
        android:gravity="center">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/addSymptomButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="8dp"
            android:text="添加症状"
            android:textSize="14sp"
            app:icon="@drawable/ic_add"
            app:cornerRadius="24dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/filterButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="8dp"
            android:text="筛选"
            android:textSize="14sp"
            app:icon="@drawable/ic_filter"
            app:cornerRadius="24dp" />
    </LinearLayout>

    <!-- 症状列表 -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/symptomsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="16dp"
        android:layout_marginHorizontal="16dp"
        android:layout_marginBottom="16dp"
        android:clipToPadding="false" />

    <!-- 进度条 -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

</LinearLayout>
```

### 6.3 症状记录对话框（dialog_pms_symptom.xml）

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- 生理症状部分 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="生理症状"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#333333"
            android:layout_marginBottom="12dp" />

        <!-- 乳房胀痛 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="乳房胀痛"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/breastTendernessSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/breastTendernessValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 腹痛/痛经 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="腹痛/痛经"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/abdominalPainSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/abdominalPainValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 头痛 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="头痛"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/headacheSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/headacheValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 腹胀 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="腹胀"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/bloatingSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/bloatingValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 疲劳 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="疲劳"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/fatigueSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/fatigueValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 心理症状部分 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="心理症状"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#333333"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="12dp" />

        <!-- 情绪波动 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="情绪波动"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/moodSwingsSeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/moodSwingsValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 易怒 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="易怒"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/irritabilitySeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/irritabilityValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 焦虑 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="焦虑"
            android:textSize="14sp"
            android:textColor="#666666" />

        <SeekBar
            android:id="@+id/anxietySeekBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="5"
            android:progress="0" />

        <TextView
            android:id="@+id/anxietyValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="无"
            android:textSize="12sp"
            android:textColor="#999999"
            android:layout_marginBottom="8dp" />

        <!-- 备注 -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="备注"
            android:textSize="14sp"
            android:textColor="#666666"
            android:layout_marginTop="16dp" />

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/notesEditText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="添加备注..."
                android:minHeight="100dp"
                android:gravity="top"
                android:inputType="textMultiLine" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- 操作按钮 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:orientation="horizontal">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/cancelButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="8dp"
                android:text="取消"
                app:cornerRadius="24dp"
                style="@style/Widget.Material3.Button.OutlinedButton" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/saveButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="8dp"
                android:text="保存"
                app:cornerRadius="24dp" />
        </LinearLayout>

    </LinearLayout>

</ScrollView>
```

---

## 7. 症状分析算法

### 7.1 PMDD（经前烦躁障碍）风险评估

```kotlin
class PMSSymptomAnalyzer {
    
    /**
     * 评估 PMDD 风险
     * 基于 DSM-5 标准
     */
    fun assessPMDDRisk(symptoms: List<PMSSymptom>): PMDDRisk {
        if (symptoms.size < 2) return PMDDRisk.Low
        
        val recentSymptoms = symptoms.takeLast(3) // 最近3次记录
        
        // 检查至少一个情绪症状是否严重（>= 4）
        val hasSevereMoodSymptom = recentSymptoms.any { symptom ->
            (symptom.moodSwings ?: 0) >= 4 ||
            (symptom.irritability ?: 0) >= 4 ||
            (symptom.anxiety ?: 0) >= 4 ||
            (symptom.depression ?: 0) >= 4
        }
        
        // 检查至少5个症状是否中度以上（>= 3）
        val moderateSymptomCount = recentSymptoms.flatMap { symptom ->
            listOfNotNull(
                symptom.moodSwings,
                symptom.irritability,
                symptom.anxiety,
                symptom.depression,
                symptom.abdominalPain,
                symptom.breastTenderness,
                symptom.headache,
                symptom.fatigue,
                symptom.bloating
            )
        }.count { it >= 3 }
        
        return when {
            hasSevereMoodSymptom && moderateSymptomCount >= 5 -> PMDDRisk.High
            hasSevereMoodSymptom || moderateSymptomCount >= 3 -> PMDDRisk.Medium
            else -> PMDDRisk.Low
        }
    }
    
    /**
     * 识别症状模式
     */
    fun identifySymptomPatterns(symptoms: List<PMSSymptom>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // 检查周期性模式
        if (symptoms.size >= 3) {
            val moodSwings = symptoms.map { it.moodSwings ?: 0 }
            if (isCyclicalPattern(moodSwings)) {
                patterns.add(SymptomPattern.CyclicalMoodSwings)
            }
        }
        
        // 检查症状加重趋势
        if (isWorseningTrend(symptoms)) {
            patterns.add(SymptomPattern.WorseningSymptoms)
        }
        
        // 检查特定症状组合
        if (hasSymptomCluster(symptoms, listOf("moodSwings", "irritability", "anxiety"))) {
            patterns.add(SymptomPattern.EmotionalCluster)
        }
        
        return patterns
    }
    
    private fun isCyclicalPattern(values: List<Int>): Boolean {
        // 简化的周期性检测算法
        if (values.size < 4) return false
        
        val variance = calculateVariance(values)
        return variance > 2.0 // 有较大的波动
    }
    
    private fun isWorseningTrend(symptoms: List<PMSSymptom>): Boolean {
        // 检查症状是否随时间加重
        val recentMoods = symptoms.takeLast(4).mapNotNull { it.moodSwings }
        return recentMoods.zipWithNext().count { it.first < it.second } >= 2
    }
    
    private fun hasSymptomCluster(symptoms: List<PMSSymptom>, symptomNames: List<String>): Boolean {
        // 检查是否存在特定症状组合
        return symptoms.any { symptom ->
            symptomNames.count { name ->
                when (name) {
                    "moodSwings" -> (symptom.moodSwings ?: 0) >= 3
                    "irritability" -> (symptom.irritability ?: 0) >= 3
                    "anxiety" -> (symptom.anxiety ?: 0) >= 3
                    else -> false
                }
            } >= symptomNames.size
        }
    }
    
    private fun calculateVariance(values: List<Int>): Double {
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average()
    }
}

enum class PMDDRisk {
    Low,      // 低风险
    Medium,   // 中等风险
    High      // 高风险，建议就医
}

enum class SymptomPattern {
    CyclicalMoodSwings,    // 周期性情绪波动
    WorseningSymptoms,     // 症状加重趋势
    EmotionalCluster,      // 情绪症状聚集
    PhysicalCluster,       // 生理症状聚集
    MixedPattern           // 混合模式
}
```

### 7.2 个性化建议生成

```kotlin
class HealthAdviceGenerator {
    
    fun generateAdvice(symptom: PMSSymptom, riskLevel: PMDDRisk): List<String> {
        val advice = mutableListOf<String>()
        
        // 基于症状严重程度生成建议
        symptom.breastTenderness?.let { severity ->
            if (severity >= 3) {
                advice.add("乳房胀痛较明显，建议穿着舒适的内衣，避免摄入咖啡因")
            }
        }
        
        symptom.abdominalPain?.let { severity ->
            if (severity >= 3) {
                advice.add("腹痛症状明显，可以尝试热敷腹部或进行轻度运动")
                if (severity >= 4) {
                    advice.add("腹痛严重，建议咨询医生是否需要使用止痛药")
                }
            }
        }
        
        symptom.moodSwings?.let { severity ->
            if (severity >= 3) {
                advice.add("情绪波动较大，建议保持规律作息，避免压力")
                if (severity >= 4) {
                    advice.add("情绪波动严重，建议进行冥想或瑜伽等放松练习")
                }
            }
        }
        
        symptom.fatigue?.let { severity ->
            if (severity >= 3) {
                advice.add("疲劳感明显，建议保证充足睡眠（7-9小时）")
            }
        }
        
        symptom.bloating?.let { severity ->
            if (severity >= 3) {
                advice.add("腹胀明显，建议减少盐分摄入，多喝水")
            }
        }
        
        // 基于风险等级生成建议
        when (riskLevel) {
            PMDDRisk.High -> {
                advice.add("您的症状较为严重，建议及时就医咨询专业医生")
                advice.add("可以考虑记录症状日记，以便医生更好地了解您的情况")
            }
            PMDDRisk.Medium -> {
                advice.add("您的症状需要关注，建议定期记录并观察变化趋势")
                advice.add("可以尝试调整生活方式，如规律运动、健康饮食")
            }
            PMDDRisk.Low -> {
                advice.add("您的症状较轻，建议继续保持健康的生活方式")
            }
        }
        
        return advice
    }
}
```

---

## 8. 数据库迁移

### 8.1 更新数据库版本

```kotlin
@Database(
    entities = [
        MenstrualCycle::class,
        PMSSymptom::class  // 新增
    ],
    version = 2,  // 版本从1升级到2
    exportSchema = false
)
abstract class WomenHealthDatabase : RoomDatabase() {
    abstract fun menstrualCycleDao(): MenstrualCycleDao
    abstract fun pmsSymptomDao(): PMSSymptomDao  // 新增
    
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
                .addMigrations(MIGRATION_1_2)  // 添加迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 数据库迁移：从版本1到版本2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 PMS 症状表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `pms_symptoms` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `recordDate` INTEGER NOT NULL,
                `cycleId` INTEGER,
                `breastTenderness` INTEGER,
                `abdominalPain` INTEGER,
                `headache` INTEGER,
                `backache` INTEGER,
                `jointPain` INTEGER,
                `bloating` INTEGER,
                `constipation` INTEGER,
                `diarrhea` INTEGER,
                `nausea` INTEGER,
                `appetiteChange` INTEGER,
                `foodCravings` INTEGER,
                `weightGain` REAL,
                `skinChanges` INTEGER,
                `fatigue` INTEGER,
                `sleepDisturbance` INTEGER,
                `hotFlashes` INTEGER,
                `coldSweats` INTEGER,
                `moodSwings` INTEGER,
                `irritability` INTEGER,
                `anxiety` INTEGER,
                `depression` INTEGER,
                `sadness` INTEGER,
                `anger` INTEGER,
                `cryingSpells` INTEGER,
                `socialWithdrawal` INTEGER,
                `concentrationIssues` INTEGER,
                `memoryIssues` INTEGER,
                `lowLibido` INTEGER,
                `aggression` INTEGER,
                `restlessness` INTEGER,
                `decreasedEnergy` INTEGER,
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`cycleId`) REFERENCES `menstrual_cycles`(`id`) ON DELETE SET NULL
            )
        """.trimIndent())
        
        // 创建索引以提高查询性能
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_pms_symptoms_recordDate` 
            ON `pms_symptoms` (`recordDate`)
        """.trimIndent())
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_pms_symptoms_cycleId` 
            ON `pms_symptoms` (`cycleId`)
        """.trimIndent())
    }
}
```

---

## 9. 测试策略

### 9.1 单元测试

```kotlin
@RunWith(AndroidJUnit4::class)
class PMSSymptomDaoTest {
    
    private lateinit var database: WomenHealthDatabase
    private lateinit var dao: PMSSymptomDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WomenHealthDatabase::class.java).build()
        dao = database.pmsSymptomDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveSymptom() = runTest {
        val symptom = PMSSymptom(
            recordDate = System.currentTimeMillis(),
            breastTenderness = 3,
            abdominalPain = 4,
            moodSwings = 2
        )
        
        val id = dao.insertSymptom(symptom)
        val retrieved = dao.getSymptomById(id)
        
        assertNotNull(retrieved)
        assertEquals(3, retrieved?.breastTenderness)
        assertEquals(4, retrieved?.abdominalPain)
    }
    
    @Test
    fun getSymptomsByDateRange() = runTest {
        val today = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000
        
        val symptom1 = PMSSymptom(recordDate = today, moodSwings = 3)
        val symptom2 = PMSSymptom(recordDate = today + dayInMillis, moodSwings = 4)
        val symptom3 = PMSSymptom(recordDate = today + 2 * dayInMillis, moodSwings = 2)
        
        dao.insertSymptom(symptom1)
        dao.insertSymptom(symptom2)
        dao.insertSymptom(symptom3)
        
        val symptoms = dao.getSymptomsByDateRange(today, today + 2 * dayInMillis).first()
        
        assertEquals(3, symptoms.size)
    }
    
    @Test
    fun calculateAverageMoodSwings() = runTest {
        dao.insertSymptom(PMSSymptom(recordDate = System.currentTimeMillis(), moodSwings = 2))
        dao.insertSymptom(PMSSymptom(recordDate = System.currentTimeMillis(), moodSwings = 4))
        dao.insertSymptom(PMSSymptom(recordDate = System.currentTimeMillis(), moodSwings = 3))
        
        val average = dao.getAverageMoodSwings()
        
        assertEquals(3.0, average)
    }
}
```

### 9.2 ViewModel 测试

```kotlin
@ExperimentalCoroutinesApi
class PMSSymptomViewModelTest {
    
    private lateinit var viewModel: PMSSymptomViewModel
    private lateinit var repository: PMSSymptomRepository
    
    @MockK
    private lateinit var mockDao: PMSSymptomDao
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = PMSSymptomRepository(mockDao)
        // 初始化 ViewModel
    }
    
    @Test
    fun `saveSymptom should insert new symptom`() = runTest {
        val symptom = PMSSymptom(recordDate = System.currentTimeMillis(), moodSwings = 3)
        
        coEvery { mockDao.insertSymptom(any()) } returns 1L
        
        viewModel.saveSymptom(symptom)
        
        coVerify { mockDao.insertSymptom(any()) }
    }
    
    @Test
    fun `selectDate should load symptom for that date`() = runTest {
        val date = System.currentTimeMillis()
        val symptom = PMSSymptom(recordDate = date, moodSwings = 3)
        
        coEvery { mockDao.getSymptomByDate(date) } returns symptom
        
        viewModel.selectDate(date)
        
        assertEquals(symptom, viewModel.currentSymptom.value)
    }
}
```

---

## 10. 性能优化建议

### 10.1 数据库优化
- 为常用查询字段添加索引（`recordDate`, `cycleId`）
- 使用 `@Transaction` 进行批量操作
- 考虑使用 Room 的 `@Relation` 进行关联查询

### 10.2 UI 优化
- 使用 RecyclerView 的 DiffUtil 进行高效列表更新
- 实现懒加载和分页
- 使用协程进行异步操作

### 10.3 内存优化
- 避免在内存中保存大量症状数据
- 使用 Flow 进行数据流处理
- 及时释放不必要的资源

---

## 11. 实现步骤

### 阶段 1：数据层开发
1. 创建 `PMSSymptom` 实体类
2. 创建 `PMSSymptomDao` 接口
3. 创建 `PMSSymptomRepository` 类
4. 更新数据库配置和迁移脚本

### 阶段 2：业务逻辑层开发
1. 创建 `PMSSymptomViewModel` 类
2. 实现症状分析器 `PMSSymptomAnalyzer`
3. 实现健康建议生成器 `HealthAdviceGenerator`

### 阶段 3：UI 层开发
1. 创建 `PMSSymptomsFragment`
2. 创建症状列表适配器 `PMSSymptomsAdapter`
3. 创建症状记录对话框 `PMSSymptomDialog`
4. 设计并实现布局文件

### 阶段 4：集成与优化
1. 集成到底部导航
2. 实现日历视图与症状关联
3. 添加图表可视化（症状趋势）
4. 性能优化和测试

---

## 12. 注意事项

1. **数据隐私**：PMS 症状数据属于敏感个人信息，确保使用 SQLCipher 进行加密存储
2. **医疗免责**：应用提供的建议仅供参考，不应替代专业医疗诊断
3. **用户体验**：症状记录界面应简洁易用，避免过多选项导致用户疲劳
4. **数据备份**：支持用户导出和备份症状数据
5. **多语言支持**：症状描述和建议需要支持中英文切换

---

## 13. 参考资料

- [DSM-5: Premenstrual Dysphoric Disorder](https://www.psychiatry.org/patients-families/pmdd/what-is-pmdd)
- [Mayo Clinic: Premenstrual Syndrome (PMS)](https://www.mayoclinic.org/diseases-conditions/premenstrual-syndrome/symptoms-causes/syc-20376780)
- [Room Database Documentation](https://developer.android.com/training/data-storage/room)
- [Kotlin Flow Documentation](https://kotlinlang.org/docs/flow.html)

---

**文档版本**: 1.0  
**创建日期**: 2026-02-14  
**维护者**: 开发团队