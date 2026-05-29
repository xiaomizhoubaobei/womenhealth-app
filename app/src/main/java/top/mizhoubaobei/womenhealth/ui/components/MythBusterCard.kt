package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MythBusterCard(
    modifier: Modifier = Modifier
) {
    val myths = remember {
        listOf(
            MythData(
                id = 1,
                mythText = "经期吃巧克力的确能缓解痛经？",
                factHeader = "【复合心理安慰效应】🍫",
                factText = "巧克力中含有些许咖啡因和高糖。高浓度的单糖容易引起体内血糖剧烈震荡，甚至加重血管痉挛导致前列腺素水平升高，激化痛经困扰。真正的暖宫和科学之选是温姜糖水、红枣茶，而非高糖巧克力哦！",
                icon = "🍫",
                colorScheme = Color(0xFFFFCC80)
            ),
            MythData(
                id = 2,
                mythText = "经期洗头老了会痛头、得子宫肌瘤？",
                factHeader = "【完全没有医学依据】🚿",
                factText = "老一辈卫生保暖条件差、保暖不足导致的经验留传。现代生活使用温暖的热水洗头，洗完及时使用毛巾和电吹风的暖风彻底吹干头发、防止头部受凉入风，完全可以确保经期头部洁净卫生，更能愉悦经期心情。",
                icon = "🚿",
                colorScheme = Color(0xFF90CAF9)
            ),
            MythData(
                id = 3,
                mythText = "经期绝对不能做任何形式的运动？",
                factHeader = "【轻量拉伸更有助于痛经放松】🧘",
                factText = "倒立、高强度耐力无氧当然要规避。但适度的轻量健走、骨盆倾斜、猫猫拉伸等，能极大地改善骨盆和腹部的局部血液微循环，使括约肌和子宫肌肉松弛下来，反倒能非常显著地缓解痛经和消解郁闷！",
                icon = "🧘",
                colorScheme = Color(0xFFA5D6A7)
            ),
            MythData(
                id = 4,
                mythText = "痛经吃布洛芬等止痛片会上瘾或伤身？",
                factHeader = "【科学合规用药绝不上瘾】💊",
                factText = "布洛芬等为非甾体抗炎药，作用于外周神经抑制前列腺素合成，非吗啡类中枢性止痛药，完全无成瘾性！在痛经刚现头、流量高前一两天，适量按说明口服或随餐吃，能极其科学、有效避痛，并无任何身心依赖隐患。",
                icon = "💊",
                colorScheme = Color(0xFFF48FB1)
            )
        )
    }

    var flippedStates by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("myth_buster_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Title Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEDE7F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Color(0xFF5E35B1),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "女性经期谣言粉碎机 🔬",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "点击卡片反转揭秘科学，关爱身体，拒绝焦虑",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel Carousel Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(myths) { index, item ->
                    val isFlipped = flippedStates[item.id] ?: false
                    
                    // Rotation transition state
                    val rotationY by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(durationMillis = 500)
                    )

                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(210.dp)
                            .graphicsLayer {
                                this.rotationY = rotationY
                                cameraDistance = 14f * density
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isFlipped) Color(0xFFFAFAFA) else item.colorScheme.copy(alpha = 0.15f)
                            )
                            .border(
                                width = 1.2.dp,
                                color = if (isFlipped) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else item.colorScheme.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                flippedStates = flippedStates.toMutableMap().apply {
                                    put(item.id, !isFlipped)
                                }
                            }
                            .padding(14.dp)
                    ) {
                        if (rotationY <= 90f) {
                            // Present FRONT Side block (MYTH representation)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                            .size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.icon, fontSize = 16.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "谣言疑惑?",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }

                                Text(
                                    text = item.mythText,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFD84315),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "轻触反转看真相",
                                        fontSize = 10.sp,
                                        color = Color(0xFFD84315),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Present BACK Side block (FACT representation)
                            // Rotate back elements so they aren't flipped mirror image
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        this.rotationY = 180f
                                    },
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.factHeader,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7D32)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "科学实证",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Text(
                                    text = item.factText,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 6.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "科学建议 / 复转",
                                        fontSize = 9.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MythData(
    val id: Int,
    val mythText: String,
    val factHeader: String,
    val factText: String,
    val icon: String,
    val colorScheme: Color
)
