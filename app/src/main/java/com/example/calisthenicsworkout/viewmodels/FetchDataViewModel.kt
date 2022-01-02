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
        getSkillsFromFireBase(context)
        getSkillsAndSkillCrossRefFromFireBase()
        //getUserAndSkillCrossRefFromFireBase(activity)

    }




    private fun getSkillsFromFireBase(context: Context) {
        var count = 0
        db.collection("skills").get().addOnSuccessListener {
            for(entry in it){
                finished.value = "Downloading Skill" + ++count + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val desc = entry.data.getValue("description").toString()
                val type = entry.data.getValue("type").toString()
                val pictureRef = fbStorage.reference.child("skillImages").child("$id.png")
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val skill = Skill(id,name,desc,bitmap,type)
                pictureRef.downloadUrl.addOnCompleteListener {
                    viewModelScope.launch {
                        if(it.isSuccessful){
                            skill.skillImage = getBitmap(it.result!!,context)
                        }
                        withContext(Dispatchers.IO){
                            database.insert(skill)
                        }
                    }
                }
            }
            getPredefinedTrainings(context)
            getUsersTrainings(context)
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
                        database.getALlSkillsDirect().forEach { skillInAllSkills ->
                            if(skillInAllSkills.skillId == skillId){
                                val exercise = Exercise(trainingId,skillId,sets,reps,skillInAllSkills.skillImage,skillInAllSkills.skillName,order)
                                database.insertExercise(exercise)
                            }
                        }
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
                //db.collection("userAndSkillCrossRef").add(mappedThing)
            }

        }
        activity.runOnUiThread {
            finished.value = "All done"
        }


    }

    private fun getUsersTrainings(context: Context) {
        var count = 0
        db.collection("trainings").whereEqualTo("owner",user.userId).get().addOnSuccessListener{
            for(entry in it){
                finished.value = "Downloading custom training" + ++count + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val training = Training(name,target,id,user.userId,bitmap,numberOfExercises)
                pictureRef.downloadUrl.addOnCompleteListener { task->
                    viewModelScope.launch {
                        if(task.isSuccessful){
                            training.image = getBitmap(task.result!!,context)
                        }
                        withContext(Dispatchers.IO){
                            database.insertTraining(training)
                        }
                    }
                }
                getExercisesForTraining(id)
            }

        }

    }

    private fun getPredefinedTrainings(context: Context) {
        var count = 0
        db.collection("trainings").whereEqualTo("owner","admin").get().addOnSuccessListener{
            for(entry in it){
                finished.value = "Downloading training" + ++count + "/" + it.size()
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                val training = Training(name,target,id,"admin",bitmap,numberOfExercises)
                val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                pictureRef.downloadUrl.addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            training.image = getBitmap(task.result!!, context)
                        }
                        withContext(Dispatchers.IO) {
                            database.insertTraining(training)
                        }
                    }
                }
                getExercisesForTraining(id)
            }
        }
    }

    private fun getUserAndSkillCrossRefFromFireBase(activity: Activity) {
        var count = 0
        db.collection("userAndSkillCrossRef").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener{
            //TODO: maaybe tuto vytvorit nejake pole , do neho nahadzat vsetky najdene crossrefs, potom ich naraz pridat cez coroutine do databazy a potom po pridani skontrolovat nove skills
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