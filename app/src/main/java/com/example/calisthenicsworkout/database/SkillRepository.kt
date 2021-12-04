package com.example.calisthenicsworkout.database

import androidx.lifecycle.LiveData
import com.example.calisthenicsworkout.database.entities.Skill

class SkillRepository(private val skillDatabaseDao: SkillDatabaseDao) {
    val getAllSkills:  LiveData<List<Skill>> = skillDatabaseDao.getALlSkills()

    suspend fun addSkill(skill: Skill){
        skillDatabaseDao.insert(skill)
    }
}