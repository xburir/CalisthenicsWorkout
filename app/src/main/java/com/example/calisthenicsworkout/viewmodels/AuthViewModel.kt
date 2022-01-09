package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.calisthenicsworkout.util.PictureUtil
import com.example.calisthenicsworkout.util.PictureUtil.Companion.getBitmapFromUri


class AuthViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

    val currentUser =  database.getUser(FirebaseAuth.getInstance().currentUser!!.uid)


    fun logout(intent: Intent,activity: Activity) {
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

    fun saveProfilePic(uri: Uri,context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        viewModelScope.launch {
            val bmp =  getBitmapFromUri(uri,context)
            val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bmp,context,userId)
            withContext(Dispatchers.IO){
                val  changedUser = database.getUserDirect(userId)
                changedUser.userImage = savedImageUri
                database.insertUser(changedUser)
            }
            FirebaseStorage.getInstance().reference.child("userProfileImages").child("$userId.png").putFile(uri).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Profile image changed and saved", Toast.LENGTH_SHORT)
                        .show()

                }
            }
            Toast.makeText(context,"Photo will be updated after app restart",Toast.LENGTH_SHORT).show()
        }
    }




}