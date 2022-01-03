package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class FetchDataViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

    val user = User(FirebaseAuth.getInstance().currentUser!!.uid,"","")

    val finished = MutableLiveData("nothing")

    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()
    private val fbAuth = FirebaseAuth.getInstance()




    fun readFireStoreData(activity: Activity){
        finished.value = "Starting"
        val context = getApplication<Application>().applicationContext
        getUser()
        getSkillsFromFireBase(context,activity)


    }




    private fun getSkillsFromFireBase(context: Context,activity: Activity) {
        db.collection("skills").get().addOnSuccessListener {
            val skillsList = mutableListOf<Skill>()
            for(entry in it){
                finished.value = "Downloading Skill" + (skillsList.size+1) + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val desc = entry.data.getValue("description").toString()
                val type = entry.data.getValue("type").toString()
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val skill = Skill(id,name,desc,bitmap,type)
                skillsList.add(skill)
            }

            skillsList.forEach {    skillInList ->
                val pictureRef = fbStorage.reference.child("skillImages").child("${skillInList.skillId}.png")
                pictureRef.downloadUrl.addOnCompleteListener {
                    viewModelScope.launch {
                        if(it.isSuccessful){
                            skillInList.skillImage = getBitmap(it.result!!,context)
                        }
                        withContext(Dispatchers.IO){
                            Log.i("Debug","pridavam skill")
                            database.insert(skillInList)
                            if(skillInList == skillsList.last()){
                                getPredefinedTrainings(context)
                                getUsersTrainings(context)
                                getSkillsAndSkillCrossRefFromFireBase()
                                getUserAndSkillCrossRefFromFireBase(activity)
                            }
                        }
                    }
                }
            }
        }


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

    private fun getUser() {
        finished.value = "Getting User Info"
        db.collection("users").document(fbAuth.currentUser!!.uid).get().addOnSuccessListener{
            user.userEmail =  it.data?.getValue("userEmail").toString()
            user.userFullName = it.data?.getValue("userFullName").toString()
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    database.insertUser(user)
                }
            }

        }

    }

    private fun checkIfNewSkillsWereAdded(activity: Activity) {
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
                    Log.i("Debug_checkifnewskillsadded","added skill to firebase")
                }
            }

        }
        activity.runOnUiThread {
            finished.value = "All done"
        }


    }

    private fun getUsersTrainings(context: Context) {
        db.collection("trainings").whereEqualTo("owner",user.userId).get().addOnSuccessListener{
            val trainingList = mutableListOf<Training>()
            for(entry in it){
                finished.value = "Downloading custom training" + (trainingList.size+1) + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val training = Training(name,target,id,user.userId,bitmap,numberOfExercises)
                trainingList.add(training)
            }

            trainingList.forEach { trainingInList ->
                val pictureRef = fbStorage.reference.child("trainingImages").child("${trainingInList.id}.png")
                pictureRef.downloadUrl.addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            trainingInList.image = getBitmap(task.result!!, context)
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
            val trainingList = mutableListOf<Training>()
            for(entry in it){
                finished.value = "Downloading training" + (trainingList.size+1) + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val training = Training(name,target,id,"admin",bitmap,numberOfExercises)
                trainingList.add(training)
            }

            trainingList.forEach { trainingInList ->
                val pictureRef = fbStorage.reference.child("trainingImages").child("${trainingInList.id}.png")
                pictureRef.downloadUrl.addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            trainingInList.image = getBitmap(task.result!!, context)
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

    private fun getUserAndSkillCrossRefFromFireBase(activity: Activity) {
        var count = 0
        db.collection("userSkill").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener{
            val list  = mutableListOf<UserAndSkillCrossRef>()
            for (entry in it){
                finished.value = "Downloading userSkillCrossRef" + ++count + "/" + it.size()
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
                    checkIfNewSkillsWereAdded(activity)
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



    private suspend fun getBitmap(source: Uri, context: Context): Bitmap {
        val loading = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(source).build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
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