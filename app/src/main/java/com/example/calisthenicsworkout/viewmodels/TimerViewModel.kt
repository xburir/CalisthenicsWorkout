package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    var timeBetweenExercises = 0L
    var timeBetweenSets = 0L
    var trainingId =  MutableLiveData("")
    val exercises = arrayListOf<Exercise>()
    val currentSet = MutableLiveData(0)
    val currentExercise =  MutableLiveData<Exercise>()
    val training = MutableLiveData<Training>()
    var exercisesDone = 0
    val allExercisesFinished = MutableLiveData(false)
    enum class State{
        Stopped,Paused,Running
    }
    lateinit var timer: CountDownTimer
    var timerSeconds = MutableLiveData(5L)
    var timerState = MutableLiveData(State.Stopped)
    var secondsRemaining = MutableLiveData(5L)


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
                allExercisesFinished.value = true
            }else{
                currentExercise.value = exercises[exercisesDone]
            }
        }

    }

    fun playPauseClick() {
        when (timerState.value){
            State.Running -> {
                timer.cancel()
                timerState.value = State.Paused

            }
            State.Stopped -> {
                startTimer()
                nextSet()
                timerState.value = State.Running
            }
            State.Paused -> {
                startTimer()
                timerState.value = State.Running
            }
        }
    }

    fun skipClicked() {
        onTimerFinished()
        timer.cancel()
    }

    fun onTimerFinished(){
        timerState.value = State.Stopped
        setNewTimerLength()
        playSound()
        secondsRemaining.value = timerSeconds.value
    }

    private fun playSound() {
        val mp = MediaPlayer.create(getApplication<Application>().applicationContext, R.raw.bell)
        mp.start()
    }

    fun startTimer(){
        timerState.value = State.Running
        val secs = secondsRemaining.value?.times(1000)!!
        timer = object : CountDownTimer(secs,1000){
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining.value = millisUntilFinished / 1000
            }
        }.start()
    }

    private fun setNewTimerLength(){
        if(allExercisesFinished.value == false){
            timerSeconds.value = if(currentSet.value!! == exercises[exercisesDone].sets.toInt()){
                timeBetweenExercises
            }   else{
                timeBetweenSets
            }
        }
    }


}