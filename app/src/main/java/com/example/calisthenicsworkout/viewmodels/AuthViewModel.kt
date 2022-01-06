package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.String
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.example.calisthenicsworkout.util.BitmapUtil
import java.io.ByteArrayOutputStream


class AuthViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){

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

    private suspend fun getBitmap(source: Uri, context: Context): Bitmap {
        val loading = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(source).build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }


    fun saveProfilePic(uri: Uri,context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        viewModelScope.launch {

            val bmp =  getBitmap(uri,context)


            val uriToFirebase = BitmapUtil.getUri(bmp,100,context)

            FirebaseStorage.getInstance().reference.child("userProfileImages").child("$userId.png").putFile(uriToFirebase)
                .addOnSuccessListener {
                    Toast.makeText(context,"Profile image changed and saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context,"Profile image couldn't be uploaded to server", Toast.LENGTH_SHORT).show()
                }


            withContext(Dispatchers.IO){
                val  changedUser = database.getUserDirect(userId)
                changedUser.userImage = bmp
                database.updateUser(changedUser)
            }
        }



    }




}