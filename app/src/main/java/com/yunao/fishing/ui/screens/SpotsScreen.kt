package com.yunao.fishing.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yunao.fishing.data.LocalRepository
import com.yunao.fishing.data.LocationHelper
import com.yunao.fishing.data.NearbySpot
import com.yunao.fishing.data.NearbySpotsRepository
import com.yunao.fishing.data.UserSpot
import kotlinx.coroutines.launch

@Composable
fun SpotsScreen() {
    var spots by remember { mutableStateOf<List<UserSpot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var myLat by remember { mutableStateOf<Double?>(null) }
    var myLon by remember { mutableStateOf<Double?>(null) }
    var searchingNearby by remember { mutableStateOf(false) }
    var nearbySearched by remember { mutableStateOf(false) }
    var nearbySpots by remember { mutableStateOf<List<NearbySpot>>(emptyList()) }

    fun searchNearby() {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        }
        scope.launch {
            searchingNearby = true
            nearbySearched = false
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                myLat = loc.latitude
                myLon = loc.longitude
                nearbySpots = NearbySpotsRepository.search(loc.latitude, loc.longitude)
            } else {
                nearbySpots = emptyList()
            }
            nearbySearched = true
            searchingNearby = false
        }
    }

    LaunchedEffect(reloadKey) {
        loading = true
        spots = try { LocalRepository.getSpots() } catch (e: Exception) { emptyList() }
        loading = false
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "新增钓点")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("我的钓点", style = MaterialTheme.typography.titleLarge)
            Text(
                "标记你的私藏钓点，沉淀专属出钓数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("附近钓场", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { searchNearby() }, enabled = !searchingNearby) {
                            Text(if (searchingNearby) "搜索中…" else "搜索附近")
                        }
                    }
                    when {
                        !hasLocationPermission && !searchingNearby && !nearbySearched -> Text(
                            "需要定位权限才能查找附近钓场，点「搜索附近」会向你申请授权",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        searchingNearby -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("正在定位并搜索附近钓场（数据来自 OpenStreetMap）…", style = MaterialTheme.typography.bodySmall)
                        }
                        nearbySearched && nearbySpots.isEmpty() -> Text(
                            "附近暂未收录钓场数据，OSM 在部分地区覆盖较少，搜不到属于正常情况",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        nearbySpots.isNotEmpty() -> Column {
                            nearbySpots.forEach { ns ->
                                NearbySpotRow(ns) {
                                    scope.launch {
                                        LocalRepository.addSpot(
                                            UserSpot(
                                                name = ns.name,
                                                type = ns.category,
                                                note = "来自附近钓场搜索",
                                                lat = ns.lat,
                                                lon = ns.lon,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        reloadKey++
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            when {
                loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                spots.isEmpty() -> Text(
                    "还没有钓点，点右下角 + 添加一个，或在上面搜索附近钓场",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 24.dp)
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(spots, key = { it.id }) { s ->
                        val distanceText = if (myLat != null && myLon != null && s.lat != null && s.lon != null) {
                            LocationHelper.formatDistance(
                                LocationHelper.distanceMeters(myLat!!, myLon!!, s.lat!!, s.lon!!)
                            )
                        } else null
                        SpotCard(s, distanceText, onDelete = {
                            scope.launch {
                                LocalRepository.deleteSpot(s.id)
                                reloadKey++
                            }
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSpotDialog(
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            onDismiss = { showAddDialog = false },
            onSave = { spot ->
                scope.launch {
                    LocalRepository.addSpot(spot)
                    showAddDialog = false
                    reloadKey++
                }
            }
        )
    }
}

@Composable
private fun NearbySpotRow(spot: NearbySpot, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(spot.name, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                Text(
                    "${spot.category} · ${LocationHelper.formatDistance(spot.distanceMeters)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
        TextButton(onClick = onAdd) { Text("添加") }
    }
}

@Composable
private fun SpotCard(s: UserSpot, distanceText: String?, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.name, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (s.type.isNotBlank()) {
                    Text(s.type, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                if (distanceText != null) {
                    if (s.type.isNotBlank()) Text(" · ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("距你 $distanceText", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (s.note.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(s.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AddSpotDialog(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (UserSpot) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var locating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加钓点") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("钓点名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(type, { type = it }, label = { Text("类型（如：野钓/黑坑/路亚）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(note, { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        if (!hasLocationPermission) {
                            onRequestPermission()
                        } else {
                            scope.launch {
                                locating = true
                                val loc = LocationHelper.getCurrentLocation(context)
                                lat = loc?.latitude
                                lon = loc?.longitude
                                locating = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            locating -> "定位中…"
                            lat != null && lon != null -> "已获取当前位置"
                            else -> "获取当前位置（可选）"
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        UserSpot(
                            name = name.trim(),
                            type = type.trim(),
                            note = note.trim(),
                            lat = lat,
                            lon = lon,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
