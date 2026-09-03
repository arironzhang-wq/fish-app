package com.yunao.fishing.data


/**
 * 基于用户自己的历史出钓日志，计算"专属出钓大脑"预测。
 * 不依赖任何外部天气 API —— 完全由用户真实记录的结构化数据统计得出，
 * 这正是与通用鱼情 App 的差异化所在：用你自己的历史数据说话。
 */
object ForecastEngine {

    private const val MIN_SAMPLES_FOR_FACTORS = 3

    fun buildForecasts(logs: List<CatchLogEntry>, maxSpots: Int = 3): List<SpotForecast> {
        if (logs.isEmpty()) return emptyList()

        val overallHitRate = hitRate(logs)
        val bySpot = logs.groupBy { it.spotName }
            .toList()
            .sortedByDescending { it.second.size }
            .take(maxSpots)

        return bySpot.map { (spotName, spotLogs) ->
            val spotHitRate = hitRate(spotLogs)
            val factors = if (spotLogs.size >= MIN_SAMPLES_FOR_FACTORS) {
                buildFactors(spotLogs, spotHitRate)
            } else {
                emptyList()
            }
            SpotForecast(
                spotName = spotName,
                totalTrips = spotLogs.size,
                hitRate = spotHitRate,
                baselineHitRate = overallHitRate,
                factors = factors
            )
        }
    }

    private fun hitRate(logs: List<CatchLogEntry>): Int {
        if (logs.isEmpty()) return 0
        val hits = logs.count { it.countFish > 0 }
        return Math.round(hits * 100f / logs.size)
    }

    private fun buildFactors(spotLogs: List<CatchLogEntry>, spotHitRate: Int): List<ForecastFactor> {
        val dims = listOf(
            "风向" to spotLogs.groupBy { it.weatherWindDir }.filterKeys { it.isNotBlank() && it != "无明显" },
            "天气" to spotLogs.groupBy { it.weatherSky }.filterKeys { it.isNotBlank() },
            "气压趋势" to spotLogs.groupBy { it.weatherPressureTrend }.filterKeys { it.isNotBlank() },
        )

        val factors = mutableListOf<ForecastFactor>()
        for ((dimension, groups) in dims) {
            val best = groups.entries
                .filter { it.value.size >= 2 }
                .maxByOrNull { hitRate(it.value) } ?: continue
            factors.add(
                ForecastFactor(
                    label = best.key,
                    dimension = dimension,
                    sampleHitRate = hitRate(best.value),
                    overallHitRate = spotHitRate,
                    sampleSize = best.value.size
                )
            )
        }
        return factors.sortedByDescending { it.contributionPercent }.take(3)
    }
}
