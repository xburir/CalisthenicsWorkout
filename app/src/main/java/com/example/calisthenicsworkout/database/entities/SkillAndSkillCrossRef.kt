package com.example.calisthenicsworkout.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["skillId","childSkillId"],tableName = "SkillAndSkillsCrossRef")
data class SkillAndSkillCrossRef(
    val skillId: Long,
    val childSkillId: Long,
    @ColumnInfo(name = "Minimal amount")
    var minAmount: Int,
)