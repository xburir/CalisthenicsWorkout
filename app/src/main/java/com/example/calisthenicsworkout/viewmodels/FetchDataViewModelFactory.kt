package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabaseDao

class FetchDataViewModelFactory (
    private val skillDatabaseDao: SkillDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(FetchDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FetchDataViewModel(skillDatabaseDao,application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}