package com.example.calisthenicsworkout.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Trainings")
data class Training(
    val name: String,
    val target: String,

    @PrimaryKey(autoGenerate = false)
    val id: String
)