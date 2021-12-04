package com.example.calisthenicsworkout.database.entities

import androidx.room.*
import com.example.calisthenicsworkout.database.Converters
import com.google.gson.Gson

@Entity(tableName = "Skills")
data class Skill(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "skillId")
    var skillId: Long = 0L,

    @ColumnInfo(name = "skillName")
    var skillName: String,

)



