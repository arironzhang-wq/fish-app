package com.yunao.fishing.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 纯本地定位工具：只用系统自带的 LocationManager 读取 GPS/网络定位坐标，
 * 不依赖 Google Play 服务，也不需要联网就能拿到 GPS 坐标。
 * 调用前需要先确认已获得 ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION 权限。
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val providers = listOfNotNull(
            LocationManager.GPS_PROVIDER.takeIf { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) },
            LocationManager.NETWORK_PROVIDER.takeIf { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        )
        if (providers.isEmpty()) return null

        val lastKnown = providers
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        val fresh = withTimeoutOrNull(8000L) {
            suspendCancellableCoroutine<Location?> { cont ->
                var delivered = false
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!delivered) {
                            delivered = true
                            cont.resume(location)
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                try {
                    lm.requestSingleUpdate(providers.first(), listener, Looper.getMainLooper())
                } catch (e: Exception) {
                    if (!delivered) {
                        delivered = true
                        cont.resume(null)
                    }
                }
                cont.invokeOnCancellation {
                    runCatching { lm.removeUpdates(listener) }
                }
            }
        }

        return fresh ?: lastKnown
    }

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${meters.toInt()} m" else "%.1f km".format(meters / 1000)
}
