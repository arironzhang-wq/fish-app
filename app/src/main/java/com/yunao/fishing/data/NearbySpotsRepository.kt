package com.yunao.fishing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 附近钓场搜索：使用 OpenStreetMap 的免费 Overpass API，不需要注册账号/API Key。
 * 查询范围内标记为"钓场/垂钓区/渔具店/可垂钓水域"的 POI 点。
 * 国内部分地区 OSM 数据覆盖有限，搜不到属于正常情况。
 */
object NearbySpotsRepository {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val endpoints = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )

    suspend fun search(lat: Double, lon: Double, radiusMeters: Int = 8000): List<NearbySpot> =
        withContext(Dispatchers.IO) {
            val query = """
                [out:json][timeout:20];
                (
                  node["leisure"="fishing"](around:$radiusMeters,$lat,$lon);
                  way["leisure"="fishing"](around:$radiusMeters,$lat,$lon);
                  node["sport"="fishing"](around:$radiusMeters,$lat,$lon);
                  node["shop"="fishing"](around:$radiusMeters,$lat,$lon);
                  node["water"]["fishing"](around:$radiusMeters,$lat,$lon);
                );
                out center 40;
            """.trimIndent()

            for (endpoint in endpoints) {
                val result = runCatching { fetch(endpoint, query, lat, lon) }.getOrNull()
                if (result != null) return@withContext result
            }
            emptyList()
        }

    private fun fetch(endpoint: String, query: String, lat: Double, lon: Double): List<NearbySpot>? {
        val encoded = "data=" + URLEncoder.encode(query, "UTF-8")
        val body = encoded.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
        val request = Request.Builder().url(endpoint).post(body).build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val text = resp.body?.string() ?: return null
            val json = JSONObject(text)
            val elements = json.optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<NearbySpot>()
            for (i in 0 until elements.length()) {
                val el = elements.optJSONObject(i) ?: continue
                val elLat: Double
                val elLon: Double
                if (el.has("lat") && el.has("lon")) {
                    elLat = el.getDouble("lat")
                    elLon = el.getDouble("lon")
                } else {
                    val center = el.optJSONObject("center") ?: continue
                    elLat = center.optDouble("lat", Double.NaN)
                    elLon = center.optDouble("lon", Double.NaN)
                }
                if (elLat.isNaN() || elLon.isNaN()) continue

                val tags = el.optJSONObject("tags")
                val name = tags?.optString("name")?.takeIf { it.isNotBlank() } ?: "未命名钓点"
                val category = when {
                    tags?.optString("shop") == "fishing" -> "渔具店"
                    tags?.optString("leisure") == "fishing" -> "钓场"
                    tags?.optString("sport") == "fishing" -> "垂钓区"
                    else -> "可垂钓水域"
                }
                val distance = LocationHelper.distanceMeters(lat, lon, elLat, elLon)
                results.add(NearbySpot(name, category, elLat, elLon, distance))
            }
            return results.sortedBy { it.distanceMeters }.take(20)
        }
    }
}
