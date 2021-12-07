package com.example.calisthenicsworkout.database;

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.relations.SkillWithSkills

@Dao
interface SkillDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(skill: Skill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef)

    @Query("SELECT * FROM Skills ORDER BY skillId DESC")
    fun getALlSkills(): LiveData<List<Skill>>

    @Query("SELECT * FROM Skills WHERE skillid IN (SELECT childskillid FROM SkillAndSkillsCrossRef WHERE skillid = :key)")
    fun getALlBeforeSkills(key: Long): List<Skill>

    @Query("SELECT * from Skills WHERE skillId = :key")
    fun getSkill(key: Long): Skill

    @Query("SELECT * from Skills ORDER BY skillId DESC LIMIT 1")
    fun getLastAddedSkill(): LiveData<Skill>

    @Delete
    fun delete(skill: Skill)

    @Query("DELETE  FROM Skills")
    fun clearSkillTable()

    @Query("DELETE  FROM SkillAndSkillsCrossRef")
    fun clearSkillAndSkillCrossRefTable()

    @Update
    fun update(skill: Skill)





}