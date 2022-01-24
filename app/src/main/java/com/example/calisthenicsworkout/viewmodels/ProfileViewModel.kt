package com.example.calisthenicsworkout.viewmodels

import android.app.Application
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
import kotlinx.coroutines.launch

class ProfileViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {



    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()

    val allUsers = MutableLiveData(mutableListOf<User>())
    val allUsersDirect = mutableListOf<User>()
    val downloadProgress = MutableLiveData(0L)

    val currentUser = MutableLiveData<User>()
    var currentUserId = ""

    init {
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
                currentUser.value = it
            }
        }
    }

}