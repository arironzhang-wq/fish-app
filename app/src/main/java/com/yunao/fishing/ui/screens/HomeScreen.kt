package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.CatchLogEntry
import com.yunao.fishing.data.LocalRepository
import com.yunao.fishing.data.ForecastEngine
import com.yunao.fishing.data.ForecastFactor
import com.yunao.fishing.data.SpotForecast

@Composable
fun HomeScreen() {
    var logs by remember { mutableStateOf<List<CatchLogEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        logs = try { LocalRepository.getLogs() } catch (e: Exception) { emptyList() }
        loading = false
    }

    val forecasts = remember(logs) { ForecastEngine.buildForecasts(logs) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text(
                "专属出钓大脑",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "基于你自己的历史出钓数据，而不是通用鱼情指数",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        when {
            loading -> item { CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp)) }
            forecasts.isEmpty() -> item {
                Text(
                    "还没有出钓记录。去「复盘日志」记一次出钓，专属预测就会在这里出现。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            else -> items(forecasts) { forecast -> ForecastCard(forecast) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ForecastCard(forecast: SpotForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(forecast.spotName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "你的整体中鱼率 ${forecast.baselineHitRate}% · 共 ${forecast.totalTrips} 次记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                ScoreBadge(score = forecast.hitRate, favorable = forecast.hitRate >= forecast.baselineHitRate)
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { forecast.hitRate / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (forecast.hitRate >= forecast.baselineHitRate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
            )

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "在「${forecast.spotName}」，你这个专属指数比整体平均${if (forecast.hitRate >= forecast.baselineHitRate) "高" else "低"} ${Math.abs(forecast.hitRate - forecast.baselineHitRate)} 个百分点",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (forecast.factors.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("为什么这么判断", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                forecast.factors.forEach { factor -> FactorRow(factor) }
            } else {
                Spacer(Modifier.height(10.dp))
                Text(
                    "该钓点数据积累中（当前 ${forecast.totalTrips} 次），记录满 3 次后会显示具体影响因子",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: Int, favorable: Boolean) {
    val color = if (favorable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$score",
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text("专属中鱼率", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FactorRow(factor: ForecastFactor) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("${factor.dimension}：${factor.label}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "该条件下 ${factor.sampleSize} 次记录，中鱼率 ${factor.sampleHitRate}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (factor.isFavorable) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                contentDescription = null,
                tint = if (factor.isFavorable) MaterialTheme.colorScheme.secondary else Color(0xFFD4562D),
                modifier = Modifier.height(18.dp)
            )
            Text(
                "${if (factor.contributionPercent > 0) "+" else ""}${factor.contributionPercent}",
                color = if (factor.isFavorable) MaterialTheme.colorScheme.secondary else Color(0xFFD4562D),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
