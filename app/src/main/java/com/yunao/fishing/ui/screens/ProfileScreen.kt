package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.CatchLogEntry
import com.yunao.fishing.data.LocalRepository
import com.yunao.fishing.data.UserSpot
import kotlin.math.roundToInt

private data class Stat(val label: String, val value: String)

@Composable
fun ProfileScreen() {
    var logs by remember { mutableStateOf<List<CatchLogEntry>>(emptyList()) }
    var spots by remember { mutableStateOf<List<UserSpot>>(emptyList()) }
    var nickname by remember { mutableStateOf("渔友") }
    var showEditNickname by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        logs = try { LocalRepository.getLogs() } catch (e: Exception) { emptyList() }
        spots = try { LocalRepository.getSpots() } catch (e: Exception) { emptyList() }
        nickname = LocalRepository.getMyNickname()
    }

    val totalWeight = logs.sumOf { it.weightKg }
    val hitTrips = logs.count { it.countFish > 0 }
    val hitRate = if (logs.isNotEmpty()) (hitTrips * 100f / logs.size).roundToInt() else 0

    val stats = listOf(
        Stat("累计出钓", "${logs.size} 次"),
        Stat("累计渔获", "%.1f kg".format(totalWeight)),
        Stat("常去钓点", "${spots.size} 个"),
        Stat("专属中鱼率", if (logs.isEmpty()) "暂无数据" else "$hitRate%"),
    )

    val insights = buildInsights(logs)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("我的钓鱼档案", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(nickname, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                TextButton(onClick = { showEditNickname = true }) { Text("编辑昵称") }
            }
            Text("出钓记录保存在本机；天气/附近钓点搜索需要联网", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(14.dp)) {
                    stats.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            row.forEach { s ->
                                Column(Modifier.padding(vertical = 6.dp)) {
                                    Text(s.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(s.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Text("专属数据洞察", style = MaterialTheme.typography.titleMedium) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(14.dp)) {
                    if (insights.isEmpty()) {
                        Text("多记录几次出钓，这里会显示只属于你的数据洞察", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        insights.forEach { text ->
                            Row(Modifier.padding(vertical = 4.dp)) {
                                Text("• ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showEditNickname) {
        var draft by remember(nickname) { mutableStateOf(nickname) }
        AlertDialog(
            onDismissRequest = { showEditNickname = false },
            title = { Text("编辑昵称") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    LocalRepository.setMyNickname(draft.trim())
                    showEditNickname = false
                    reloadKey++
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNickname = false }) { Text("取消") }
            }
        )
    }
}

private fun buildInsights(logs: List<CatchLogEntry>): List<String> {
    if (logs.size < 3) return emptyList()
    val insights = mutableListOf<String>()

    val byWindDir = logs.groupBy { it.weatherWindDir }.filterKeys { it.isNotBlank() && it != "无明显" }
    byWindDir.entries.filter { it.value.size >= 2 }
        .maxByOrNull { it.value.count { l -> l.countFish > 0 } * 100f / it.value.size }
        ?.let { (dir, dirLogs) ->
            val rate = (dirLogs.count { it.countFish > 0 } * 100f / dirLogs.size).roundToInt()
            val overall = (logs.count { it.countFish > 0 } * 100f / logs.size).roundToInt()
            if (rate > overall) insights.add("你在${dir}风时的中鱼率比平均高 ${rate - overall} 个百分点")
        }

    val bestSpot = logs.groupBy { it.spotName }.maxByOrNull { it.value.size }
    if (bestSpot != null && bestSpot.value.size >= 2) {
        insights.add("「${bestSpot.key}」是你去得最多的钓点，共记录 ${bestSpot.value.size} 次")
    }

    val bestHour = logs.groupBy { it.weatherSky }.filterKeys { it.isNotBlank() }
        .maxByOrNull { it.value.size }
    if (bestHour != null) {
        insights.add("你出钓时最常遇到「${bestHour.key}」天气，共 ${bestHour.value.size} 次")
    }

    return insights.take(3)
}
