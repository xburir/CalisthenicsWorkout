package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
//import com.example.calisthenicsworkout.database.SkillDatabaseDao
import kotlinx.coroutines.*


class SkillViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    val allSkills = database.getALlSkills()
    val chosenSkillId = MutableLiveData<Long>();
    //lateinit var allSkillsBeforeSkills: LiveData<List<Skill>>


    init {
        Log.i("Debug","ViewModel created")

    }

    fun onSkillClicked(skillId: Long) {
        chosenSkillId.value = skillId
   //     allSkillsBeforeSkills = database.getALlBeforeSkills(skillId)
    }
    fun onSkillNavigated(){
        chosenSkillId.value = null

    }

    fun addSkillToDatabase(skill: Skill){
        viewModelScope.launch {
            suspendfunction(skill)
        }
    }
    suspend fun suspendfunction(skill: Skill){
        withContext(Dispatchers.IO){
            database.insert(skill)

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
            Skill(0,"Knee Push up","Lower yourself until your chest nearly touches the floor end then extend your arms, with your knees on the floor instead of your feet"),
            Skill(0,"Back lever","Hang on the bar with your body being completely horizontal, with hands behind your back"),
            Skill(0,"Planche","Put your body in a horizontal position while being only on your hands"),
            Skill(0,"Pike Push up","Bend your body so your chest and legs make 90° like the letter L, do a pushup but instead of going down with your whole body, go directly down with your shoulders."),
            Skill(0,"Lsit","Elevate your legs until your body makes an L letter.")
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