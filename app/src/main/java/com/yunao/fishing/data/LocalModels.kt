package com.yunao.fishing.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.UUID

/**
 * 本机离线数据模型（Room）。
 * 所有数据保存在设备本地 SQLite 数据库中，不依赖任何云端服务。
 * 字段结构与之前的 Firestore 版本保持一致，方便以后切回云端同步（比如国内可用的后端）。
 */

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}

@Entity(tableName = "catch_logs")
data class CatchLogEntry(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var spotName: String = "",
    var species: String = "",
    var countFish: Int = 0,
    var weightKg: Double = 0.0,
    var weatherSky: String = "",       // 晴/多云/阴/雨
    var weatherWindDir: String = "",   // 东北/东南/西北/西南/无明显
    var weatherWindForce: String = "", // 无风/1-2级/3-4级/5级以上
    var weatherPressureTrend: String = "", // 上升/平稳/下降
    var gearUsed: String = "",
    var note: String = "",
    var dateStr: String = "",
    var timestamp: Long = 0L
) {
    val countAndSize: String
        get() = if (countFish > 0) "$countFish 尾，约 ${weightKg} kg" else "空军"

    val weatherSnapshot: String
        get() = listOf(weatherSky, weatherWindDir + weatherWindForce, "气压" + weatherPressureTrend)
            .filter { it.isNotBlank() && it != "无明显" }
            .joinToString(" · ")
}

@Entity(tableName = "spots")
data class UserSpot(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var type: String = "",
    var note: String = "",
    var lat: Double? = null,
    var lon: Double? = null,
    var timestamp: Long = 0L
)

/** 附近钓场搜索结果（来自 OpenStreetMap Overpass，不入库，仅用于展示和"添加到我的钓点"） */
data class NearbySpot(
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double
)

@Entity(tableName = "gear_items")
data class UserGearItem(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var category: String = "",
    var name: String = "",
    var note: String = "",
    var timestamp: Long = 0L
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var spotName: String = "",
    var dateTime: String = "",
    var organizerUid: String = "",
    var organizerName: String = "",
    var capacity: Int = 6,
    var joinedUids: List<String> = emptyList(),
    var timestamp: Long = 0L
) {
    val joined: Int
        get() = joinedUids.size
}

/** Home 页"出钓大脑"用：基于用户自己历史记录统计出的单个钓点专属预测 */
data class ForecastFactor(
    val label: String,        // 例如"东北风"
    val dimension: String,    // 例如"风向"
    val sampleHitRate: Int,   // 该条件下的中鱼率
    val overallHitRate: Int,  // 该钓点整体中鱼率
    val sampleSize: Int
) {
    val contributionPercent: Int get() = sampleHitRate - overallHitRate
    val isFavorable: Boolean get() = contributionPercent >= 0
}

data class SpotForecast(
    val spotName: String,
    val totalTrips: Int,
    val hitRate: Int,       // 该钓点专属中鱼率
    val baselineHitRate: Int, // 你所有钓点的平均中鱼率
    val factors: List<ForecastFactor>
)
