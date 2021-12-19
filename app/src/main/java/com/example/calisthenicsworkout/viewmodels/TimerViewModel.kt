package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {



    var trainingId =  MutableLiveData("")
    val exercises = arrayListOf<Exercise>()
    val currentSet = MutableLiveData(0)
    val currentExercise =  MutableLiveData<Exercise>()
    val training = MutableLiveData<Training>()
    var exercisesDone = 0
    val allExercisesFinished = MutableLiveData(false)







    fun loadExercises(trainingId: String, requireActivity: FragmentActivity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.getExercisesOfTrainingDirect(trainingId).forEach {    exercise ->
                    exercises.add(exercise)
                }
                exercises.sortBy { exercise -> exercise.order }
                requireActivity.runOnUiThread {
                    currentExercise.value =exercises[0]
                }
            }
        }

    }

    fun loadTraining(id: String,requireActivity: FragmentActivity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val _training = database.getTraining(id)
                requireActivity.runOnUiThread {
                    training.value = _training
                }

            }
        }
    }



    fun nextSet() {
        currentSet.value = currentSet.value?.plus(1)
        if(currentSet.value!! > exercises[exercisesDone].sets.toInt()){
            currentSet.value = 1
            exercisesDone++
            if(exercisesDone == exercises.size) {
                Log.i("Debug","Channging to true")
                allExercisesFinished.value = true
            }else{
                currentExercise.value = exercises[exercisesDone]
            }
        }





    }

}