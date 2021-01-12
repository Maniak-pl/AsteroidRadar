package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

enum class FilterType { WEEKLY, TODAY, SAVED }

class Repository(private val database: NasaDatabase) {

    private val filterType = MutableLiveData(FilterType.WEEKLY)

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.switchMap(filterType) { filter ->
            when (filter) {
                FilterType.TODAY ->
                    Transformations.map(database.asteroidsDao.getAsteroidsFromToday(DateHelper.getToday())) { it.toDomainModel() }
                FilterType.WEEKLY ->
                    Transformations.map(
                        database.asteroidsDao.getAsteroidsFromWeek(
                            DateHelper.getToday(),
                            DateHelper.getOneWeekAhead()
                        )
                    ) { it.toDomainModel() }
                FilterType.SAVED ->
                    Transformations.map(database.asteroidsDao.getAsteroids()) { it.toDomainModel() }
            }
        }

    val picture: LiveData<PictureOfDay> =
        Transformations.map(database.pictureDao.getPicture()) {
            it?.toDomainModel()
        }

    fun applyFilter(filter: FilterType) {
        filterType.value = filter
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