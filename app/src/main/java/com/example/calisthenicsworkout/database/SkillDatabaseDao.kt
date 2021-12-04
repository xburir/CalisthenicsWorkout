package com.example.calisthenicsworkout.database;

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.relations.SkillWithSkills

@Dao
interface SkillDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: Skill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef)

    @Query("SELECT * FROM Skills ORDER BY skillId DESC")
    fun getALlSkills(): LiveData<List<Skill>> //MutableLiveData<MutableList<Skill>>

    @Query("SELECT * from Skills WHERE skillId = :key")
    fun getSkill(key: Long): LiveData<Skill>


    @Transaction
    @Query("SELECT * FROM SkillAndSkillsCrossRef WHERE skillId=:skillId")
    suspend fun getBeforeSkillsOfSkill(skillId: Long): List<SkillWithSkills>


    @Delete
    fun delete(skill: Skill)

    @Query("DELETE  FROM Skills")
    fun clearSkillTable()

    @Query("DELETE  FROM SkillAndSkillsCrossRef")
    fun clearSkillAndSkillCrossRefTable()

    @Update
    fun update(skill: Skill)





}