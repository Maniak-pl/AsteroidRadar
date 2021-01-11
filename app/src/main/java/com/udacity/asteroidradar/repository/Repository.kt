package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.data.domain.Asteroid
import com.udacity.asteroidradar.data.domain.PictureOfDay
import com.udacity.asteroidradar.db.NasaDatabase
import com.udacity.asteroidradar.util.DateHelper
import com.udacity.asteroidradar.util.toDatabaseModel
import com.udacity.asteroidradar.util.toDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

class Repository(private val database: NasaDatabase) {

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidsDao.getAsteroidsFromToday(DateHelper.getToday())) { it.toDomainModel() }
    val picture: LiveData<PictureOfDay> =
        Transformations.map(database.pictureDao.getPicture()) {
            it?.toDomainModel()
        }

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            refreshAsteroids()
            refreshPicture()
        }
    }

    private suspend fun refreshAsteroids() {
        try {
            val result =
                Network.api.getAsteroids(DateHelper.getToday(), DateHelper.getOneWeekAhead())
            val asteroids = parseAsteroidsJsonResult(JSONObject(result))
            database.asteroidsDao.insertAsteroids(*asteroids.toDatabaseModel())
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Timber.e(e, "Refresh asteroids failed")
            }
        }
    }

    private suspend fun refreshPicture() {
        try {
            val result = Network.api.getPictureOfTheDay()
            database.pictureDao.insertPicture(result.toDatabaseModel())
        } catch (e: Exception) {
            Timber.e(e, "Refresh picture failed")
        }
    }

    suspend fun deleteOldAsteroids() {
        withContext(Dispatchers.IO) {
            database.asteroidsDao.deleteOldAsteroids(DateHelper.getToday())
        }
    }
}