package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun logosut(intent: Intent) {

    }


}