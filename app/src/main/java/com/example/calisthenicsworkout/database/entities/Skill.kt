package com.example.calisthenicsworkout.database.entities

import android.graphics.Bitmap
import androidx.room.*

@Entity(tableName = "Skills")
data class Skill(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "skillId")
    var skillId: String,

    @ColumnInfo(name = "skillName")
    var skillName: String,

    @ColumnInfo(name = "skillDescription")
    var skillDescription: String,

    var skillImage: Bitmap,

    val skillType: String,

    var target: ArrayList<String>

)



