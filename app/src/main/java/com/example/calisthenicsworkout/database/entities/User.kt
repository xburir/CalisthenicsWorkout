package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    var userId: String,

    var userEmail: String,

    var userFullName: String,

    var userImage: Uri,

    var points: Int
)