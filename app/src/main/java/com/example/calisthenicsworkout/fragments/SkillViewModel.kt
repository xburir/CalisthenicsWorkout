package com.example.calisthenicsworkout.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import kotlinx.coroutines.*
import timber.log.Timber


class SkillViewModel(val database: SkillDatabaseDao,application: Application): AndroidViewModel(application) {


    val skills = database.getALlSkills()

    val skillId = MutableLiveData(1L)
    val skill = Transformations.map(skillId){
        database.getSkill(it)
    }

    init {

        val skills = listOf(
            Skill(0,"Handstand"),
            Skill(0,"Muscle up"),
            Skill(0,"Pull Up"),
            Skill(0,"Dip")
        )
        val skillWithSkillsRelations = listOf(
            SkillAndSkillCrossRef(1,2,15),
            SkillAndSkillCrossRef(1,3,20)
        )


        viewModelScope.launch {
            clear()
            addThingsToDatabase(skills,skillWithSkillsRelations)
        }
    }

    private suspend fun addThingsToDatabase(skills: List<Skill>, skillWithSkillsRelations: List<SkillAndSkillCrossRef>){
            skills.forEach{
                database.insert(it)
            }
            skillWithSkillsRelations.forEach {
                database.insertSkillAndSkillCrossRef(it)
            }


    }


    fun initializeSkill(id:Long){
        viewModelScope.launch {
//            skill.value = getSkillFromDatabase(id)
        }
    }



//    private suspend fun getSkillFromDatabase(key: Long): Skill? {
//        val skill = database.getSkill(key)
//
//    }

    fun addSkill(name:String){
        viewModelScope.launch {
            val newSkill = Skill(0,name);
            insert(newSkill);
        }

    }

    private suspend fun insert(skill: Skill){
        withContext(Dispatchers.IO){
            database.insert(skill);
        }
    }

    private suspend fun update(skill: Skill){
        withContext(Dispatchers.IO){
            database.update(skill)
        }
    }

    private fun clearDatabase(){
        viewModelScope.launch {
            clear();
        }
    }

    private suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clearSkillAndSkillCrossRefTable()
            database.clearSkillTable()
        }
    }

    override fun onCleared() {
        super.onCleared();
        Timber.i("Skill viewModel cleared");
    }
}