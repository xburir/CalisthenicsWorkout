package com.example.calisthenicsworkout.fragments

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import javax.sql.DataSource

class SkillViewModelFactory(private val dataSource: SkillDatabaseDao,private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(SkillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SkillViewModel(dataSource,application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}