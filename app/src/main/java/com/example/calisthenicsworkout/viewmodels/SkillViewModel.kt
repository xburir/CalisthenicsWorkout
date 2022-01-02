package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.util.Log
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


    suspend fun insertSkillToDatabase(skill: Skill){
        withContext(Dispatchers.IO){
            database.insert(skill)
        }
    }

    suspend fun insertSkillAndSkillCrossRef(crossRef: SkillAndSkillCrossRef){
        withContext(Dispatchers.IO){
            database.insertSkillAndSkillCrossRef(crossRef)
        }
    }

    suspend fun insertExercise(exercise: Exercise){
        withContext(Dispatchers.IO){
            database.insertExercise(exercise)
        }
    }

    suspend fun insertUser(user: User){
        withContext(Dispatchers.IO){
            database.insertUser(user)
        }
    }


    fun userAndSkillCrossRef(userId: String, skillId: String, mode : String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("userAndSkillCrossRef").whereEqualTo("userId",userId).whereEqualTo("skillId",skillId).get().addOnSuccessListener{
            Log.i("Debug","I got "+it.documents.size+" entries")
            //val id = it.documents[0].id
            when (mode) {
                "setLiked" -> {
                    //TODO: check if this is not adding too much things to database
                    //db.collection("userAndSkillCrossRef").document(id).update("liked",true)
                    viewModelScope.launch {
                        updateUserAndSkillCrossRef(userId,skillId,true)
                    }
                }
                "setUnliked" -> {
                    //TODO: check if this is not adding too much things to database
                    //db.collection("userAndSkillCrossRef").document(id).update("liked",false)
                    viewModelScope.launch {
                        updateUserAndSkillCrossRef(userId,skillId,false)
                    }

                }
            }
        }

    }

    private suspend fun updateUserAndSkillCrossRef(userId: String, skillId: String, liked: Boolean) {
        withContext(Dispatchers.IO){
            val crossref = UserAndSkillCrossRef(userId,skillId, liked)
            database.updateUserAndSkillCrossRef(crossref)
        }
    }

    private suspend fun deleteUserAndSkillCrossRef(userId: String, skillId: String) {
        withContext(Dispatchers.IO){
            database.deleteUserAndSkillCrossRef(database.getUserAndSkillCrossRef(userId,skillId))
        }
    }

    private suspend fun insertUserAndSkillCrossRef(userId: String, skillId: String, liked: Boolean) {
        withContext(Dispatchers.IO){
            database.insertUserAndSkillCrossRef(UserAndSkillCrossRef(userId,skillId,liked))
        }
    }


}