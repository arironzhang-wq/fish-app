package com.yunao.fishing.ui.screens
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.Factor
import com.yunao.fishing.data.MockData
import com.yunao.fishing.data.PersonalForecast

@Composable
fun HomeScreen() {
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
        items(MockData.personalForecasts) { forecast ->
            ForecastCard(forecast)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ForecastCard(forecast: PersonalForecast) {
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
                        "通用指数 ${forecast.baselineScore} 分 · 你的专属指数 ↓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                ScoreBadge(score = forecast.score, favorable = forecast.score >= forecast.baselineScore)
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { forecast.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (forecast.score >= forecast.baselineScore) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
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
                Text(forecast.summary, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))
            Text("为什么这么判断", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(6.dp))
            forecast.factors.forEach { factor -> FactorRow(factor) }
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
        Text("专属指数", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FactorRow(factor: Factor) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(factor.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${factor.currentValue} · 理想区间 ${factor.idealRange}",
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
