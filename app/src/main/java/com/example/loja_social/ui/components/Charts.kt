package com.example.loja_social.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Stock por Categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            data.forEach { (category, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.width(80.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    ) {
                        val barWidth = (value.toFloat() / maxValue) * size.width
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(barWidth, size.height)
                        )
                    }
                    
                    Text(
                        text = value.toString(),
                        modifier = Modifier.width(40.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleDonutChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val error = MaterialTheme.colorScheme.error
    val colors = listOf(primary, secondary, tertiary, error)
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Distribuição do Stock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val total = data.sumOf { it.second.toDouble() }.toFloat()
                var startAngle = 0f
                
                data.forEachIndexed { index, (_, value) ->
                    val sweepAngle = (value / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            data.forEachIndexed { index, (label, value) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(colors[index % colors.size])
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$label: ${value.toInt()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun TrendIndicator(
    value: String,
    trend: Float, // Positive = up, Negative = down
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        val (arrow, color) = when {
            trend > 0 -> "↗" to MaterialTheme.colorScheme.primary
            trend < 0 -> "↘" to MaterialTheme.colorScheme.error
            else -> "→" to MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        Text(
            text = arrow,
            color = color,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun PercentageIndicator(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val color = when {
        percentage >= 80f -> MaterialTheme.colorScheme.primary
        percentage >= 50f -> Color(0xFFFF9800) // Orange
        else -> MaterialTheme.colorScheme.error
    }
    
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier.size(60.dp)
        ) {
            // Background circle
            drawCircle(
                color = surfaceVariant,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
            )
            
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (percentage / 100f) * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
            )
        }
        
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariant
        )
    }
}