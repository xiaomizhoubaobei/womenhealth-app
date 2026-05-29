package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HealingGuideCard(
    herbCompleted: Boolean,
    footBathCompleted: Boolean,
    massageCompleted: Boolean,
    onToggleHerb: () -> Unit,
    onToggleFootBath: () -> Unit,
    onToggleMassage: () -> Unit,
    currentPhase: String,
    modifier: Modifier = Modifier
) {
    // Dynamic advice according to the phase
    val dietName = when (currentPhase) {
        "月经期" -> "暖宫红糖老姜茶 🍵"
        "黄体期" -> "枸杞桂圆养气汤 🍯"
        else -> "温和滋补玫瑰茶 🌹"
    }

    val dietTip = when (currentPhase) {
        "月经期" -> "红茶老红糖与生姜同煮，沸水泡开，散寒止痛。"
        "黄体期" -> "桂圆、红枣与枸杞文火煲煮，能有效调节经前焦虑。"
        else -> "玫瑰花放温水泡制，理气解郁，调节经期前奏。"
    }

    var expandedHerb by remember { mutableStateOf(false) }
    var expandedFootBath by remember { mutableStateOf(false) }
    var expandedMassage by remember { mutableStateOf(false) }

    val completedCount = (if (herbCompleted) 1 else 0) + (if (footBathCompleted) 1 else 0) + (if (massageCompleted) 1 else 0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("healing_guide_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Title Header Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFFF1F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌸", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "温宫调养与自护方案",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "今日健康自查达成：$completedCount / 3 项",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Small circular progressive progress indicator
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                    CircularProgressIndicator(
                        progress = completedCount.toFloat() / 3f,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        strokeWidth = 3.5.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${(completedCount * 100) / 3}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Guidance Items List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // 1. Herbal Diet Tea
                HealingItemRow(
                    title = "温热食疗：$dietName",
                    isCompleted = herbCompleted,
                    onToggle = onToggleHerb,
                    isExpanded = expandedHerb,
                    onToggleExpand = { expandedHerb = !expandedHerb },
                    expandContent = {
                        Text(
                            text = "【制作秘方】：$dietTip\n【温补功效】：中医学讲究‘痛则不通，通则不痛’。在行经前后温饮此茶，可祛散子宫寒气，促进淤血顺利排出，温和滋养气血。",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 6.dp, start = 12.dp, end = 12.dp, bottom = 4.dp)
                        )
                    }
                )

                // 2. Foot Bath
                HealingItemRow(
                    title = "暖足理气：艾叶草温水泡足 🦶",
                    isCompleted = footBathCompleted,
                    onToggle = onToggleFootBath,
                    isExpanded = expandedFootBath,
                    onToggleExpand = { expandedFootBath = !expandedFootBath },
                    expandContent = {
                        Text(
                            text = "【泡脚指南】：使用40℃左右温水，可放入干艾叶1小包，浸泡15-20分钟至额头微微出汗。切记不要在大汗淋漓或空腹时进行。\n【驱寒机理】：热力从足底涌泉学升腾，疏通下焦经络，引火下行，有效缓和下肢冰冷及痛经不适。",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 6.dp, start = 12.dp, end = 12.dp, bottom = 4.dp)
                        )
                    }
                )

                // 3. Acupoint Massage
                HealingItemRow(
                    title = "穴位理疗：三阴交穴黄金推拿 💆",
                    isCompleted = massageCompleted,
                    onToggle = onToggleMassage,
                    isExpanded = expandedMassage,
                    onToggleExpand = { expandedMassage = !expandedMassage },
                    expandContent = {
                        Text(
                            text = "【取穴技巧】：小腿内侧，足内踝尖直上4横指（3寸），胫骨内侧缘后方。\n【推拿方法】：用拇指顺时针点按揉动2-3分钟，力度至酸胀感为宜，两侧交替。常以此法点按，能健脾益气、调理冲任，迅速宣通淤滞、缓解行经腹痛。",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 6.dp, start = 12.dp, end = 12.dp, bottom = 4.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun HealingItemRow(
    title: String,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
    val checkColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Interactive Checkbutton
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已完成标记",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.None else TextDecoration.None
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "收起详情" else "展开详情",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(bottom = 10.dp)
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                expandContent()
            }
        }
    }
}
