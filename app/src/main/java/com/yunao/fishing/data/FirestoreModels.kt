package com.yunao.fishing.data
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName


/**
 * 以下均为 Firestore 读写用的数据模型。
 * Firestore 反序列化要求：无参构造函数 + 可变/带默认值的 var 属性。
 */

data class CatchLogEntry(
    @get:Exclude var id: String = "",
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
        @Exclude get() = if (countFish > 0) "$countFish 尾，约 ${weightKg} kg" else "空军"
    val weatherSnapshot: String
        @Exclude get() = listOf(weatherSky, weatherWindDir + weatherWindForce, "气压" + weatherPressureTrend)
            .filter { it.isNotBlank() && it != "无明显" }
            .joinToString(" · ")
}

data class UserSpot(
    @get:Exclude var id: String = "",
    var name: String = "",
    var type: String = "",
    var note: String = "",
    var timestamp: Long = 0L
)

data class UserGearItem(
    @get:Exclude var id: String = "",
    var category: String = "",
    var name: String = "",
    var note: String = "",
    var timestamp: Long = 0L
)

data class Trip(
    @get:Exclude var id: String = "",
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
        @Exclude get() = joinedUids.size
}

data class UserProfile(
    var nickname: String = "",
    @PropertyName("createdAt") var createdAt: Long = 0L
)

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
