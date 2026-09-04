package com.yunao.fishing.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * 离线本机版数据仓库：所有数据保存在设备本地（Room 数据库 + SharedPreferences），
 * 不需要注册/登录，也不依赖任何网络服务，适合国内直接使用。
 * 方法名尽量与之前的 FirebaseRepository 保持一致，方便以后需要时切回云端同步。
 */
object LocalRepository {

    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        db = AppDatabase.getInstance(context)
        prefs = context.applicationContext.getSharedPreferences("yunao_prefs", Context.MODE_PRIVATE)
    }

    data class LocalUser(val uid: String)

    val currentUser: LocalUser
        get() {
            var uid = prefs.getString(KEY_DEVICE_UID, null)
            if (uid == null) {
                uid = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_UID, uid).apply()
            }
            return LocalUser(uid)
        }

    fun getMyNickname(): String = prefs.getString(KEY_NICKNAME, null) ?: "渔友"

    fun setMyNickname(nickname: String) {
        prefs.edit().putString(KEY_NICKNAME, nickname.ifBlank { "渔友" }).apply()
    }

    suspend fun addLog(entry: CatchLogEntry) = db.catchLogDao().insert(entry)

    suspend fun deleteLog(id: String) = db.catchLogDao().deleteById(id)

    suspend fun getLogs(): List<CatchLogEntry> = db.catchLogDao().getAll()

    suspend fun addSpot(spot: UserSpot) = db.spotDao().insert(spot)

    suspend fun deleteSpot(id: String) = db.spotDao().deleteById(id)

    suspend fun getSpots(): List<UserSpot> = db.spotDao().getAll()

    suspend fun addGearItem(item: UserGearItem) = db.gearDao().insert(item)

    suspend fun deleteGearItem(id: String) = db.gearDao().deleteById(id)

    suspend fun getGearItems(): List<UserGearItem> = db.gearDao().getAll()

    suspend fun addTrip(trip: Trip) = db.tripDao().insert(trip)

    suspend fun joinTrip(tripId: String) {
        val uid = currentUser.uid
        val trip = db.tripDao().getById(tripId) ?: return
        if (uid !in trip.joinedUids && trip.joinedUids.size < trip.capacity) {
            db.tripDao().insert(trip.copy(joinedUids = trip.joinedUids + uid))
        }
    }

    suspend fun getTrips(): List<Trip> = db.tripDao().getAll()

    private const val KEY_DEVICE_UID = "device_uid"
    private const val KEY_NICKNAME = "nickname"
}

