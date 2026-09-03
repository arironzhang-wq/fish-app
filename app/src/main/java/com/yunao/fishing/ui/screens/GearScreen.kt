package com.yunao.fishing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.yunao.fishing.data.FirebaseRepository
import com.yunao.fishing.data.Gear
import com.yunao.fishing.data.GearPlan
import com.yunao.fishing.data.MockData
import com.yunao.fishing.data.UserGearItem
import kotlinx.coroutines.launch

@Composable
fun GearScreen() {
    var myGear by remember { mutableStateOf<List<UserGearItem>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadKey) {
        myGear = try { FirebaseRepository.getGearItems() } catch (e: Exception) { emptyList() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("装备智能推荐", style = MaterialTheme.typography.titleLarge)
            Text("按目标鱼种、水域类型、季节匹配线组/饵料/钓法（参考方案）", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        items(MockData.gearPlans) { plan -> GearPlanCard(plan) }

        item { Spacer(Modifier.height(6.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("我的装备清单", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.height(16.dp))
                    Text(" 添加")
                }
            }
        }
        if (myGear.isEmpty()) {
            item {
                Text(
                    "还没有添加装备",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            items(myGear, key = { it.id }) { item ->
                MyGearRow(item, onDelete = {
                    scope.launch {
                        FirebaseRepository.deleteGearItem(item.id)
                        reloadKey++
                    }
                })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showAddDialog) {
        AddGearDialog(
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                scope.launch {
                    FirebaseRepository.addGearItem(item)
                    showAddDialog = false
                    reloadKey++
                }
            }
        )
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

@Composable
private fun MyGearRow(item: UserGearItem, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                Column {
                    Row {
                        if (item.category.isNotBlank()) {
                            Text(item.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("  ")
                        }
                        Text(item.name, fontWeight = FontWeight.Medium)
                    }
                    if (item.note.isNotBlank()) {
                        Text(item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun AddGearDialog(onDismiss: () -> Unit, onSave: (UserGearItem) -> Unit) {
    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加装备") },
        text = {
            Column {
                OutlinedTextField(category, { category = it }, label = { Text("分类（如：竿/线组/饵料）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(note, { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(UserGearItem(category = category.trim(), name = name.trim(), note = note.trim(), timestamp = System.currentTimeMillis()))
                }
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
