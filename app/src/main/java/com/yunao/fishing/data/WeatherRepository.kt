package com.yunao.fishing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 自动获取当前位置天气快照：用定位坐标查询 Open-Meteo（免注册免 key），
 * 给"记一次出钓"表单自动预填天气/风向/风力/气压趋势的 Chip 选项，减少手动选择。
 * 注意：这只是给"记录当次天气"这个输入环节做自动化，跟 ForecastEngine 不依赖外部天气 API
 * 做预测的设计原则不冲突——预测算法仍然只用你自己的历史记录统计，没有变化。
 */
object WeatherRepository {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    data class AutoWeather(
        val summary: String,      // 给用户看的一句话摘要，例如"多云 · 东南风 3-4级 · 气压上升"
        val sky: String,          // 对应 ChipGroup("天气", listOf("晴","多云","阴","雨"))
        val windDir: String,      // 对应 ChipGroup("风向", listOf("东北","东南","西北","西南","无明显"))
        val windForce: String,    // 对应 ChipGroup("风力", listOf("无风","1-2级","3-4级","5级以上"))
        val pressureTrend: String // 对应 ChipGroup("气压趋势", listOf("上升","平稳","下降"))
    )

    suspend fun fetchAutoWeather(lat: Double, lon: Double): Result<AutoWeather> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&current=weather_code,wind_speed_10m,wind_direction_10m" +
                "&hourly=pressure_msl&past_hours=3&forecast_hours=1&timezone=auto"

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) throw java.io.IOException("HTTP ${resp.code}")
                val body = resp.body?.string() ?: throw java.io.IOException("空响应")
                val json = JSONObject(body)
                val current = json.getJSONObject("current")

                val sky = skyFromCode(current.getInt("weather_code"))
                val windSpeed = current.getDouble("wind_speed_10m")
                val windForce = windForceBucket(windSpeed)
                val windDir = if (windForce == "无风") "无明显" else windDirBucket(current.getInt("wind_direction_10m"))
                val pressureTrend = pressureTrendFromHourly(json.optJSONObject("hourly"))

                AutoWeather(
                    summary = "$sky · ${if (windDir == "无明显") "" else windDir}$windForce · 气压$pressureTrend",
                    sky = sky,
                    windDir = windDir,
                    windForce = windForce,
                    pressureTrend = pressureTrend
                )
            }
        }
    }

    /** WMO 天气代码归到现有四选一分类，详见 https://open-meteo.com/en/docs */
    private fun skyFromCode(code: Int): String = when (code) {
        0 -> "晴"
        1, 2 -> "多云"
        3, 45, 48 -> "阴"
        else -> "雨" // 雨/雷雨/雪/冰雹等统一归入"雨"档
    }

    private fun windDirBucket(deg: Int): String {
        val d = ((deg % 360) + 360) % 360
        return when {
            d < 90 -> "东北"
            d < 180 -> "东南"
            d < 270 -> "西南"
            else -> "西北"
        }
    }

    private fun windForceBucket(speedKmh: Double): String = when {
        speedKmh < 2 -> "无风"
        speedKmh < 12 -> "1-2级"
        speedKmh < 29 -> "3-4级"
        else -> "5级以上"
    }

    /** 用过去3小时到现在的气压变化判断趋势，而不是单一时间点的绝对值 */
    private fun pressureTrendFromHourly(hourly: JSONObject?): String {
        val values = hourly?.optJSONArray("pressure_msl") ?: return "平稳"
        if (values.length() < 2) return "平稳"
        val first = values.getDouble(0)
        val last = values.getDouble(values.length() - 1)
        return when {
            last - first > 0.5 -> "上升"
            last - first < -0.5 -> "下降"
            else -> "平稳"
        }
    }
}
