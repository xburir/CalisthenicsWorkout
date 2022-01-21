package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabaseDao

class TrainingViewModelFactory(private val skillDatabaseDao: SkillDatabaseDao,
                               private val application: Application
) :  ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(skillDatabaseDao,application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}