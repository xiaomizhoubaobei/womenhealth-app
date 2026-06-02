package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecordSheet(
    onDismissRequest: () -> Unit,
    onSaveRecord: (startDate: String, endDate: String?, flow: String, symptoms: List<String>, moods: List<String>, notes: String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    var startDate by remember { mutableStateOf(today.format(formatter)) }
    var hasEnded by remember { mutableStateOf(true) }
    var endDate by remember { mutableStateOf(today.plusDays(4).format(formatter)) }

    var selectedFlow by remember { mutableStateOf("正常") }

    val flowsList = listOf("极少", "较少", "正常", "较多")

    val symptomsList = listOf("痛经", "头痛", "腹胀", "乳房胀痛", "粉刺", "无症状")
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    val moodsList = listOf("平静", "敏感", "郁闷", "开朗", "疲惫", "焦虑")
    val selectedMoods = remember { mutableStateListOf<String>() }

    var noteText by remember { mutableStateOf("") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun isValidDateStr(dateStr: String): Boolean {
        return try {
            LocalDate.parse(dateStr, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("add_record_sheet_dialog")
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // HeaderRow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🩸 登记生理周期记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_sheet_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable details form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Date Interval Configuration
                    Text(
                        text = "📅 周期时段设定",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("开始日期 (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_date_input"),
                            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )

                        if (hasEnded) {
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("结束日期 (YYYY-MM-DD)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("end_date_input"),
                                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                            )
                        }
                    }

                    // On-going toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = hasEnded,
                            onCheckedChange = { hasEnded = it },
                            modifier = Modifier.testTag("has_ended_checkbox")
                        )
                        Text(
                            text = "生理期已结束 (未勾选表示经期仍在进行中)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Flow Intensity
                    Text(
                        text = "🩸 经量大小及特征",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flowsList.forEach { flow ->
                            val isSelected = selectedFlow == flow
                            val btnBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            val btnText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(btnBg)
                                    .testTag("flow_btn_$flow")
                                    .clickable { selectedFlow = flow },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = flow,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = btnText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Symptoms (Multi-select)
                    Text(
                        text = "🌡️ 伴随症状 (多选)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptomsList.forEach { symptom ->
                            val isSelected = selectedSymptoms.contains(symptom)
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(chipBg)
                                    .testTag("symptom_btn_$symptom")
                                    .clickable {
                                        if (symptom == "无症状") {
                                            selectedSymptoms.clear()
                                            selectedSymptoms.add("无症状")
                                        } else {
                                            selectedSymptoms.remove("无症状")
                                            if (isSelected) {
                                                selectedSymptoms.remove(symptom)
                                            } else {
                                                selectedSymptoms.add(symptom)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = symptom,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = chipText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Mood Profile
                    Text(
                        text = "💖 情绪波动指数 (多选)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodsList.forEach { mood ->
                            val isSelected = selectedMoods.contains(mood)
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(chipBg)
                                    .testTag("mood_btn_$mood")
                                    .clickable {
                                        if (isSelected) {
                                            selectedMoods.remove(mood)
                                        } else {
                                            selectedMoods.add(mood)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mood,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = chipText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Notes text field
                    Text(
                        text = "📝 私房备忘录",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("记录今生这一天的感受、经期痛感或红糖水疗效...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("notes_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("record_validation_error_text")
                    )
                }

                // Bottom Action Button Row
                Button(
                    onClick = {
                        errorMessage = null
                        if (startDate.isBlank() || !isValidDateStr(startDate)) {
                            errorMessage = "⚠️ 开始日期格式不正确，请输入 YYYY-MM-DD (如 ${today.format(formatter)})"
                            return@Button
                        }
                        var finalEndDate: String? = null
                        if (hasEnded) {
                            if (endDate.isBlank() || !isValidDateStr(endDate)) {
                                errorMessage = "⚠️ 结束日期格式不正确，请输入 YYYY-MM-DD (如 ${today.plusDays(4).format(formatter)})"
                                return@Button
                            }
                            try {
                                val start = LocalDate.parse(startDate, formatter)
                                val end = LocalDate.parse(endDate, formatter)
                                if (end.isBefore(start)) {
                                    errorMessage = "⚠️ 结束日期不能早于开始开始日期哦！"
                                    return@Button
                                }
                                finalEndDate = endDate
                            } catch (e: Exception) {
                                errorMessage = "⚠️ 日期解析错误，请检查输入格式"
                                return@Button
                            }
                        }

                        val symps = if (selectedSymptoms.isEmpty()) listOf("无症状") else selectedSymptoms.toList()
                        val mds = if (selectedMoods.isEmpty()) listOf("平静") else selectedMoods.toList()
                        onSaveRecord(
                            startDate,
                            finalEndDate,
                            selectedFlow,
                            symps,
                            mds,
                            noteText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_record_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("保存此生理卡片", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
