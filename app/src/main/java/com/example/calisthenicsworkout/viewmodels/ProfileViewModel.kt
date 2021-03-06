package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.util.PictureUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class ProfileViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {



    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()

    val allUsers = MutableLiveData(mutableListOf<User>())
    val allUsersDirect = mutableListOf<User>()
    val downloadProgress = MutableLiveData(0L)
    val chosenUsersTrainings = MutableLiveData(mutableListOf<Training>())
    var trainings = mutableListOf<Training>()

    val chosenUser = MutableLiveData<User>()
    var chosenUserId = ""


    var currentUser = database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)

    val uploadProgress = MutableLiveData(0L)


    init {
        val context = application.applicationContext
        CoroutineScope(IO).launch {
            val user = database.getUserDirect(FirebaseAuth.getInstance().currentUser!!.uid)
            if(user == null){
                db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
                    val name = it.data?.getValue("userFullName").toString()
                    val email = it.data?.getValue("userEmail").toString()
                    val id = it.id
                    val points = it.data?.getValue("userPoints").toString()
                    val user =  User(id,email,name,PictureUtil.getDefaultProfilePic(),points.toInt())
                    fbStorage.reference.child("userProfileImages").child("$id.png").downloadUrl.addOnCompleteListener {
                        viewModelScope.launch {
                            if(it.isSuccessful){
                                val bitmap = PictureUtil.getBitmapFromUri(it.result!!, context)
                                val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,id)
                                user.userImage = savedImageUri

                            }
                            withContext(IO){
                                database.insertUser(user)
                                currentUser = database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)
                            }

                        }
                    }

                }
            }
        }
    }

    fun getAllUsers() {
        val context = getApplication<Application>().applicationContext
        db.collection("users").get().addOnSuccessListener { query ->
            for (entry in query){
                val id = entry.id
                val fullName = entry.data.getValue("userFullName").toString()
                val email = entry.data.getValue("userEmail").toString()
                val points = entry.data.getValue("userPoints").toString().toInt()
                val pictureRef = fbStorage.reference.child("userProfileImages").child("${id}.png")
                pictureRef.downloadUrl
                    .addOnFailureListener {
                        val user = User(id,email,fullName, PictureUtil.getDefaultProfilePic(),points)
                        allUsersDirect.add(user)
                        allUsers.value = allUsersDirect
                        downloadProgress.value = 100L * (allUsersDirect.size / query.size())
                    }
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            val bmp = PictureUtil.getBitmapFromUri(it,context)
                            val url = PictureUtil.saveBitmapToInternalStorage(bmp,context,id)
                            val user = User(id,email,fullName,url,points)
                            allUsersDirect.add(user)
                            allUsers.value = allUsersDirect
                            downloadProgress.value = 100L * (allUsersDirect.size / query.size())
                        }
                    }
            }
        }
    }

    fun setUser(userId: String) {
        allUsersDirect.forEach {
            if(it.userId == userId){
                chosenUser.value = it
            }
        }
    }

    fun logout(intent: Intent, activity: Activity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.clearExerciseTable()
                database.clearSkillAndSkillsCrossRefTable()
                database.clearUserTable()
                database.clearSkillsTable()
                database.clearTrainingTable()
                database.clearUserAndSkillsTable()

                FirebaseAuth.getInstance().signOut()
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    fun saveProfilePic(uri: Uri, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        viewModelScope.launch {
            val bmp = PictureUtil.getBitmapFromUri(uri, context)
            val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bmp,context,userId)

            withContext(IO){
                val  changedUser = database.getUserDirect(userId)
                changedUser.userImage = savedImageUri
                database.insertUser(changedUser)
                currentUser = database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)
            }
            FirebaseStorage.getInstance().reference.child("userProfileImages").child("$userId.png").putFile(uri)
                .addOnProgressListener {
                    uploadProgress.value = (100*it.bytesTransferred/it.totalByteCount)
                }

        }
    }

    fun unregister(intent: Intent,activity: Activity) {
        val auth = FirebaseAuth.getInstance()
        val currUserId = auth.currentUser!!.uid

        deleteUserImage(currUserId,intent, activity)






    }

    private fun deleteUserSkillCrossRefs(currUserId: String, intent: Intent,activity: Activity) {
        db.collection("userSkill").whereEqualTo("userId",currUserId).get().addOnSuccessListener {   userSkillQuery->
            for(entry in userSkillQuery){
                db.collection("userSkill").document(entry.id).delete().addOnCompleteListener {
                    Log.i("Debug","Deleting User Skill Cross Ref")
                    if(entry == userSkillQuery.last()){
                        deleteTrainings(currUserId,intent, activity)
                    }
                }
            }
            if(userSkillQuery.isEmpty){
                deleteTrainings(currUserId,intent, activity)
            }
        }
    }

    private fun deleteTrainings(currUserId: String, intent: Intent,activity: Activity) {
        db.collection("trainings").whereEqualTo("owner",currUserId).get().addOnSuccessListener { trainingQuery ->
            for (trainingEntry in trainingQuery){
                val trainingId = trainingEntry.id
                fbStorage.reference.child("trainingImages").child("$trainingId.png").delete().addOnCompleteListener {
                    db.collection("exercises").whereEqualTo("trainingId",trainingId).get().addOnSuccessListener { exerciseQuery ->
                        for (exerciseEntry in exerciseQuery){
                            db.collection("exercises").document(exerciseEntry.id).delete().addOnCompleteListener {
                                Log.i("Debug","Deleting exercise")
                                if(exerciseEntry == exerciseQuery.last()){
                                    db.collection("trainings").document(trainingId).delete().addOnCompleteListener {
                                        Log.i("Debug","Deleting training")
                                        FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener {
                                            Log.i("Debug","Deleting user")
                                            logout(intent,activity)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(trainingQuery.isEmpty){
                FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener {
                    Log.i("Debug","Deleting user")
                    logout(intent,activity)
                }
            }
        }
    }

    private fun deleteUserImage(currUserId: String,intent: Intent,activity: Activity) {
        fbStorage.reference.child("userProfileImages").child("$currUserId.png").delete().addOnCompleteListener {
            Log.i("Debug","Deleting User image")
            deleteUserInfo(currUserId,intent, activity)
        }
    }

    private fun deleteUserInfo(currUserId: String,intent: Intent,activity: Activity) {
        db.collection("users").document(currUserId).delete().addOnCompleteListener {
            Log.i("Debug","Deleting User info")
            deleteUserSkillCrossRefs(currUserId,intent, activity)
        }
    }

    fun getChosenUsersTrainings(context: Context){
        trainings = mutableListOf()

        CoroutineScope(IO).launch{
            val query = db.collection("trainings").whereEqualTo("owner",chosenUserId).get().await()
            for (entry in query){
                val id = entry.id
                val name = entry.data.getValue("name").toString()
                val target = entry.data.getValue("target") as ArrayList<String>
                val type = entry.data.getValue("type").toString()
                val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                val defaultPic = PictureUtil.getDefaultTrainingPic()
                val training = Training(name,target,id,chosenUserId,defaultPic,numberOfExercises,"0",type)
                try{
                    val uri =  fbStorage.reference.child("trainingImages").child("${id}.png").downloadUrl.await()
                    val bitmap = PictureUtil.getBitmapFromUri(uri, context)
                    val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,id)
                    training.image = savedImageUri
                }catch (e: Exception) {
                }finally {
                    trainings.add(training)

                }
            }
            withContext(Main){
                chosenUsersTrainings.value = trainings
            }

        }



    }
}