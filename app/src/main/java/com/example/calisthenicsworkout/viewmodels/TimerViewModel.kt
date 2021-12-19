package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {



    var trainingId =  ""
    val exercises = arrayListOf<Exercise>()
    val currentSet = MutableLiveData(1)
    val training = database.getTrainingAsLiveData(trainingId)






    fun loadExercises(training: Training) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.getExercisesOfTrainingDirect(training.id).forEach {
                    exercises.add(it)
                }
                exercises.sortBy { it.order  }
            }
        }
    }

}