package com.example.calisthenicsworkout.fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.SkillRepository
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
//import com.example.calisthenicsworkout.database.SkillDatabaseDao
import kotlinx.coroutines.*
import timber.log.Timber


class SkillViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    val chosenSkill = MutableLiveData<String>()

    val allSkills = database.getALlSkills()
    private lateinit var lastSkill: LiveData<Skill>

//    val skillId = MutableLiveData(1L)
//    val skill = Transformations.map(skillId){
//        database.getSkill(it)
//    }

    init {
        Log.i("Debug","ViewModel created")
        //addTestData()


    }

    fun addSkillToDatabase(skill: Skill){
        viewModelScope.launch {
            suspendfunction(skill)
            lastSkill = database.getLastAddedSkill()
        }
    }
    suspend fun suspendfunction(skill: Skill){
        withContext(Dispatchers.IO){
//            database.insert(skill)

//            var skill = database.getSkill(4)
//            skill.skillDescription = "Lower yourself until your arms make 90° and then push yourself up so your arms are fully extended"
//            database.update(skill)

        }
    }

    override fun onCleared() {
        super.onCleared();
        Log.i("Debug","ViewModel cleared")
    }

    fun addTestData(){
        val skills = listOf(
            Skill(0,"Handstand",""),
            Skill(0,"Muscle up",""),
            Skill(0,"Pull Up",""),
            Skill(0,"Dip","")
        )
        val skillWithSkillsRelations = listOf(
            SkillAndSkillCrossRef(1,2,15),
            SkillAndSkillCrossRef(1,3,20)
        )

        skills.forEach{
            addSkillToDatabase(it)
        }
    }
}