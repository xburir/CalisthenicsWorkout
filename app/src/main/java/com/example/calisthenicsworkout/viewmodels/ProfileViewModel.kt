package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.util.PictureUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {



    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()

    val allUsers = MutableLiveData(mutableListOf<User>())
    val allUsersDirect = mutableListOf<User>()
    val downloadProgress = MutableLiveData(0L)

    val chosenUser = MutableLiveData<User>()
    var chosenUserId = ""

    var currentUser = database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)

    val uploadProgress = MutableLiveData(0L)

    init {
        val context = application.applicationContext
        db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            val name = it.data?.getValue("userFullName").toString()
            val email = it.data?.getValue("userEmail").toString()
            val id = it.id
            val user =  User(id,email,name,PictureUtil.getDefaultProfilePic())
            fbStorage.reference.child("userProfileImages").child("$id.png").downloadUrl.addOnCompleteListener {
                viewModelScope.launch {
                    if(it.isSuccessful){
                        val bitmap = PictureUtil.getBitmapFromUri(it.result!!, context)
                        val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bitmap,context,id)
                        user.userImage = savedImageUri

                    }
                    withContext(Dispatchers.IO){
                        database.insertUser(user)
                        currentUser = database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)
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
                val pictureRef = fbStorage.reference.child("userProfileImages").child("${id}.png")
                pictureRef.downloadUrl
                    .addOnFailureListener {
                        val user = User(id,email,fullName, PictureUtil.getDefaultProfilePic())
                        allUsersDirect.add(user)
                        allUsers.value = allUsersDirect
                        downloadProgress.value = 100L * (allUsersDirect.size / query.size())
                    }
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            val bmp = PictureUtil.getBitmapFromUri(it,context)
                            val url = PictureUtil.saveBitmapToInternalStorage(bmp,context,id)
                            val user = User(id,email,fullName,url)
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
            withContext(Dispatchers.IO){
                val  changedUser = database.getUserDirect(userId)
                changedUser.userImage = savedImageUri
                database.insertUser(changedUser)
            }
            FirebaseStorage.getInstance().reference.child("userProfileImages").child("$userId.png").putFile(uri)
                .addOnProgressListener {
                    uploadProgress.value = (100*it.bytesTransferred/it.totalByteCount)
                }

        }
    }

}