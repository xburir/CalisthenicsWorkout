package com.example.calisthenicsworkout.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["skillId","childSkillId"],tableName = "SkillAndSkillsCrossRef")
data class SkillAndSkillCrossRef(
    val skillId: String,
    val childSkillId: String,
    @ColumnInfo(name = "Minimal amount")
    var minAmount: Int,
)