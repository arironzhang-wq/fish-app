package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunao.fishing.data.Gear
import com.yunao.fishing.data.GearPlan
import com.yunao.fishing.data.MockData

@Composable
fun GearScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("装备智能推荐", style = MaterialTheme.typography.titleLarge)
            Text("按目标鱼种、水域类型、季节匹配线组/饵料/钓法", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        items(MockData.gearPlans) { plan -> GearPlanCard(plan) }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun GearPlanCard(plan: GearPlan) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row {
                Tag(plan.targetSpecies)
                Spacer(Modifier.width(8.dp))
                Tag(plan.waterType)
                Spacer(Modifier.width(8.dp))
                Tag(plan.season)
            }
            Spacer(Modifier.height(10.dp))
            plan.items.forEach { g -> GearRow(g) }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun GearRow(g: Gear) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row {
            Text(g.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text("  ${g.name}", fontWeight = FontWeight.Medium)
        }
        Text(g.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
    }
}
