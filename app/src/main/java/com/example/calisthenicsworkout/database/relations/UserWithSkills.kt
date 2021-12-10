package com.example.calisthenicsworkout.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.database.entities.UserAndSkillCrossRef


data class UserWithSkills(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "skillId",
        associateBy = Junction(UserAndSkillCrossRef::class)
    )
    val skills: List<Skill>
)