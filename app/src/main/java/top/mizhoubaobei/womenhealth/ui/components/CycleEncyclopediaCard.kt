package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CycleEncyclopediaCard(
    modifier: Modifier = Modifier
) {
    var activePhaseIndex by remember { mutableStateOf(0) } // 0:月经期, 1:卵泡期, 2:排卵期, 3:黄体期

    val phases = listOf(
        PhaseInfo(
            title = "月经期 (行经期)",
            icon = "🩸",
            days = "第 1~5 天",
            hormoneEstrogen = 0.15f,
            hormoneProgesterone = 0.08f,
            hormoneLh = 0.1f,
            skinCondition = "干燥缺水、敏感、黯淡。行经期前两天容易爆痘痘，油脂分泌旺。建议强效温和补水，绝不使用刺激性功能护肤品。",
            dietAdvice = "喜温热温补！多吃铁质丰富的食物。推荐红肉、菠菜、黑木耳、红豆、生姜红糖茶。忌生冷、忌辛辣和咖啡因。",
            sportAdvice = "以静止、温和拉伸、散步或舒缓排毒瑜伽为主。禁止进行任何高抗阻、高负荷及长时间腹肌训练，防止淤血倒流。",
            goldenQuote = "“对身体温柔，原谅一切疲劳。给自己灌上一杯暖水，安然舒眠。”"
        ),
        PhaseInfo(
            title = "卵泡期 (丰满期)",
            icon = "🌱",
            days = "第 6~12 天",
            hormoneEstrogen = 0.75f,
            hormoneProgesterone = 0.12f,
            hormoneLh = 0.3f,
            skinCondition = "黄金透亮期！雌激素分泌增多、水分充足、新陈代谢旺盛，容光焕发。正是尝试高浓度活性护肤与去角质的最好时机。",
            dietAdvice = "消化良好。多摄入优质高蛋白、膳食纤维和抗氧化水果（如蓝莓、羽衣甘蓝、豆浆），帮助雌激素稳定并利于优质卵泡成熟。",
            sportAdvice = "精力充沛。可以尝试中高强度运动，包括力量训练、有氧慢跑、高爆HIIT等。此时肌肉恢复速度极快，减脂塑形效率倍增！",
            goldenQuote = "“能量爆发期！去迈开步子拥抱生活吧，此刻的你神采飞扬。”"
        ),
        PhaseInfo(
            title = "排卵期 (繁育期)",
            icon = "🌸",
            days = "第 13~16 天",
            hormoneEstrogen = 0.95f,
            hormoneProgesterone = 0.28f,
            hormoneLh = 0.95f, // LH surge!
            skinCondition = "皮肤弹润光滑，但由于黄体生成素（LH）瞬间飙升，皮脂腺会变得有点活跃。注重出油控油防尘，温和洁面即可。",
            dietAdvice = "备孕或避孕敏感期。可以多补充深海鱼肉（富含Ω-3）、五谷杂粮、叶酸。继续坚持每天喝足量温开水，加速基础排泄更新。",
            sportAdvice = "雌激素极高！爆发力达到峰值。非常建议进行燃脂力量训练、游泳、和中长跑。你会发现注意力非常集中，表现突出。",
            goldenQuote = "“生命力与魅力的巅峰时刻。好心情、好状态，让体能尽情律动。”"
        ),
        PhaseInfo(
            title = "黄体期 (调护期)",
            icon = "🍁",
            days = "第 17~28 天",
            hormoneEstrogen = 0.5f,
            hormoneProgesterone = 0.9f, // Progesterone peak!
            hormoneLh = 0.15f,
            skinCondition = "PMS预警。由于孕激素暴增，毛孔粗大、油脂排泄不畅、角质层变厚。极易长硬核闭口痘。做好面部温和控油和深层清洁。",
            dietAdvice = "易水肿和对碳水极度渴望。多吃排水肿利湿食物，比如冬瓜、红豆、薏豆。补充足够的维生素E和镁，能有效缓解乳房胀痛与暴躁情绪。",
            sportAdvice = "身体发热，容易疲劳心慌。多做中低等强度的慢跑、快走或舒缓肌肉的弹力带训练。不要过度透支体能，注重静心放松。",
            goldenQuote = "“生理期的前奏。感受情绪的波澜起伏，并学着温柔、接纳并呵护它们。”"
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cycle_encyclopedia_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFCE4EC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "生理周期四阶段小百科",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "基于体内激素节律的科学养护指导",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selection Segment Row (4 stages)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                phases.forEachIndexed { index, phase ->
                    val isActive = activePhaseIndex == index
                    val bgBrush = if (isActive) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    } else {
                        null
                    }
                    val textColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .then(if (bgBrush != null) Modifier.background(bgBrush) else Modifier)
                            .clickable { activePhaseIndex = index }
                            .testTag("encyclopedia_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${phase.icon} ${phase.title.substringBefore(" ")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = phase.days,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Panel Content
            val current = phases[activePhaseIndex]
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // Phase title banner inside content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${current.icon} ${current.title} (${current.days})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. HEALTH INTERACTIVE HORMONE SLIDER METER
                Text(
                    text = "🧬 今日相对激素波动水平",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    HormoneBar(label = "雌激素 (Estrogen)", ratio = current.hormoneEstrogen, color = Color(0xFFF06292))
                    HormoneBar(label = "孕激素 (Progesterone)", ratio = current.hormoneProgesterone, color = Color(0xFFFFB74D))
                    HormoneBar(label = "黄体生成素 (LH)", ratio = current.hormoneLh, color = Color(0xFF4DB6AC))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Grid-like Details (Skin, Diet, Sport, Golden Quote)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdviceItemRow(title = "🧖 肤质状态与保修", content = current.skinCondition)
                    AdviceItemRow(title = "🍎 黄金养护膳食推荐", content = current.dietAdvice)
                    AdviceItemRow(title = "🏃 行动与阻抗运动推荐", content = current.sportAdvice)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Beautiful custom bottom quotation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = current.goldenQuote,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HormoneBar(
    label: String,
    ratio: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = "${(ratio * 100).roundToInt()}%", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun AdviceItemRow(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = 15.sp
        )
    }
}

private data class PhaseInfo(
    val title: String,
    val icon: String,
    val days: String,
    val hormoneEstrogen: Float,
    val hormoneProgesterone: Float,
    val hormoneLh: Float,
    val skinCondition: String,
    val dietAdvice: String,
    val sportAdvice: String,
    val goldenQuote: String
)
