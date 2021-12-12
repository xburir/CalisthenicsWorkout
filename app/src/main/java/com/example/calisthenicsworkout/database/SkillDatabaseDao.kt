package com.example.calisthenicsworkout.database;

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.database.entities.UserAndSkillCrossRef
import com.example.calisthenicsworkout.database.relations.SkillWithSkills

@Dao
interface SkillDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(skill: Skill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM Skills ORDER BY skillId DESC")
    fun getALlSkills(): LiveData<List<Skill>>

    @Query("SELECT * FROM Skills WHERE skillid IN (SELECT childskillid FROM SkillAndSkillsCrossRef WHERE skillid = :key)")
    fun getALlBeforeSkills(key: String): List<Skill>

    @Query("SELECT * FROM Skills WHERE skillid IN (SELECT skillId FROM SkillAndSkillsCrossRef WHERE childskillid = :key)")
    fun getALlAfterSkills(key: String): List<Skill>

    @Query("SELECT * from Skills WHERE skillId = :key")
    fun getSkill(key: String): Skill

    @Query("SELECT * FROM skillandskillscrossref ORDER BY skillId DESC")
    fun getALlSkillCrossRefs(): LiveData<List<SkillAndSkillCrossRef>>

    @Query("SELECT `Minimal amount` FROM skillandskillscrossref WHERE skillId = :key AND childSkillId = :key2")
    fun getCrossRefAmount(key: String, key2: String): Int

    @Query("SELECT * FROM userandskillcrossref WHERE userId= :userId")
    fun getUserSkillCrossRefs(userId : String ): LiveData<List<UserAndSkillCrossRef>>

    @Query("SELECT * FROM userandskillcrossref WHERE userId = :user AND skillId = :skill")
    fun getUserAndSkillCrossRef(user: String, skill: String): UserAndSkillCrossRef



    @Delete
    fun deleteUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)



    @Update
    fun updateSkill(skill: Skill)

    @Update
    fun updateUser(user: User)

    @Update
    fun updateUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)





}