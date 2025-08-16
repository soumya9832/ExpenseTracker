package com.soumyadip.expensetracker.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soumyadip.expensetracker.R
import com.soumyadip.expensetracker.viewmodel.ExpenseReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExpenseReportScreen(
    expenseReportViewModel: ExpenseReportViewModel = viewModel()
){

    val sevenDayTotal by expenseReportViewModel.sevenDayTotal.collectAsState()
    val dailyAverage by expenseReportViewModel.dailyAverage.collectAsState()
    val highestSpendingDay by expenseReportViewModel.highestSpendingDay.collectAsState()
    val topCategory by expenseReportViewModel.topCategory.collectAsState()
    val categorySpendingAndCount by expenseReportViewModel.categorySpendingAndCount.collectAsState()
    val dailySpendingData by expenseReportViewModel.dailySpendingData.collectAsState()
    val sevenDaysDates by expenseReportViewModel.sevenDaysDates.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Section 1: Summary Cards Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryCard("7-Day Total", "₹${"%.0f".format(sevenDayTotal)}", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            SummaryCard("Daily Average", "₹${"%.0f".format(dailyAverage)}", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Quick Insights
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row (modifier = Modifier
                    .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_trending_up_24),
                        contentDescription = null,
                        tint = Color(0xFF118AB5),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Quick Insights",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Highest Spending Day
                InsightCard(
                    title = "Highest Spending Day",
                    subtitle = highestSpendingDay?.let {
                        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it.date))
                    } ?: "N/A",
                    amount = highestSpendingDay?.amount ?: 0.0,
                    iconId = R.drawable.outline_calendar_today_24,
                    tintColor = Color(0xFF208ACA)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Top Category
                InsightCard(
                    title = "Top Category",
                    subtitle = topCategory ?: "N/A",
                    amount = categorySpendingAndCount[topCategory]?.first ?: 0.0,
                    iconId = R.drawable.outline_trending_down_24,
                    tintColor = Color(0xFFF42A25)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Charts
        ChartCard(title = "Daily Spending (Last 7 Days)") {
            // Placeholder for the Bar Chart
            BarChart(
                dailySpendingData = dailySpendingData,
                sevenDaysDates = sevenDaysDates
                )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartCard(title = "Category-wise Spending") {
            PieChart(data = categorySpendingAndCount.mapValues { it.value.first })
            // Lists of category summaries
            Spacer(modifier = Modifier.height(16.dp))

            // Use a Column to display the list of cards with spacing
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                categorySpendingAndCount.forEach { (category, data) ->
                    CategorySummaryRow(
                        category = category,
                        amount = data.first,
                        percentage = (data.first / sevenDayTotal) * 100,
                        count = data.second
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Export Options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp)
            ,
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Export Options", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                ExportButton(text = "Export PDF", iconId = R.drawable.outline_download_24, onClick = {
                    expenseReportViewModel.exportToPdf(context)
                })
                Spacer(modifier = Modifier.height(8.dp))
                ExportButton(text = "Export CSV", iconId = R.drawable.outline_download_24, onClick = {
                    expenseReportViewModel.exportToCsv(context)
                })
                Spacer(modifier = Modifier.height(8.dp))
                ExportButton(text = "Share Report", iconId = R.drawable.outline_share_24, onClick = {
                    expenseReportViewModel.shareReport(context)
                })
            }
        }


    }
}

@Composable
fun InsightCard(title: String, subtitle: String, amount: Double, iconId: Int, tintColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5F9))
    ) {

        Column(modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(title, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(subtitle, fontSize = 25.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "₹${"%.0f".format(amount)}",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.End
            )
        }
    }
}


@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            content()
        }
    }
}


@Composable
fun BarChart(dailySpendingData: Map<String, Double>, sevenDaysDates: List<String>) {
    val dataWithAllDays = sevenDaysDates.associateWith { dailySpendingData[it] ?: 0.0 }

    val totalValues = dataWithAllDays.values.sum()
    if (totalValues == 0.0) {
        EmptyChart(title = "No expenses found for the last 7 days")
        return
    }

    val maxSpending = dataWithAllDays.values.maxOrNull() ?: 0.0
    val yAxisMax = when {
        maxSpending == 0.0 -> 400.0
        maxSpending > 1000 -> (maxSpending / 100).toInt() * 100 + 100.0
        else -> (maxSpending / 50).toInt() * 50 + 50.0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                for (i in 4 downTo 0) {
                    val labelValue = (yAxisMax / 4) * i
                    Text(
                        text = "₹${"%.0f".format(labelValue)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }
            }

            // Chart and X-axis labels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw the vertical y-axis line
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, 0f),
                            end = Offset(0f, canvasHeight),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Draw Y-Axis horizontal grid lines (dashed)
                        val numYAxisLines = 4
                        val dashPathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        for (i in 0..numYAxisLines) {
                            val y = (canvasHeight / numYAxisLines) * i
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashPathEffect
                            )
                        }

                        // Draw the bottom-most line (x-axis) as a solid line
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )


                        // Draw Bars (the code remains the same)
                        val barCount = dataWithAllDays.size
                        val barSpacing = canvasWidth / barCount
                        val barWidth = barSpacing * 0.6f
                        val startOffset = (barSpacing - barWidth) / 2

                        var currentIndex = 0
                        dataWithAllDays.values.forEach { amount ->
                            val barHeight = (amount / yAxisMax).toFloat() * canvasHeight
                            val xPos = (currentIndex * barSpacing) + startOffset

                            drawRect(
                                color = Color(0xFF4DB6AC),
                                topLeft = Offset(xPos, canvasHeight - barHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                            )
                            currentIndex++
                        }
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sevenDaysDates.forEach { date ->
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// New PieChart Composable
@Composable
fun PieChart(data: Map<String, Double>) {
    val totalSum = data.values.sum()
    if (totalSum == 0.0) {
        EmptyChart(title = "No expenses found for the last 7 days")
        return
    }

    val textMeasurer = rememberTextMeasurer()

    // Pie chart drawing
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2
        val center = Offset(size.width / 2, size.height / 2)
        val topLeftOffset = Offset(
            x = (size.width - diameter) / 2,
            y = (size.height - diameter) / 2
        )
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
        var startAngle = 0f

        // First, draw all the pie slices
        data.forEach { (category, amount) ->
            val angle = (amount / totalSum).toFloat() * 360f
            val sweepAngle = angle
            drawArc(
                color = when (category) {
                    "Staff" -> Color(0xFF00BCD4)
                    "Travel" -> Color(0xFF4CAF50)
                    "Food" -> Color(0xFFFFC107)
                    "Utility" -> Color(0xFFF44336)
                    else -> Color.Gray
                },
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = androidx.compose.ui.graphics.drawscope.Fill,
                topLeft = topLeftOffset,
                size = arcSize
            )
            startAngle += angle
        }

        // Second, draw the text labels
        startAngle = 0f // Reset startAngle for drawing labels
        data.forEach { (category, amount) ->
            val angle = (amount / totalSum).toFloat() * 360f
            val sweepAngle = angle

            // Calculate the position for the text label
            val midAngle = startAngle + sweepAngle / 2
            val xPos = center.x + (radius * 0.7f * cos(midAngle * PI / 180).toFloat())
            val yPos = center.y + (radius * 0.7f * sin(midAngle * PI / 180).toFloat())

            val text = "$category: ₹${"%.0f".format(amount)}"
            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = when (category) {
                        "Staff" -> Color(0xFF00BCD4)
                        "Travel" -> Color(0xFF4CAF50)
                        "Food" -> Color(0xFFFFC107)
                        "Utility" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = xPos - textLayoutResult.size.width / 2,
                    y = yPos - textLayoutResult.size.height / 2
                )
            )

            startAngle += angle
        }
    }
}

@Composable
fun EmptyChart(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun CategorySummaryRow(category: String, amount: Double, percentage: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: color dot, category name, transaction count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color = when (category) {
                                "Staff" -> Color(0xFF00BCD4)
                                "Travel" -> Color(0xFF4CAF50)
                                "Food" -> Color(0xFFFFC107)
                                "Utility" -> Color(0xFFF44336)
                                else -> Color.Gray
                            })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$count transactions",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Right side: amount and percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%.0f".format(amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${"%.1f".format(percentage)}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


@Composable
fun ExportButton(text: String, iconId: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(painter = painterResource(id = iconId), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}