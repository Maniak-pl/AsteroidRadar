package com.udacity.asteroidradar.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PictureEntity(
    @PrimaryKey
    val mediaType: String,
    val title: String,
    val url: String
)