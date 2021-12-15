package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Trainings")
data class Training(
    var name: String,
    var target: String,

    @PrimaryKey(autoGenerate = false)
    val id: String,
    var owner: String,
    var image: Bitmap,
    var numberOfExercises: Int
)