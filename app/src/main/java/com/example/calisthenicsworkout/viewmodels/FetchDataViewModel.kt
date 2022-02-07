package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.CountDownTimer
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
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.net.URI
import com.google.firebase.firestore.FirebaseFirestoreSettings




class FetchDataViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    val finished = MutableLiveData("false")
    val TIMEOUT = 20000L
    lateinit var timer: CountDownTimer
    val timeLeft = MutableLiveData(0L)

    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()

    val skillsInDb = MutableLiveData(0)
    val trainingsInDb = MutableLiveData(0)

    val trainings = database.getALlTrainings()
    val skills = database.getALlSkills()







    suspend fun readFireStoreData(){




        val context = getApplication<Application>().applicationContext

        withContext(Main){
            timer = object : CountDownTimer(TIMEOUT,1000){
                override fun onFinish() {
                }
                override fun onTick(millisUnitlFinished: Long) {
                    timeLeft.value = millisUnitlFinished/1000
                }
            }.start()
        }


        val job  = withTimeoutOrNull(TIMEOUT){
            getSkillsFromFireBase(context)
            getPredefinedTrainings(context)
            getUsersTrainings(context)
            getSkillsAndSkillCrossRefFromFireBase()
            getUserAndSkillCrossRefFromFireBase()
            withContext(Main){
                finished.value = "true"
            }

        }
        if(job == null){
            withContext(Main){
                finished.value = "under"
            }

        }


    }




    private suspend fun getSkillsFromFireBase(context: Context) {
        val query = db.collection("skills").get().await()

        withContext(Main){
            skillsInDb.value = query.size()
        }

        for(entry in query){
            CoroutineScope(IO).launch {
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val desc = entry.data.getValue("description").toString()
                val type = entry.data.getValue("type").toString()
                val difficulty = entry.data.getValue("difficulty").toString().toInt()
                val target = entry.data.getValue("target") as ArrayList<String>
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val skill = Skill(id,name,desc,bitmap,type,target,difficulty)

                try {
                    val url = fbStorage.reference.child("skillImagesMini").child("${skill.skillId}.jpg").downloadUrl.await()
                    skill.skillImage = getBitmapFromUri(url, context)
                }catch (e: Exception){
                }finally {
                    Log.i("Debug","Adding skill")
                    database.insert(skill)
                }
            }
        }


    }


    private suspend fun getExercisesForTraining(training: String) {

        val query = db.collection("exercises").whereEqualTo("trainingId",training).get().await()
        for (entry in query){
            val skillId = entry.data.getValue("skillId").toString()
            val order = entry.data.getValue("order").toString().toInt()
            val trainingId = entry.data.getValue("trainingId").toString()
            val reps = entry.data.getValue("reps").toString()
            val sets = entry.data.getValue("sets").toString()
            withContext(IO){
                val skill = database.getSkill(skillId)
                val exercise = Exercise(trainingId,skillId,sets,reps,skill.skillImage,skill.skillName,order)
                Log.i("Debug","Adding exercise")
                database.insertExercise(exercise)
            }

        }

    }

    private fun checkIfNewSkillsWereAdded() {
        CoroutineScope(IO).launch {
            database.getALlSkillsDirect().forEach { skillInAllSkills ->
                var crossRefFound = false
                database.getUserSkillCrossRefsDirect(userId).forEach { userSkillCrossRef ->
                    if(userSkillCrossRef.skillId == skillInAllSkills.skillId){
                        crossRefFound = true
                    }
                }
                if(!crossRefFound){
                    val userId = userId
                    val skillId = skillInAllSkills.skillId
                    val liked = false
                    val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                    database.insertUserAndSkillCrossRef(crossRef)
                    val mappedThing: MutableMap<String,Any> = HashMap()
                    mappedThing["skillId"] = skillId
                    mappedThing["userId"] = userId
                    mappedThing["liked"] = liked
                    Log.i("Debug","crossref not found, adding ")
                    db.collection("userSkill").add(mappedThing)
                }
            }
        }



    }

    private suspend fun getUsersTrainings(context: Context) {

        val query = db.collection("trainings").whereEqualTo("owner",userId).get().await()

        CoroutineScope(Main).launch {
            trainingsInDb.value = trainingsInDb.value!! + query.size()
        }

        for(entry in query){
            CoroutineScope(IO).launch {
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target") as ArrayList<String>
                val type = entry.data.getValue("type").toString()
                val defaultPic = PictureUtil.getDefaultTrainingPic()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val training = Training(name,target,id,userId,defaultPic,numberOfExercises,"0",type)
                try{
                    val uri =  fbStorage.reference.child("trainingImages").child("${id}.png").downloadUrl.await()
                    val bitmap = getBitmapFromUri(uri,context)
                    val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,id)
                    training.image = savedImageUri
                }catch (e: Exception) {
                }finally {
                    Log.i("Debug","Adding training")
                    database.insertTraining(training)
                    getExercisesForTraining(id)
                }
            }
        }

    }

    private suspend fun getPredefinedTrainings(context: Context) {

        val query = db.collection("trainings").whereEqualTo("owner","admin").get().await()

        withContext(Main) {
            trainingsInDb.value = trainingsInDb.value!! +  query.size()
        }

        for (entry in query){
            CoroutineScope(IO).launch {
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target") as ArrayList<String>
                val type = entry.data.getValue("type").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val defaultPic = PictureUtil.getDefaultTrainingPic()
                val training = Training(name,target,id,"admin",defaultPic,numberOfExercises,"0",type)


                try{
                    val uri =  fbStorage.reference.child("trainingImages").child("${id}.png").downloadUrl.await()
                    val bitmap = getBitmapFromUri(uri,context)
                    val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,id)
                    training.image = savedImageUri
                }catch (e: Exception) {
                }finally {
                    Log.i("Debug","Adding training")
                    database.insertTraining(training)
                    getExercisesForTraining(id)
                }
            }


        }

    }

    private suspend fun getUserAndSkillCrossRefFromFireBase() {

        val query = db.collection("userSkill").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().await()
        for (entry in query){
            val userId = entry.data.getValue("userId").toString()
            val skillId = entry.data.getValue("skillId").toString()
            val liked = entry.data.getValue("liked").toString().toBoolean()
            val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
            withContext(IO){
                database.insertUserAndSkillCrossRef(crossRef)
            }

        }
        checkIfNewSkillsWereAdded()

    }

    private suspend fun getSkillsAndSkillCrossRefFromFireBase() {

        val query = db.collection("skillAndSkillsCrossRef").get().await()
        for (entry in query){
            val crossRef = SkillAndSkillCrossRef(
                entry.data.getValue("skillId").toString(),
                entry.data.getValue("childId").toString(),
                entry.data.getValue("amount").toString().toInt()
            )
            withContext(IO){
                database.insertSkillAndSkillCrossRef(crossRef)
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