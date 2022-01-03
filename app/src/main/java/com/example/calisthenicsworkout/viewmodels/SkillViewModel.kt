package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*


class SkillViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    val allSkills = database.getALlSkills()
    val chosenSkillId = MutableLiveData<String>()
    var lastViewedSkillId = ""

    val userSkillCrossRefs = database.getUserSkillCrossRefs(FirebaseAuth.getInstance().currentUser!!.uid)

    val allTrainings = database.getALlTrainings()
    val chosenTrainingId = MutableLiveData<String>()
    var lastViewedTrainingId = ""





    suspend fun addTrainingToDatabase(training: Training) {
        withContext(Dispatchers.IO){
            database.insertTraining(training)
        }
    }

    suspend fun addExerciseToDatabase(exercise: Exercise){
        withContext(Dispatchers.IO){
            database.insertExercise(exercise)
        }
    }

    fun onSkillClicked(skillId: String) {
        chosenSkillId.value = skillId
        lastViewedSkillId = skillId
    }


    fun onSkillNavigated(){
        chosenSkillId.value = null

    }
    fun onTrainingClicked(trainingId: String) {
        chosenTrainingId.value = trainingId
        lastViewedTrainingId = trainingId
    }
    fun onTrainingNavigated(){
        chosenTrainingId.value = null
    }


    suspend fun addSkillToDatabase(skill: Skill){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.insert(skill)
            }
        }

    }

    suspend fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef){
        withContext(Dispatchers.IO){
            database.insertSkillAndSkillCrossRef(crossRef)
        }
    }






    fun userAndSkillCrossRef(userId: String, skillId: String, liked: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection("userSkill").whereEqualTo("userId",userId).whereEqualTo("skillId",skillId).get()
            .addOnSuccessListener{
                val id = it.documents[0].id
                db.collection("userSkill").document(id).update("liked",liked)
                viewModelScope.launch {
                    updateUserAndSkillCrossRef(userId,skillId,liked)
                }
        }

    }

    private suspend fun updateUserAndSkillCrossRef(userId: String, skillId: String, liked: Boolean) {
        withContext(Dispatchers.IO){
            val crossref = UserAndSkillCrossRef(userId,skillId, liked)
            database.updateUserAndSkillCrossRef(crossref)
        }
    }




}