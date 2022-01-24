package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabaseDao

class ProfileViewModelFactory (
    private val skillDatabaseDao: SkillDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(skillDatabaseDao,application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}