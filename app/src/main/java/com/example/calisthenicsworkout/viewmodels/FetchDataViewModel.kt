package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.util.PictureUtil
import com.example.calisthenicsworkout.util.PictureUtil.Companion.getBitmapFromUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers.IO


class FetchDataViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    val finished = MutableLiveData("false")
    val TIMEOUT = 5000L
    lateinit var timer: CountDownTimer
    val timeLeft = MutableLiveData(0L)

    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()

    val skillsInDb = MutableLiveData(0)
    val trainingsInDb = MutableLiveData(0)
    val customTrainingsInDb = MutableLiveData(0)
    val exercisesInDb = MutableLiveData(0)
    val skillAndSkillCrossRefsInDb = MutableLiveData(0)
    val userAndSkillCrossRefsInDb = MutableLiveData(0)


    val skills = mutableListOf<Skill>()
    val trainings = mutableListOf<Training>()
    val exercises = mutableListOf<Exercise>()
    val beforeSkills = mutableListOf<SkillAndSkillCrossRef>()
    val userSkills = mutableListOf<UserAndSkillCrossRef>()

    val trainingsAdded = MutableLiveData(0)
    val customTrainingsAdded = MutableLiveData(0)
    val skillsAdded = MutableLiveData(0)
    val exercisesAdded = MutableLiveData(0)
    val beforeSkillsAdded = MutableLiveData(0)
    val userSkillsAdded = MutableLiveData(0)







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

        var downloadedSkills: QuerySnapshot
        var downloadedTrainings: QuerySnapshot
        var downloadedUserTrainings: QuerySnapshot
        var downloadedSkillAndSkillCrossRefs: QuerySnapshot
        var downloadedUserSkillCrossRefs: QuerySnapshot





        val downloadData  = withTimeoutOrNull(TIMEOUT){
            downloadedSkills = db.collection("skills").get().await()
            downloadedTrainings = db.collection("trainings").whereEqualTo("owner","admin").get().await()
            downloadedUserTrainings = db.collection("trainings").whereEqualTo("owner",userId).get().await()
            downloadedSkillAndSkillCrossRefs = db.collection("skillAndSkillsCrossRef").get().await()
            downloadedUserSkillCrossRefs = db.collection("userSkill").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().await()

            withContext(Main){
                skillsInDb.value = downloadedSkills.size()
                trainingsInDb.value = downloadedTrainings.size()
                customTrainingsInDb.value = downloadedUserTrainings.size()
                skillAndSkillCrossRefsInDb.value = downloadedSkillAndSkillCrossRefs.size()
                userAndSkillCrossRefsInDb.value = downloadedUserSkillCrossRefs.size()

                getSkills(downloadedSkills,context)
                getTrainings(downloadedTrainings)
                getTrainings(downloadedUserTrainings)
                getBeforeSkills(downloadedSkillAndSkillCrossRefs)
                getUserSkills(downloadedUserSkillCrossRefs)

            }



        }

        if(downloadData == null){
            withContext(Main){
                finished.value = "under"
            }

        }else{
            withContext(Main){
                finished.value = "downloaded"
            }
            downloadSkillsImages(context)
            downloadTrainingImages(context)
            addSkillsToDB()
            addTrainingsToDB()
            addExercisesToDB()
            addBeforeSkillsToDB()
            addUserSkillsToDb()
            withContext(Main){
                finished.value = "done"
            }
        }


    }

    private suspend fun addUserSkillsToDb() {
        userSkills.forEach { us->
            withContext(IO){
                database.insertUserAndSkillCrossRef(us)
            }
            withContext(Main){
                userSkillsAdded.value = userSkillsAdded.value?.plus(1);
            }
        }
    }

    private fun getUserSkills(downloadedUserSkillCrossRefs: QuerySnapshot) {

        for (entry in downloadedUserSkillCrossRefs){
            val userId = entry.data.getValue("userId").toString()
            val skillId = entry.data.getValue("skillId").toString()
            val liked = entry.data.getValue("liked").toString().toBoolean()
            val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
            userSkills.add(crossRef)
        }
        checkIfNewSkillsWereAdded()
    }

    private suspend fun addBeforeSkillsToDB() {
        beforeSkills.forEach { bs ->
            withContext(IO){
                database.insertSkillAndSkillCrossRef(bs)
            }
            withContext(Main){
                beforeSkillsAdded.value = beforeSkillsAdded.value?.plus(1);
            }
        }
    }

    private fun getBeforeSkills(query: QuerySnapshot) {
        for (entry in query){
            val crossRef = SkillAndSkillCrossRef(
                entry.data.getValue("skillId").toString(),
                entry.data.getValue("childId").toString(),
                entry.data.getValue("amount").toString().toInt()
            )
            beforeSkills.add(crossRef)
        }
    }

    private suspend fun addExercisesToDB() {
        trainings.forEach { tr ->
            val query = db.collection("exercises").whereEqualTo("trainingId",tr.id).get().await()
            withContext(Main){
                exercisesInDb.value = exercisesInDb.value?.plus(query.size())
            }
            for (entry in query){
                val skillId = entry.data.getValue("skillId").toString()
                val order = entry.data.getValue("order").toString().toInt()
                val trainingId = entry.data.getValue("trainingId").toString()
                val reps = entry.data.getValue("reps").toString()
                val sets = entry.data.getValue("sets").toString()

                skills.forEach { sk ->
                    if(sk.skillId == skillId){
                        val exercise = Exercise(entry.id,trainingId,skillId,sets,reps,sk.skillImage,sk.skillName,order)
                        withContext(IO){
                            database.insertExercise(exercise)
                        }
                        withContext(Main){
                            exercisesAdded.value = exercisesAdded.value?.plus(1)
                        }
                    }
                }
            }
        }

    }

    private suspend fun addTrainingsToDB() {
        trainings.forEach { tr ->
            withContext(IO){
                database.insertTraining(tr)
            }
            withContext(Main){
                if(tr.owner == "admin"){
                    trainingsAdded.value = trainingsAdded.value?.plus(1)
                }else{
                    customTrainingsAdded.value = customTrainingsAdded.value?.plus(1)
                }
            }
        }
    }

    private fun getTrainings(downloadedTrainings: QuerySnapshot) {
        for(entry in downloadedTrainings){
            val id = entry.id
            val name = entry.data.getValue("name").toString()
            val target = entry.data.getValue("target") as ArrayList<String>
            val type = entry.data.getValue("type").toString()
            val owner = entry.data.getValue("owner").toString()
            val defaultPic = PictureUtil.getDefaultTrainingPic()
            val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
            val training = Training(name,target,id,owner,defaultPic,numberOfExercises,"0",type)
            trainings.add(training)

        }
    }

    private suspend fun addSkillsToDB() {
        skills.forEach { sk ->
            withContext(IO){
                database.insert(sk)
            }
            withContext(Main){
                skillsAdded.value = skillsAdded.value?.plus(1);
            }
        }
    }


    private fun getSkills(query: QuerySnapshot,context: Context) {
        for(entry in query){
            val id = entry.id
            val name = entry.data.getValue("name").toString()
            val desc = entry.data.getValue("description").toString()
            val type = entry.data.getValue("type").toString()
            val difficulty = entry.data.getValue("difficulty").toString().toInt()
            val target = entry.data.getValue("target") as ArrayList<String>
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
            val skill = Skill(id,name,desc,bitmap,type,target,difficulty)
            skills.add(skill)
        }
    }


    private suspend fun downloadSkillsImages(context: Context) {
        for (i in 0 until skills.size) {
            CoroutineScope(IO).launch {
                try {
                    val url = fbStorage.reference.child("skillImagesMini").child("${skills[i].skillId}.jpg").downloadUrl.await()
                    skills[i].skillImage = getBitmapFromUri(url, context)
                }catch (e: Exception){
                }
            }

        }
    }

    private suspend fun downloadTrainingImages(context:Context){
        for(i in 0 until trainings.size){
            CoroutineScope(IO).launch {
                try{
                    val uri =  fbStorage.reference.child("trainingImages").child("${trainings[i].id}.png").downloadUrl.await()
                    val bitmap = getBitmapFromUri(uri,context)
                    val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,trainings[i].id)
                    trainings[i].image = savedImageUri
                }catch (e: Exception) {
                }
            }
        }

    }


     private fun checkIfNewSkillsWereAdded() {
         skills.forEach { skillInAllSkills ->
             var crossRefFound = false
             userSkills.forEach { userSkillCrossRef ->
                 if(userSkillCrossRef.skillId == skillInAllSkills.skillId){
                     crossRefFound = true
                 }
             }
             if(!crossRefFound){
                 val userId = userId
                 val skillId = skillInAllSkills.skillId
                 val liked = false
                 val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                 userSkills.add(crossRef)
                 val mappedThing: MutableMap<String,Any> = HashMap()
                 mappedThing["skillId"] = skillId
                 mappedThing["userId"] = userId
                 mappedThing["liked"] = liked
                 Log.i("Debug","crossref not found, adding ")
                 db.collection("userSkill").add(mappedThing)
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