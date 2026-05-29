package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.ui.viewmodel.AiState
import top.mizhoubaobei.womenhealth.ui.viewmodel.ChatMessage

@Composable
fun AiAnalysisCard(
    aiState: AiState,
    chatMessages: List<ChatMessage>,
    onGenerateReport: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTabState by remember { mutableStateOf(0) } // 0 = Report, 1 = Chat QA
    var customQuestionInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val recommendedQuestions = listOf(
        "痛经该怎么缓解？💧",
        "经期可以运动或洗头吗？‍♀",
        "月经量多少才算正常？🩸",
        "情绪暴躁，如何调理PMS？"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_analysis_main_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Widget Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI 智能女性健康管理分析",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-navigation Switch Tab Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(4.dp)
            ) {
                // Tab Button 1: Health Diagnostic Report
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (activeTabState == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .testTag("report_tab_btn")
                        .clickable { activeTabState = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "智能周期特征报告",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTabState == 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Tab Button 2: Interactive Q/A Assistant
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (activeTabState == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .testTag("chat_tab_btn")
                        .clickable { activeTabState = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI 在线贴心咨询",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTabState == 1) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab View Swapping content
            AnimatedContent(
                targetState = activeTabState,
                label = "tabAnim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // TAB 1: Auto-Generated Report Screen
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (aiState) {
                                is AiState.Idle -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "📖 尚未生成本周期专属健康分析图文",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "AI 将一键聚合您最近几次的周期长度、月事经量与合并症状，帮您测算子宫暖调、科学饮食与经期调理指南哦！",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = onGenerateReport,
                                                modifier = Modifier.testTag("generate_report_btn")
                                            ) {
                                                Text("✨ 一键生成报告 (Gemini 加持)")
                                            }
                                        }
                                    }
                                }
                                is AiState.Loading -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 3.dp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "✨ Gemini 3.5-Flash 正在深度调阅分析...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "科学统筹暖腹指导中，大约需要几秒钟...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                is AiState.Success -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Report Display block
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = aiState.response,
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Re-generate button
                                        OutlinedButton(
                                            onClick = onGenerateReport,
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .testTag("regenerate_report_btn")
                                        ) {
                                            Text("重新更新检测 ✨", fontSize = 12.sp)
                                        }
                                    }
                                }
                                is AiState.Error -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = aiState.error,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = onGenerateReport) {
                                            Text("重新尝试连网")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 2: AI Q/A Chatbot Area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                        ) {
                            // Clear chat row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = onClearChat,
                                    modifier = Modifier.testTag("clear_chat_btn")
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("清空对话", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Conversation Area (List of message balloons)
                            val listState = rememberLazyListState()
                            LaunchedEffect(chatMessages.size) {
                                if (chatMessages.isNotEmpty()) {
                                    listState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { message ->
                                    val bubbleBg = if (message.isUser) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                    
                                    val bubbleText = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .widthIn(max = 280.dp)
                                                .background(
                                                    color = bubbleBg,
                                                    shape = RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                                                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                                                    )
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = message.text,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                color = bubbleText
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Recommended Fast Tap prompt chips
                            Text(
                                text = "💬 推荐提问卡片 (点击一键速问)：",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            recommendedQuestions.forEach { promptText ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                                        .clickable {
                                                            onSendMessage(promptText.dropLast(1)) // drop emoji
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = promptText,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive input row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customQuestionInput,
                                    onValueChange = { customQuestionInput = it },
                                    placeholder = { Text("输入您的痛经疑惑或询问调理方案...", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_input_text_field"),
                                    shape = RoundedCornerShape(20.dp),
                                    maxLines = 2,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (customQuestionInput.isNotBlank()) {
                                                onSendMessage(customQuestionInput)
                                                customQuestionInput = ""
                                                keyboardController?.hide()
                                            }
                                        }
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                FloatingActionButton(
                                    onClick = {
                                        if (customQuestionInput.isNotBlank()) {
                                            onSendMessage(customQuestionInput)
                                            customQuestionInput = ""
                                            keyboardController?.hide()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("chat_send_button"),
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "发送",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mandatory AI Security Warning Card (Strict regulation constraint compliance)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "健康提示与安全警告：此健康助手和 AI 特征报告由人工智能语言模型生成，旨在提供舒适暖宫与保健指导作为原型演示，不能作为临床医学诊断、用药或重大手术的判断依据。如有严重或剧烈疼痛，请务必咨询专业妇科医院医师。原型内置演示 API Key 受到本安全客户端限制保护，请勿通过公共渠道扩散以符合审计安全规范。",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}
