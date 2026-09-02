package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.Achievement
import com.yunao.fishing.data.FishingTrip
import com.yunao.fishing.data.LeaderboardEntry
import com.yunao.fishing.data.MockData

@Composable
fun CommunityScreen() {
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
        items(MockData.trips) { trip -> TripCard(trip) }

        item { Spacer(Modifier.height(6.dp)) }
        item { Text("本周渔获排行榜", style = MaterialTheme.typography.titleMedium) }
        item { LeaderboardCard(MockData.leaderboard) }

        item { Spacer(Modifier.height(6.dp)) }
        item { Text("成就勋章", style = MaterialTheme.typography.titleMedium) }
        items(MockData.achievements) { a -> AchievementRow(a) }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun TripCard(trip: FishingTrip) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(trip.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${trip.spotName} · ${trip.dateTime}", style = MaterialTheme.typography.bodyMedium)
            Text("发起人：${trip.organizer}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已报名 ${trip.joined}/${trip.capacity} 人", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                OutlinedButton(onClick = { }) { Text("申请加入") }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(entries: List<LeaderboardEntry>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            entries.forEach { e ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (e.rank <= 3) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text(
                                "${e.rank}",
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(e.nickname, fontWeight = FontWeight.Medium)
                            if (e.badge.isNotBlank()) {
                                Text(e.badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text("${e.totalWeightKg} kg", fontWeight = FontWeight.Bold)
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
