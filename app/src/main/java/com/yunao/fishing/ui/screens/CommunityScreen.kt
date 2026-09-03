package com.yunao.fishing.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.Achievement
import com.yunao.fishing.data.CatchLogEntry
import com.yunao.fishing.data.FirebaseRepository
import com.yunao.fishing.data.Trip
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen() {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var logs by remember { mutableStateOf<List<CatchLogEntry>>(emptyList()) }
    var nickname by remember { mutableStateOf("渔友") }
    var showAddDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val myUid = FirebaseRepository.currentUser?.uid

    LaunchedEffect(reloadKey) {
        trips = try { FirebaseRepository.getTrips() } catch (e: Exception) { emptyList() }
        logs = try { FirebaseRepository.getLogs() } catch (e: Exception) { emptyList() }
        nickname = try { FirebaseRepository.getMyNickname() } catch (e: Exception) { "渔友" }
    }

    val achievements = buildAchievements(logs)
    Scaffold2(onAdd = { showAddDialog = true }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text("组队约钓", style = MaterialTheme.typography.titleLarge)
                Text("发起或加入约钓局，一起出钓", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (trips.isEmpty()) {
                item {
                    Text(
                        "还没有约钓局，点右下角 + 发起一个",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                items(trips, key = { it.id }) { trip ->
                    TripCard(trip, joined = myUid != null && myUid in trip.joinedUids, onJoin = {
                        scope.launch {
                            FirebaseRepository.joinTrip(trip.id)
                            reloadKey++
                        }
                    })
                }
            }

            item { Spacer(Modifier.height(6.dp)) }
            item { Text("我的成就", style = MaterialTheme.typography.titleMedium) }
            items(achievements) { a -> AchievementRow(a) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showAddDialog) {
        AddTripDialog(
            onDismiss = { showAddDialog = false },
            onSave = { trip ->
                scope.launch {
                    val uid = FirebaseRepository.currentUser?.uid ?: ""
                    FirebaseRepository.addTrip(
                        trip.copy(organizerUid = uid, organizerName = nickname, joinedUids = listOf(uid))
                    )
                    showAddDialog = false
                    reloadKey++
                }
            }
        )
    }
}

@Composable
private fun Scaffold2(onAdd: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "发起约钓")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun TripCard(trip: Trip, joined: Boolean, onJoin: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(trip.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${trip.spotName} · ${trip.dateTime}", style = MaterialTheme.typography.bodyMedium)
            Text("发起人：${trip.organizerName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已报名 ${trip.joined}/${trip.capacity} 人", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                OutlinedButton(onClick = onJoin, enabled = !joined && trip.joined < trip.capacity) {
                    Text(if (joined) "已加入" else "申请加入")
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(a: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (a.unlocked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                (if (a.unlocked) "✓ " else "") + a.name,
                fontWeight = FontWeight.Bold,
                color = if (a.unlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(a.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun AddTripDialog(onDismiss: () -> Unit, onSave: (Trip) -> Unit) {
    var title by remember { mutableStateOf("") }
    var spotName by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("6") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发起约钓") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(spotName, { spotName = it }, label = { Text("钓点") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateTime, { dateTime = it }, label = { Text("时间（如：本周六 05:30）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(capacity, { capacity = it.filter { c -> c.isDigit() } }, label = { Text("人数上限") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && spotName.isNotBlank()) {
                    onSave(
                        Trip(
                            title = title.trim(),
                            spotName = spotName.trim(),
                            dateTime = dateTime.trim(),
                            capacity = capacity.toIntOrNull() ?: 6,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("发起") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun buildAchievements(logs: List<CatchLogEntry>): List<Achievement> {
    val hasAny = logs.isNotEmpty()
    val accumulated10 = logs.size >= 10
    val last5AllCaught = logs.size >= 5 && logs.take(5).all { it.countFish > 0 }
    val bigCatch = logs.any { it.weightKg >= 2.0 }
    return listOf(
        Achievement("初次出钓", "完成第一次出钓记录", hasAny),
        Achievement("数据积累者", "累计记录 10 次出钓", accumulated10),
        Achievement("五连胜", "最近 5 次出钓均有渔获", last5AllCaught),
        Achievement("大物猎人", "单次渔获总重超过 2kg", bigCatch),
    )
}
