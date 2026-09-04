package com.yunao.fishing.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.UUID

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
    var weatherSky: String = "",
    var weatherWindDir: String = "",
    var weatherWindForce: String = "",
    var weatherPressureTrend: String = "",
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
    var name: String = "", var type: String = "", var note: String = "", var timestamp: Long = 0L
)

@Entity(tableName = "gear_items")
data class UserGearItem(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var category: String = "", var name: String = "", var note: String = "", var timestamp: Long = 0L
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var title: String = "", var spotName: String = "", var dateTime: String = "",
    var organizerUid: String = "", var organizerName: String = "", var capacity: Int = 6,
    var joinedUids: List<String> = emptyList(), var timestamp: Long = 0L
) {
    val joined: Int get() = joinedUids.size
}

data class ForecastFactor(
    val label: String, val dimension: String, val sampleHitRate: Int,
    val overallHitRate: Int, val sampleSize: Int
) {
    val contributionPercent: Int get() = sampleHitRate - overallHitRate
    val isFavorable: Boolean get() = contributionPercent >= 0
}

data class SpotForecast(
    val spotName: String, val totalTrips: Int, val hitRate: Int,
    val baselineHitRate: Int, val factors: List<ForecastFactor>
)

