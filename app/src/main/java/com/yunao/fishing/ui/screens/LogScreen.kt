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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yunao.fishing.data.CatchLogEntry
import com.yunao.fishing.data.LocalRepository
import com.yunao.fishing.data.LocationHelper
import com.yunao.fishing.data.WeatherRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogScreen() {
    var logs by remember { mutableStateOf<List<CatchLogEntry>>(emptyList()) }
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

    LaunchedEffect(reloadKey) {
        loading = true
        logs = try { LocalRepository.getLogs() } catch (e: Exception) { emptyList() }
        loading = false
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "记录一次出钓")
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
            Text("出钓复盘日志", style = MaterialTheme.typography.titleLarge)
            Text(
                "每一次记录都在训练你的专属出钓大脑",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))

            when {
                loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                logs.isEmpty() -> Text(
                    "还没有记录，点右下角 + 记一次出钓吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 24.dp)
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogCard(log, onDelete = {
                            scope.launch {
                                LocalRepository.deleteLog(log.id)
                                reloadKey++
                            }
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLogDialog(
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            onDismiss = { showAddDialog = false },
            onSave = { entry ->
                scope.launch {
                    LocalRepository.addLog(entry)
                    showAddDialog = false
                    reloadKey++
                }
            }
        )
    }
}

@Composable
private fun LogCard(log: CatchLogEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(log.spotName, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(log.dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
            Text("${log.species} · ${log.countAndSize}", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            if (log.weatherSnapshot.isNotBlank()) {
                Text("天气快照：${log.weatherSnapshot}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            if (log.gearUsed.isNotBlank()) {
                Text("装备：${log.gearUsed}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            if (log.note.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(log.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AddLogDialog(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (CatchLogEntry) -> Unit
) {
    var spotName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var countFish by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var sky by remember { mutableStateOf("") }
    var windDir by remember { mutableStateOf("") }
    var windForce by remember { mutableStateOf("") }
    var pressureTrend by remember { mutableStateOf("") }
    var gearUsed by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var autoWeatherLoading by remember { mutableStateOf(false) }
    var autoWeatherSummary by remember { mutableStateOf<String?>(null) }
    var autoWeatherError by remember { mutableStateOf<String?>(null) }
    var hasAutoFetched by remember { mutableStateOf(false) }

    fun autoFetchWeather() {
        autoWeatherLoading = true
        autoWeatherError = null
        scope.launch {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc == null) {
                autoWeatherError = "定位失败，请检查 GPS/位置服务，或在下方手动选择"
                autoWeatherLoading = false
                return@launch
            }
            WeatherRepository.fetchAutoWeather(loc.latitude, loc.longitude)
                .onSuccess { auto ->
                    sky = auto.sky
                    windDir = auto.windDir
                    windForce = auto.windForce
                    pressureTrend = auto.pressureTrend
                    autoWeatherSummary = auto.summary
                }
                .onFailure {
                    autoWeatherError = "天气获取失败（${it.message ?: "网络异常"}），请在下方手动选择"
                }
            autoWeatherLoading = false
        }
    }

    // 弹窗一打开就自动申请定位权限并抓取天气，不需要用户额外点按钮
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onRequestPermission()
        } else if (!hasAutoFetched) {
            hasAutoFetched = true
            autoFetchWeather()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记一次出钓") },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(spotName, { spotName = it }, label = { Text("钓点") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(species, { species = it }, label = { Text("鱼种") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        countFish, { countFish = it.filter { c -> c.isDigit() } },
                        label = { Text("尾数") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        weightKg, { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("总重(kg)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                Text("所在位置天气", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                when {
                    !hasLocationPermission -> Column {
                        Text(
                            "需要定位权限才能自动显示天气",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        TextButton(onClick = onRequestPermission) { Text("授权定位") }
                    }
                    autoWeatherLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("正在自动获取…", style = MaterialTheme.typography.bodySmall)
                    }
                    autoWeatherError != null -> Column {
                        Text(autoWeatherError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        TextButton(onClick = {
                            if (!hasLocationPermission) onRequestPermission() else autoFetchWeather()
                        }) { Text("重试") }
                    }
                    autoWeatherSummary != null -> Text(
                        autoWeatherSummary!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (autoWeatherSummary != null) {
                    Text(
                        "已自动获取，如有出入可在下面手动调整",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                Spacer(Modifier.height(8.dp))

                ChipGroup("天气", listOf("晴", "多云", "阴", "雨"), sky) { sky = it }
                Spacer(Modifier.height(8.dp))
                ChipGroup("风向", listOf("东北", "东南", "西北", "西南", "无明显"), windDir) { windDir = it }
                Spacer(Modifier.height(8.dp))
                ChipGroup("风力", listOf("无风", "1-2级", "3-4级", "5级以上"), windForce) { windForce = it }
                Spacer(Modifier.height(8.dp))
                ChipGroup("气压趋势", listOf("上升", "平稳", "下降"), pressureTrend) { pressureTrend = it }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(gearUsed, { gearUsed = it }, label = { Text("使用装备") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(note, { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (spotName.isNotBlank()) {
                    onSave(
                        CatchLogEntry(
                            spotName = spotName.trim(),
                            species = species.trim(),
                            countFish = countFish.toIntOrNull() ?: 0,
                            weightKg = weightKg.toDoubleOrNull() ?: 0.0,
                            weatherSky = sky,
                            weatherWindDir = windDir,
                            weatherWindForce = windForce,
                            weatherPressureTrend = pressureTrend,
                            gearUsed = gearUsed.trim(),
                            note = note.trim(),
                            dateStr = SimpleDateFormat("MM-dd", Locale.CHINA).format(Date()),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun ChipGroup(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            options.forEach { opt ->
                FilterChip(
                    selected = selected == opt,
                    onClick = { onSelect(if (selected == opt) "" else opt) },
                    label = { Text(opt, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}
