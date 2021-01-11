package com.udacity.asteroidradar.data.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AsteroidResponse(
    val id: Long, val codename: String, val closeApproachDate: String,
    val absoluteMagnitude: Double, val estimatedDiameter: Double,
    val relativeVelocity: Double, val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
) : Parcelable