package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val trainingId: String,
    val skillId: String,
    val sets: String,
    val repetitions: String,
    val skillImage: Bitmap,
    val skillName: String,
    val order: Int
)