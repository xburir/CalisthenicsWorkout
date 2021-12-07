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
    val chosenSkillId = MutableLiveData<String>();
    var lastViewedSkillId = ""


    init {
        Log.i("Debug","ViewModel created")


    }

    fun onSkillClicked(skillId: String) {
        chosenSkillId.value = skillId
        lastViewedSkillId = skillId
    }
    fun onSkillNavigated(){
        chosenSkillId.value = null

    }

    fun addSkillToDatabase(skill: Skill){
        viewModelScope.launch {
            insertSkillToDatabase(skill)
        }
    }
    suspend fun insertSkillToDatabase(skill: Skill){
        withContext(Dispatchers.IO){
            database.insert(skill)
        }
    }

    fun addSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef){
        viewModelScope.launch {
            insertSkillAndSkillCrossRef(crossRef)
        }
    }
    suspend fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef){
        withContext(Dispatchers.IO){
            database.insertSkillAndSkillCrossRef(crossRef)
        }
    }

    override fun onCleared() {
        super.onCleared();
        Log.i("Debug","ViewModel cleared")
    }

    suspend fun addData(){
        withContext(Dispatchers.IO){
            val skillWithSkillsRelations = listOf(
                SkillAndSkillCrossRef("diNPnsIMXFD77XS27c47","eZvOsXPA6xm78eH5pq4W", 10),
                SkillAndSkillCrossRef("diNPnsIMXFD77XS27c47","bUtOavsF1sgr0e2X4lVc", 10),
                SkillAndSkillCrossRef("bUtOavsF1sgr0e2X4lVc","mDrWpi7wfuxfW4fdNRDH", 5),
                SkillAndSkillCrossRef("XvyQCdNMgkxoumtsrAVY","eZvOsXPA6xm78eH5pq4W", 20),
                SkillAndSkillCrossRef("XvyQCdNMgkxoumtsrAVY","wJyxVbujrKQWhYFiWIqh", 30),
                SkillAndSkillCrossRef("XvyQCdNMgkxoumtsrAVY","NXVQJbsy3rhb312tOW3E", 50),
                SkillAndSkillCrossRef("mDrWpi7wfuxfW4fdNRDH","RV6ZgL068YaRdfBkffTv", 5),
                SkillAndSkillCrossRef("wcgFzEKmWHkGjjSjTJ0A","92DfStRxQd25WPujS74J", 20),
                SkillAndSkillCrossRef("wcgFzEKmWHkGjjSjTJ0A","NXVQJbsy3rhb312tOW3E", 50),
                SkillAndSkillCrossRef("wcgFzEKmWHkGjjSjTJ0A","wJyxVbujrKQWhYFiWIqh", 20),
                SkillAndSkillCrossRef("SD2dc0gnxKi7hWPHxN3U","wJyxVbujrKQWhYFiWIqh", 10),
                SkillAndSkillCrossRef("SD2dc0gnxKi7hWPHxN3U","NXVQJbsy3rhb312tOW3E", 30),
                SkillAndSkillCrossRef("wJyxVbujrKQWhYFiWIqh","NXVQJbsy3rhb312tOW3E", 30),
                SkillAndSkillCrossRef("92DfStRxQd25WPujS74J","mDrWpi7wfuxfW4fdNRDH", 5),
            )
            skillWithSkillsRelations.onEach {
                database.insertSkillAndSkillCrossRef(it)
            }
        }

    }


}