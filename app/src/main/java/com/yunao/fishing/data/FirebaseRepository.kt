package com.yunao.fishing.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
/**
 * 统一封装 Firebase Authentication + Cloud Firestore 的访问。
 * 所有个人数据都写在 users/{uid}/... 下面，只有本人能读写（见 Firestore 安全规则）；
 * 约钓（trips）是公开集合，登录用户都可读，发起人可写自己创建的记录。
 */
object FirebaseRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val currentUser: FirebaseUser? get() = auth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a -> trySend(a.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String, nickname: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return
        db.collection("users").document(uid)
            .set(UserProfile(nickname = nickname.ifBlank { "渔友" }, createdAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun signOut() = auth.signOut()

    suspend fun getMyNickname(): String {
        val uid = auth.currentUser?.uid ?: return "渔友"
        return try {
            db.collection("users").document(uid).get().await()
                .toObject(UserProfile::class.java)?.nickname?.ifBlank { "渔友" } ?: "渔友"
        } catch (e: Exception) {
            "渔友"
        }
    }

    // ---------- 出钓日志 ----------

    suspend fun addLog(entry: CatchLogEntry) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("logs").add(entry).await()
    }
    suspend fun deleteLog(id: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("logs").document(id).delete().await()
    }

    suspend fun getLogs(): List<CatchLogEntry> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("users").document(uid).collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
        return snap.documents.mapNotNull { d -> d.toObject(CatchLogEntry::class.java)?.apply { id = d.id } }
    }

    // ---------- 我的钓点 ----------

    suspend fun addSpot(spot: UserSpot) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("spots").add(spot).await()
    }
    suspend fun deleteSpot(id: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("spots").document(id).delete().await()
    }

    suspend fun getSpots(): List<UserSpot> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("users").document(uid).collection("spots")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
        return snap.documents.mapNotNull { d -> d.toObject(UserSpot::class.java)?.apply { id = d.id } }
    }

    // ---------- 我的装备 ----------

    suspend fun addGearItem(item: UserGearItem) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("gear").add(item).await()
    }
    suspend fun deleteGearItem(id: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("gear").document(id).delete().await()
    }

    suspend fun getGearItems(): List<UserGearItem> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("users").document(uid).collection("gear")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
        return snap.documents.mapNotNull { d -> d.toObject(UserGearItem::class.java)?.apply { id = d.id } }
    }

    // ---------- 约钓（公开集合） ----------

    suspend fun addTrip(trip: Trip) {
        db.collection("trips").add(trip).await()
    }

    suspend fun joinTrip(tripId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("trips").document(tripId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val trip = snap.toObject(Trip::class.java) ?: return@runTransaction
            if (uid !in trip.joinedUids && trip.joinedUids.size < trip.capacity) {

                tx.update(ref, "joinedUids", trip.joinedUids + uid)
            }
        }.await()
    }

    suspend fun getTrips(): List<Trip> {
        val snap = db.collection("trips")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(30).get().await()
        return snap.documents.mapNotNull { d -> d.toObject(Trip::class.java)?.apply { id = d.id } }
    }
}
