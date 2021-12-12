package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import androidx.room.*
import com.example.calisthenicsworkout.database.Converters
import com.google.gson.Gson

@Entity(tableName = "Skills")
data class Skill(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "skillId")
    var skillId: String,

    @ColumnInfo(name = "skillName")
    var skillName: String,

    @ColumnInfo(name = "skillDescription")
    var skillDescription: String,

    val skillImage: Bitmap

)



