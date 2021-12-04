package com.example.calisthenicsworkout.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef


data class SkillWithSkills(
    @Embedded val skill: Skill,
    @Relation(
        parentColumn = "skillId",
        entityColumn = "childSkillId",
        associateBy = Junction(SkillAndSkillCrossRef::class)
    )
    @Embedded val childSkill: Skill
)