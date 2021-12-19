package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
//import com.example.calisthenicsworkout.database.SkillDatabaseDao
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext


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



    fun userAndSkillCrossRef(userId: String, skillId: String, mode : String) {
        viewModelScope.launch {
            when (mode) {
                "true" -> {
                    insertUserAndSkillCrossRef(userId,skillId,true)
                }
                "false" -> {
                    insertUserAndSkillCrossRef(userId,skillId,false)
                }
                "del" -> {
                    deleteUserAndSkillCrossRef(userId,skillId)
                }
                "setLiked" -> {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("userAndSkillCrossRef").whereEqualTo("userId",userId).whereEqualTo("skillId",skillId).get().addOnCompleteListener{
                        if(it.isSuccessful){
                            var entryId = ""
                            for(entry in it.result!!){
                                entryId = entry.id
                            }
                            db.collection("userAndSkillCrossRef").document(entryId).update("liked",true)
                        }
                    }
                    updateUserAndSkillCrossRef(userId,skillId,true)
                }
                "setUnliked" -> {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("userAndSkillCrossRef").whereEqualTo("userId",userId).whereEqualTo("skillId",skillId).get().addOnCompleteListener{
                        if(it.isSuccessful){
                            var entryId = ""
                            for(entry in it.result!!){
                                entryId = entry.id
                            }
                            db.collection("userAndSkillCrossRef").document(entryId).update("liked",false)
                        }
                    }
                    updateUserAndSkillCrossRef(userId,skillId,false)
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