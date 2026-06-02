package top.mizhoubaobei.womenhealth.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.mizhoubaobei.womenhealth.ui.viewmodel.BbtWeightPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun BbtWeightTrackerCard(
    history: List<BbtWeightPoint>,
    todayBbt: Float,
    todayWeight: Float,
    onUpdateBbt: (Float) -> Unit,
    onUpdateWeight: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("BBT") } // "BBT" or "WEIGHT"
    var tempInput by remember { mutableStateOf(if (todayBbt > 0) todayBbt else 36.5f) }
    var weightInput by remember { mutableStateOf(if (todayWeight > 0) todayWeight else 52.0f) }

    // Sync state when today's values update
    LaunchedEffect(todayBbt) {
        if (todayBbt > 0) {
            tempInput = todayBbt
        }
    }
    LaunchedEffect(todayWeight) {
        if (todayWeight > 0) {
            weightInput = todayWeight
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bbt_weight_tracker_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Banner
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
                        Text("🌡️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "体征规律追踪",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "掌握每日基础体温与体重的细微起伏",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(2.dp)
            ) {
                val bbtBg = if (selectedTab == "BBT") MaterialTheme.colorScheme.primary else Color.Transparent
                val bbtText = if (selectedTab == "BBT") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(17.dp))
                        .background(bbtBg)
                        .clickable { selectedTab = "BBT" }
                        .testTag("tracker_tab_bbt"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("基础体温 (BBT) 🌡️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = bbtText)
                }

                val weightBg = if (selectedTab == "WEIGHT") MaterialTheme.colorScheme.primary else Color.Transparent
                val weightText = if (selectedTab == "WEIGHT") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(17.dp))
                        .background(weightBg)
                        .clickable { selectedTab = "WEIGHT" }
                        .testTag("tracker_tab_weight"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("身体重量 (Weight) ⚖️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = weightText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == "BBT") {
                // BBT Content - Interactive Custom Slider with increase/decrease buttons
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "今日体温",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.2f", tempInput),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " °C",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step Adjuster Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { tempInput = (tempInput - 0.05f).coerceAtLeast(35.5f) },
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-0.05", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Slider(
                            value = tempInput,
                            onValueChange = { tempInput = (it * 100).roundToInt() / 100f },
                            valueRange = 35.8f..37.5f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )

                        OutlinedButton(
                            onClick = { tempInput = (tempInput + 0.05f).coerceAtMost(37.5f) },
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+0.05", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onUpdateBbt(tempInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("save_bbt_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存今日体温记录", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "📈 7天基础体温曲线走势",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // BASAL BODY TEMPERATURE GRAPH USING CUSTOM CANVAS
                    BbtLineChart(history = history)

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F2F1).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                tint = Color(0xFF00796B),
                                modifier = Modifier.size(16.dp).padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💡 小贴士：女性排卵日前后由于激素影响，基础体温会呈现“双相分布”。排卵日往往是低温谷底，之后会迅速上升0.3℃~0.5℃保持高体温，避孕和备孕都可以参考温度变化哦！",
                                fontSize = 10.5.sp,
                                color = Color(0xFF004D40),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            } else {
                // WEIGHT Content - Interactive Custom Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "今日重量",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.1f", weightInput),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = " kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { weightInput = (weightInput - 0.2f).coerceAtLeast(30f) },
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-0.2", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Slider(
                            value = weightInput,
                            onValueChange = { weightInput = (it * 10).roundToInt() / 10f },
                            valueRange = 40f..90f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        )

                        OutlinedButton(
                            onClick = { weightInput = (weightInput + 0.2f).coerceAtMost(120f) },
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+0.2", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onUpdateWeight(weightInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("save_weight_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存今日体重记录", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "📈 7天体重波动走势",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // WEIGHT GRAPH USING CUSTOM CANVAS
                    WeightLineChart(history = history)

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp).padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💡 小贴士：女性由于激素水平的反复波动，在黄体期及行经期第1-2天因为“水钠潴留”导致体内水分储存，体重通常会短时上涨1-2.5kg级，这完全是健康的生理储水，不要焦虑哦！经期结束后会自动回落。",
                                fontSize = 10.5.sp,
                                color = Color(0xFFD84315),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BbtLineChart(
    history: List<BbtWeightPoint>
) {
    val textPaintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val lineColor = MaterialTheme.colorScheme.primary
    val fillGradientColors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), MaterialTheme.colorScheme.primary.copy(alpha = 0.0f))

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalDays = history.size

        if (totalDays < 2) return@Canvas

        val validPoints = history.filter { it.bbt > 0f }
        val minTemp = if (validPoints.isNotEmpty()) validPoints.minOf { it.bbt } - 0.1f else 35.8f
        val maxTemp = if (validPoints.isNotEmpty()) validPoints.maxOf { it.bbt } + 0.1f else 37.5f
        val range = maxTemp - minTemp

        val path = Path()
        val fillPath = Path()
        var lastPointX = 0f
        var lastPointY = 0f

        val stepX = width / (totalDays - 1)

        history.forEachIndexed { index, point ->
            val x = index * stepX
            val bbtVal = if (point.bbt > 0f) point.bbt else 36.5f // fallback representation
            val y = height - ((bbtVal - minTemp) / range) * height

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                // Draw bezier or standard line
                val previousX = (index - 1) * stepX
                val previousBbt = if (history[index - 1].bbt > 0f) history[index - 1].bbt else 36.5f
                val previousY = height - ((previousBbt - minTemp) / range) * height
                
                // Draw bezier control points
                val controlX1 = previousX + (x - previousX) / 2
                val controlY1 = previousY
                val controlX2 = previousX + (x - previousX) / 2
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            if (index == totalDays - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // 1. Draw solid threshold dashed division helper line at 36.6°C
        val thresholdY = height - ((36.6f - minTemp) / range) * height
        if (thresholdY in 0f..height) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, thresholdY),
                end = Offset(width, thresholdY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 2. Draw standard fill undercurve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(fillGradientColors)
        )

        // 3. Draw curve line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // 4. Draw node dots
        history.forEachIndexed { index, point ->
            val x = index * stepX
            val bbtVal = if (point.bbt > 0f) point.bbt else 36.5f
            val y = height - ((bbtVal - minTemp) / range) * height

            // Outer white ring, inner primary core dot
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Draw temperature texts inside graph bounds
            if (point.bbt > 0f && (index % 2 == 0 || index == totalDays - 1)) {
                // Simple label represent
            }
        }
    }
}

@Composable
fun WeightLineChart(
    history: List<BbtWeightPoint>
) {
    val textPaintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val lineColor = MaterialTheme.colorScheme.secondary
    val fillGradientColors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.0f))

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalDays = history.size

        if (totalDays < 2) return@Canvas

        val validPoints = history.filter { it.weight > 0f }
        val minW = if (validPoints.isNotEmpty()) validPoints.minOf { it.weight } - 0.5f else 48f
        val maxW = if (validPoints.isNotEmpty()) validPoints.maxOf { it.weight } + 0.5f else 55f
        val range = maxW - minW

        val path = Path()
        val fillPath = Path()
        val stepX = width / (totalDays - 1)

        history.forEachIndexed { index, point ->
            val x = index * stepX
            val weightVal = if (point.weight > 0f) point.weight else 52f
            val y = height - ((weightVal - minW) / range) * height

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val previousX = (index - 1) * stepX
                val previousWeight = if (history[index - 1].weight > 0f) history[index - 1].weight else 52f
                val previousY = height - ((previousWeight - minW) / range) * height
                
                val controlX1 = previousX + (x - previousX) / 2
                val controlY1 = previousY
                val controlX2 = previousX + (x - previousX) / 2
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            if (index == totalDays - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw standard fill undercurve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(fillGradientColors)
        )

        // Draw curve line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw node dots
        history.forEachIndexed { index, point ->
            val x = index * stepX
            val weightVal = if (point.weight > 0f) point.weight else 52f
            val y = height - ((weightVal - minW) / range) * height

            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
