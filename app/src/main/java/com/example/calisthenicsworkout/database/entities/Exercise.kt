package com.example.calisthenicsworkout.database.entities

import androidx.room.Entity

@Entity(primaryKeys = ["trainingId","skillId"],tableName = "Exercises")
data class Exercise(
    val trainingId: String,
    val skillId: String,
    val sets: Int,
    val repetitions: Int
)