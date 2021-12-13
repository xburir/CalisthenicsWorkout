package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import androidx.room.Entity

@Entity(primaryKeys = ["trainingId","skillId"],tableName = "Exercises")
data class Exercise(
    val trainingId: String,
    val skillId: String,
    val sets: String,
    val repetitions: String,
    val skillImage: Bitmap,
    val skillName: String
)