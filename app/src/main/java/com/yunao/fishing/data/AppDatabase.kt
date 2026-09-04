package com.yunao.fishing.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Dao
interface CatchLogDao {
    @Query("SELECT * FROM catch_logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<CatchLogEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CatchLogEntry)

    @Query("DELETE FROM catch_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots ORDER BY timestamp DESC")
    suspend fun getAll(): List<UserSpot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: UserSpot)

    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface GearDao {
    @Query("SELECT * FROM gear_items ORDER BY timestamp DESC")
    suspend fun getAll(): List<UserGearItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserGearItem)

    @Query("DELETE FROM gear_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY timestamp DESC LIMIT 30")
    suspend fun getAll(): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip)
}

@Database(
    entities = [CatchLogEntry::class, UserSpot::class, UserGearItem::class, Trip::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catchLogDao(): CatchLogDao
    abstract fun spotDao(): SpotDao
    abstract fun gearDao(): GearDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yunao.db"
                ).build().also { INSTANCE = it }
            }
    }
}

