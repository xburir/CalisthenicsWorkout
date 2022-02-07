package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.util.PictureUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


class SkillViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    val allSkills = database.getALlSkills()
    val chosenSkillId = MutableLiveData<String>()
    lateinit var chosenSkill: Skill
    lateinit var beforeSkills: List<Skill>
    lateinit var afterSkills: List<Skill>
    var lastViewedSkillId = ""
    val finishedLoading = MutableLiveData(false)

    val userSkillCrossRefs = database.getUserSkillCrossRefs(FirebaseAuth.getInstance().currentUser!!.uid)

    val allTrainings = database.getALlTrainings()
    val chosenTrainingId = MutableLiveData<String>()
    var lastViewedTrainingId = ""
    lateinit var chosenTraining: Training

    val db = FirebaseFirestore.getInstance()
    val fbStorage = FirebaseStorage.getInstance()











    fun onSkillClicked(skillId: String) {
        finishedLoading.value = false
        chosenSkillId.value = skillId
        lastViewedSkillId = skillId

        CoroutineScope(IO).launch {
            chosenSkill = database.getSkill(skillId)
            beforeSkills = database.getALlBeforeSkills(skillId)
            afterSkills = database.getALlAfterSkills(skillId)

            beforeSkills.forEach { skillInList ->
                if(skillInList.skillType == "reps"){
                    skillInList.skillName = skillInList.skillName + " "+ database.getCrossRefAmount(skillId,skillInList.skillId).toString()+"x"
                }else{
                    skillInList.skillName = skillInList.skillName + " "+ database.getCrossRefAmount(skillId,skillInList.skillId).toString()+"s"
                }
            }
            CoroutineScope(Main).launch {
                finishedLoading.value = true
            }
        }
    }


    fun onSkillNavigated(){
        chosenSkillId.value = null

    }
    fun onTrainingClicked(trainingId: String) {
        chosenTrainingId.value = trainingId
        lastViewedTrainingId = trainingId
        finishedLoading.value = false
        CoroutineScope(IO).launch {
            chosenTraining = database.getTraining(trainingId)
        }
        CoroutineScope(Main).launch {
            finishedLoading.value = true
        }
    }
    fun onTrainingNavigated(){
        chosenTrainingId.value = null
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





    suspend fun addTrainingToDatabase(training: Training) {
        withContext(Dispatchers.IO){
            database.insertTraining(training)
        }
    }

    fun addSharedTraining(trainingId: String,context: Context){

        db.collection("trainings").get().addOnCompleteListener{
            if(it.isSuccessful){
                var found = false
                for(entry in it.result!!){
                    if(entry.id == trainingId){
                        found = true
                        val id = entry.id
                        val name = entry.data.getValue("name").toString()
                        val owner = entry.data.getValue("owner").toString()
                        val target = entry.data.getValue("target") as ArrayList<String>
                        val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                        val type = entry.data.getValue("type").toString()
                        val training = Training(name,target,id,owner,PictureUtil.getDefaultTrainingPic(),numberOfExercises,"0",type)
                        val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                        pictureRef.downloadUrl
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    it?.let{
                                        training.image = it
                                    }
                                    addTrainingToDatabase(training)
                                }
                            }
                            .addOnFailureListener {
                                viewModelScope.launch {
                                    addTrainingToDatabase(training)
                                }
                            }
                    }
                }
                if(!found){
                    Toast.makeText(context,"Training not found",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




}