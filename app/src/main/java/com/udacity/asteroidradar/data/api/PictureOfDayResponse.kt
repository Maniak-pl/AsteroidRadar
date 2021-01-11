package com.udacity.asteroidradar.data.api

import com.squareup.moshi.Json

data class PictureOfDayResponse(
    @Json(name = "media_type") val mediaType: String,
    val title: String,
    val url: String
)