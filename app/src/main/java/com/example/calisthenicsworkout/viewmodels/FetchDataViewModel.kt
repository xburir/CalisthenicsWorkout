package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.util.PictureUtil
import com.example.calisthenicsworkout.util.PictureUtil.Companion.getBitmapFromUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchDataViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

    val user = User(FirebaseAuth.getInstance().currentUser!!.uid,"","", PictureUtil.getDefaultProfilePic())

    val finished = MutableLiveData("nothing")

    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()
    private val fbAuth = FirebaseAuth.getInstance()

    val skillsInDb = MutableLiveData(0)
    val trainingsInDb = MutableLiveData(0)

    val trainings = database.getALlTrainings()
    val skills = database.getALlSkills()

    val userInfo = MutableLiveData(false)




    fun readFireStoreData(){
        val context = getApplication<Application>().applicationContext
        getUser(context)
        getSkillsFromFireBase(context)
        skillsInDb.value = 0
        trainingsInDb.value = 0
        userInfo.value = false
    }




    private fun getSkillsFromFireBase(context: Context) {
        db.collection("skills").get().addOnSuccessListener { query ->
            skillsInDb.value = query.size()
            val skillsList = mutableListOf<Skill>()
            for(entry in query){
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val desc = entry.data.getValue("description").toString()
                val type = entry.data.getValue("type").toString()
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val skill = Skill(id,name,desc,bitmap,type)
                skillsList.add(skill)
            }

            skillsList.forEach {    skillInList ->
                val pictureRef = fbStorage.reference.child("skillImagesMini").child("${skillInList.skillId}.jpg")
                pictureRef.downloadUrl
                    .addOnCompleteListener {
                        viewModelScope.launch{
                            if(it.isSuccessful){
                                skillInList.skillImage = getBitmapFromUri(it.result!!,context)
                            }
                            withContext(Dispatchers.IO){
                                Log.i("Debug","pridavam skill "+ skillInList.skillName)
                                database.insert(skillInList)
                                if(skillInList == skillsList.last()){
                                    finishedSkillsDownloading(context)
                                }
                            }
                        }
                }
            }
        }
    }

    private fun finishedSkillsDownloading(context: Context) {
        getPredefinedTrainings(context)
        getUsersTrainings(context)
        getSkillsAndSkillCrossRefFromFireBase()
        getUserAndSkillCrossRefFromFireBase()

    }

    private fun getExercisesForTraining(training: String) {
        db.collection("exercises").whereEqualTo("trainingId",training).get().addOnSuccessListener{
            for(entry in it){
                val skillId = entry.data.getValue("skillId").toString()
                val order = entry.data.getValue("order").toString().toInt()
                val trainingId = entry.data.getValue("trainingId").toString()
                val reps = entry.data.getValue("reps").toString()
                val sets = entry.data.getValue("sets").toString()

                viewModelScope.launch {
                    withContext(Dispatchers.IO){
                        val skill = database.getSkill(skillId)
                        val exercise = Exercise(trainingId,skillId,sets,reps,skill.skillImage,skill.skillName,order)
                        Log.i("Debug","pridavam cvicenie pre trening "+training)
                        database.insertExercise(exercise)
                    }
                }
            }
        }
    }

    private fun getUser(context: Context) {
        db.collection("users").document(fbAuth.currentUser!!.uid).get().addOnSuccessListener{
            userInfo.value = true
            user.userEmail =  it.data?.getValue("userEmail").toString()
            user.userFullName = it.data?.getValue("userFullName").toString()



            val pictureRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child("${user.userId}.png")
            pictureRef.downloadUrl.addOnCompleteListener{ task ->
                viewModelScope.launch {
                    if(task.isSuccessful){
                        val bitmap = getBitmapFromUri(task.result!!,context)
                        val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,user.userId)
                        user.userImage = savedImageUri

                    }
                    withContext(Dispatchers.IO){
                        database.insertUser(user)
                    }
                }
            }

        }


    }

    private fun checkIfNewSkillsWereAdded() {
        database.getALlSkillsDirect().forEach { skillInAllSkills ->
            var crossRefFound = false
            database.getUserSkillCrossRefsDirect(user.userId).forEach { userSkillCrossRef ->
                if(userSkillCrossRef.skillId == skillInAllSkills.skillId){
                    crossRefFound = true
                }
            }
            if(!crossRefFound){
                val userId = user.userId
                val skillId = skillInAllSkills.skillId
                val liked = false
                val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                database.insertUserAndSkillCrossRef(crossRef)
                val mappedThing: MutableMap<String,Any> = HashMap()
                mappedThing["skillId"] = skillId
                mappedThing["userId"] = userId
                mappedThing["liked"] = liked
                Log.i("Debug","crossref not found, adding ")
                db.collection("userSkill").add(mappedThing).addOnSuccessListener {
                    Log.i("Debug","added skill to firebase")
                }
            }
        }


    }

    private fun getUsersTrainings(context: Context) {
        db.collection("trainings").whereEqualTo("owner",user.userId).get().addOnSuccessListener{
            val trainingList = mutableListOf<Training>()
            trainingsInDb.value = trainingsInDb.value!! +  it.size()
            for(entry in it){
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val type = entry.data.getValue("type").toString()
                val defaultPic = PictureUtil.getDefaultTrainingPic()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val training = Training(name,target,id,user.userId,defaultPic,numberOfExercises,"0",type)



                trainingList.add(training)
            }

            trainingList.forEach { trainingInList ->
                val pictureRef = fbStorage.reference.child("trainingImages").child("${trainingInList.id}.png")
                pictureRef.downloadUrl.addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            val bitmap = getBitmapFromUri(task.result!!,context)
                            val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,trainingInList.id)
                            trainingInList.image = savedImageUri
                        }
                        withContext(Dispatchers.IO) {
                            Log.i("Debug","pridavam trening "+trainingInList.id)
                            database.insertTraining(trainingInList)
                            getExercisesForTraining(trainingInList.id)
                        }
                    }
                }
            }

        }

    }

    private fun getPredefinedTrainings(context: Context) {
        db.collection("trainings").whereEqualTo("owner","admin").get().addOnSuccessListener{
            trainingsInDb.value = trainingsInDb.value!! +  it.size()
            val trainingList = mutableListOf<Training>()
            for(entry in it){
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val type = entry.data.getValue("type").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val defaultPic = PictureUtil.getDefaultTrainingPic()
                val training = Training(name,target,id,"admin",defaultPic,numberOfExercises,"0",type)
                trainingList.add(training)
            }

            trainingList.forEach { trainingInList ->
                val pictureRef = fbStorage.reference.child("trainingImages").child("${trainingInList.id}.png")
                pictureRef.downloadUrl.addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            val bitmap = getBitmapFromUri(task.result!!,context)
                            val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,trainingInList.id)
                            trainingInList.image = savedImageUri
                        }
                        withContext(Dispatchers.IO) {
                            Log.i("Debug","pridavam trening "+trainingInList.id)
                            database.insertTraining(trainingInList)
                            getExercisesForTraining(trainingInList.id)
                        }
                    }
                }
            }


        }
    }

    private fun getUserAndSkillCrossRefFromFireBase() {
        db.collection("userSkill").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener{
            val list  = mutableListOf<UserAndSkillCrossRef>()
            for (entry in it){
                val userId = entry.data.getValue("userId").toString()
                val skillId = entry.data.getValue("skillId").toString()
                val liked = entry.data.getValue("liked").toString().toBoolean()
                val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                list.add(crossRef)
            }
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    list.forEach {  crossRef ->
                        Log.i("Debug","Adding userAndSkillCrossRef")
                        database.insertUserAndSkillCrossRef(crossRef)
                    }
                    checkIfNewSkillsWereAdded()
                }
            }

        }
    }

    private fun getSkillsAndSkillCrossRefFromFireBase() {
        db.collection("skillAndSkillsCrossRef").get().addOnSuccessListener{
            for(entry in it){
                val crossRef = SkillAndSkillCrossRef(
                    entry.data.getValue("skillId").toString(),
                    entry.data.getValue("childId").toString(),
                    entry.data.getValue("amount").toString().toInt()
                )
                viewModelScope.launch {
                    withContext(Dispatchers.IO){
                        Log.i("Debug","Adding crossref for skill"+crossRef.skillId)
                        database.insertSkillAndSkillCrossRef(crossRef)
                    }
                }
            }
        }
    }






    fun saveFireStore(crossRef: SkillAndSkillCrossRef){
        val db = FirebaseFirestore.getInstance()
        val mappedThing: MutableMap<String,Any> = HashMap()
        mappedThing["skillId"] = crossRef.skillId
        mappedThing["childId"] = crossRef.childSkillId
        mappedThing["amount"] = crossRef.minAmount



        db.collection("skillAndSkillsCrossRef").add(mappedThing)
            .addOnSuccessListener {
                Log.i("Debug","added succesfully")
            }
            .addOnFailureListener{
                Log.i("Debug","not added")
            }
    }
}