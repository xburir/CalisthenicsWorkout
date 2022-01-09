package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.util.PictureUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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



    fun saveTraining(training: Training,context: Context,imgUrl: String,exerciseList: MutableList<Exercise>) {
        viewModelScope.launch {
            if(imgUrl.isNotEmpty()){
                val bmp = PictureUtil.getBitmapFromUri(Uri.parse(imgUrl), context)
                val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bmp,context,training.id)
                training.image = savedImageUri
                FirebaseStorage.getInstance().reference.child("trainingImages").child("${training.id}.png").putFile(Uri.parse(imgUrl))
            }
            addTrainingToDatabase(training)
            exerciseList.forEach {
                addExerciseToDatabase(it)
            }
            saveTrainingToFirestore(training,exerciseList,context)
        }
    }

    private fun saveTrainingToFirestore(training: Training, exerciseList: MutableList<Exercise>, context: Context) {
        val database = FirebaseFirestore.getInstance()
        val mappedTraining: MutableMap<String,Any> = HashMap()
        mappedTraining["name"] = training.name
        mappedTraining["owner"] = training.owner
        mappedTraining["numberOfExercises"] = training.numberOfExercises
        mappedTraining["target"] = training.target
        database.collection("trainings").document(training.id).set(mappedTraining)
            .addOnSuccessListener {
                lastViewedTrainingId = training.id
            }
            .addOnFailureListener {
                Toast.makeText(context,"Saving training to online database failed, upload it later with good internet connection",Toast.LENGTH_SHORT).show()
            }
        exerciseList.forEach{
            val mappedExercise: MutableMap<String,Any> = HashMap()
            mappedExercise["reps"] = it.repetitions
            mappedExercise["sets"] = it.sets
            mappedExercise["order"] = it.order
            mappedExercise["skillId"] = it.skillId
            mappedExercise["trainingId"] = it.trainingId
            database.collection("exercises").add(mappedExercise)
        }

    }

    fun addSharedTraining(trainingId: String,context: Context){
        val db = FirebaseFirestore.getInstance()
        val fbStorage = FirebaseStorage.getInstance()
        db.collection("trainings").get().addOnCompleteListener{
            if(it.isSuccessful){
                var found = false
                for(entry in it.result!!){
                    if(entry.id == trainingId){
                        found = true
                        val id = entry.id
                        val name = entry.data.getValue("name").toString()
                        val owner = entry.data.getValue("owner").toString()
                        val target = entry.data.getValue("target").toString()
                        val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                        val training = Training(name,target,id,owner,PictureUtil.getDefaultTrainingPic(),numberOfExercises)
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