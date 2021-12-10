package com.example.calisthenicsworkout.database.entities

import androidx.room.Entity


@Entity(primaryKeys = ["userId","skillId"])
data class UserAndSkillCrossRef(
    val userId: String,
    val skillId: String

)