package com.udacity.asteroidradar.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.data.db.AsteroidEntity
import com.udacity.asteroidradar.data.db.PictureEntity

@Dao
interface AsteroidsDao {
    @Query("select * from AsteroidEntity order by closeApproachDate ASC")
    fun getAsteroids(): LiveData<List<AsteroidEntity>>

    @Query("select * from AsteroidEntity where closeApproachDate = :today")
    fun getAsteroidsFromToday(today: String): LiveData<List<AsteroidEntity>>

    @Query("select * from AsteroidEntity where closeApproachDate between :startDate and :endDate")
    fun getAsteroidsFromWeek(startDate: String, endDate: String): LiveData<List<AsteroidEntity>>

    @Query("delete from AsteroidEntity where closeApproachDate < :days")
    fun deleteOldAsteroids(days: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAsteroids(vararg asteroids: AsteroidEntity)
}

@Dao
interface PictureDao {
    @Query("select * from PictureEntity")
    fun getPicture(): LiveData<PictureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPicture(vararg picture: PictureEntity)
}

@Database(
    entities = [AsteroidEntity::class, PictureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NasaDatabase : RoomDatabase() {
    abstract val asteroidsDao: AsteroidsDao
    abstract val pictureDao: PictureDao
}

private lateinit var INSTANCE: NasaDatabase

fun getDatabase(context: Context): NasaDatabase {
    synchronized(NasaDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                NasaDatabase::class.java,
                "database"
            ).build()
        }
    }
    return INSTANCE
}