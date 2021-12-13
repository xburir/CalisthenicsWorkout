package com.example.calisthenicsworkout.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.calisthenicsworkout.database.entities.*

data class TrainingWithSkills(
    @Embedded val training: Training,
    @Relation(
        parentColumn = "trainingId",
        entityColumn = "skillId",
        associateBy = Junction(Exercise::class)
    )
    val skills: List<Skill>
)
