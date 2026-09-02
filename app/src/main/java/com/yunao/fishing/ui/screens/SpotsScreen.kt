package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Spot(
    val name: String,
    val type: String,
    val distanceKm: Double,
    val myVisits: Int,
    val myBestScore: Int
)

private val spots = listOf(
    Spot("东湖野钓点·3号桩", "野钓·开阔水域", 4.2, 12, 82),
    Spot("南渠黑坑·老坑", "黑坑·付费坑塘", 7.8, 5, 47),
    Spot("水库大坝西侧", "路亚·大型水库", 15.3, 3, 66),
    Spot("城北河道·柳树湾", "野钓·河道", 2.1, 0, 0),
)

@Composable
fun SpotsScreen() {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("我的钓点", style = MaterialTheme.typography.titleLarge)
            Text("标记你的私藏钓点，沉淀专属出钓数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🗺  卫星地图视图（Demo 中以列表代替）",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        items(spots) { s -> SpotCard(s) }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SpotCard(s: Spot) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.name, style = MaterialTheme.typography.titleMedium)
                Text("${s.distanceKm} km", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }
            Text(s.type, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            if (s.myVisits > 0) {
                Text(
                    "你去过 ${s.myVisits} 次 · 专属指数最高 ${s.myBestScore} 分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text("还没有数据 · 去这里记录第一次出钓", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
