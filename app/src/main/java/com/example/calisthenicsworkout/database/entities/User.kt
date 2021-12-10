package com.example.calisthenicsworkout.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    var userId: String,

    var userEmail: String,

    var userFullName: String,
)