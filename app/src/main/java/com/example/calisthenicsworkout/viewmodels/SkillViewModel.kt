package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.entities.UserAndSkillCrossRef
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.example.calisthenicsworkout.database.SkillDatabaseDao
import kotlinx.coroutines.*


class SkillViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    val allSkills = database.getALlSkills()
    val chosenSkillId = MutableLiveData<String>()
    var lastViewedSkillId = ""
    val usersFavSkills = database.getUsersFavoriteSkills(FirebaseAuth.getInstance().currentUser!!.uid)


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

    fun userAndSkillCrossRef(userId: String, skillId: String, mode : String) {
        viewModelScope.launch {
            if (mode == "add"){
                insertUserAndSkillCrossRef(userId,skillId)
            }else if (mode == "del"){
                deleteUserAndSkillCrossRef(userId,skillId)
            }

        }
    }

    private suspend fun deleteUserAndSkillCrossRef(userId: String, skillId: String) {
        withContext(Dispatchers.IO){
            database.deleteUserAndSkillCrossRef(database.getUserAndSkillCrossRef(userId,skillId))
        }
    }

    private suspend fun insertUserAndSkillCrossRef(userId: String, skillId: String) {
        withContext(Dispatchers.IO){
            database.insertUserAndSkillCrossRef(UserAndSkillCrossRef(userId,skillId))
        }
    }


}