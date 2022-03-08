package com.example.calisthenicsworkout.database;

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.calisthenicsworkout.database.entities.*

@Dao
interface SkillDatabaseDao {
    //skill
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(skill: Skill)

    @Query("SELECT * FROM Skills ORDER BY skillId DESC")
    fun getALlSkillsDirect(): List<Skill>

    @Query("SELECT * FROM Skills ORDER BY skillId DESC")
    fun getALlSkills(): LiveData<List<Skill>>

    @Query("SELECT * FROM Skills WHERE skillid IN (SELECT childskillid FROM SkillAndSkillsCrossRef WHERE skillid = :key)")
    fun getALlBeforeSkills(key: String): List<Skill>

    @Query("SELECT * FROM Skills WHERE skillid IN (SELECT skillId FROM SkillAndSkillsCrossRef WHERE childskillid = :key)")
    fun getALlAfterSkills(key: String): List<Skill>

    @Query("SELECT * from Skills WHERE skillId = :key")
    fun getSkill(key: String): Skill

    @Update
    fun updateSkill(skill: Skill)

    @Query("DELETE FROM Skills")
    fun clearSkillsTable()



    //skill and skill cross ref
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef)

    @Query("SELECT * FROM skillandskillscrossref ORDER BY skillId DESC")
    fun getALlSkillCrossRefs(): LiveData<List<SkillAndSkillCrossRef>>

    @Query("SELECT `Minimal amount` FROM skillandskillscrossref WHERE skillId = :key AND childSkillId = :key2")
    fun getCrossRefAmount(key: String, key2: String): Int

    @Query("DELETE FROM skillandskillscrossref")
    fun clearSkillAndSkillsCrossRefTable()



    //user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUser(userId: String): LiveData<User>

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUserDirect(userId: String): User

    @Update
    fun updateUser(user: User)

    @Query("DELETE FROM User")
    fun clearUserTable()

    //user and skill cross ref
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)

    @Query("SELECT * FROM userandskillcrossref WHERE userId= :userId")
    fun getUserSkillCrossRefsDirect(userId : String ): List<UserAndSkillCrossRef>

    @Query("SELECT * FROM userandskillcrossref WHERE userId= :userId")
    fun getUserSkillCrossRefs(userId : String ): LiveData<List<UserAndSkillCrossRef>>

    @Query("SELECT * FROM userandskillcrossref WHERE userId = :user AND skillId = :skill")
    fun getUserAndSkillCrossRef(user: String, skill: String): UserAndSkillCrossRef

    @Delete
    fun deleteUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)

    @Update
    fun updateUserAndSkillCrossRef(crossRef: UserAndSkillCrossRef)

    @Query("DELETE FROM userandskillcrossref")
    fun clearUserAndSkillsTable()



    //training
    @Query("SELECT * FROM Trainings")
    fun getALlTrainings(): LiveData<List<Training>>

    @Query("SELECT * FROM Trainings")
    fun getALlTrainingsDirect(): List<Training>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTraining(training: Training)

    @Query("SELECT * from Trainings WHERE id = :key")
    fun getTraining(key: String): Training

    @Query("SELECT * from Trainings WHERE id = :key")
    fun getTrainingAsLiveData(key: String): LiveData<Training>

    @Query("DELETE FROM Trainings WHERE id = :trainingId")
    fun deleteTraining(trainingId: String)

    @Query("DELETE FROM Trainings")
    fun clearTrainingTable()


    //exercise
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM Exercises WHERE trainingId = :key")
    fun getExercisesOfTraining(key: String): LiveData<List<Exercise>>

    @Query("SELECT * FROM Exercises WHERE trainingId = :key")
    fun getExercisesOfTrainingDirect(key: String): List<Exercise>

    @Query("DELETE FROM exercises WHERE trainingId = :trainingId")
    fun deleteTrainingExercises(trainingId: String)

    @Query("DELETE FROM Exercises")
    fun clearExerciseTable()




}