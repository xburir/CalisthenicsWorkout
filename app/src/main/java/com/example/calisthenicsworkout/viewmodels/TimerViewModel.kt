package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
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
    val training = MutableLiveData<Training>()


    val exercises = arrayListOf<Exercise>()
    var setNumber = 0
    var exerciseNumber = 0

    var allExercisesFinished = false


    enum class State{
        Stopped,Paused,Running
    }
    lateinit var timer: CountDownTimer
    var timerSeconds = MutableLiveData(5L)
    var timerState = MutableLiveData(State.Stopped)
    var secondsRemaining = MutableLiveData(5L)
    var exerciseTimer = false
    var prepareTimer = true


    fun loadExercises(trainingId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.getExercisesOfTrainingDirect(trainingId).forEach {    exercise ->
                    exercises.add(exercise)
                }
                exercises.sortBy { exercise -> exercise.order }
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



    private fun nextSet() {
        if(prepareTimer){
           prepareTimer = false
        }else{
            setNumber++
        }
        if(setNumber == exercises[exerciseNumber].sets.toInt()){
            setNumber = 0
            exerciseNumber++
            if(exerciseNumber == exercises.size) {
                allExercisesFinished = true
                Log.i("Debug","Finishing")
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
                if(exerciseTimer){
                    startTimer()
                }else{
                    startTimer()
                    nextSet()
                }
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


        val reps = exercises[exerciseNumber].repetitions.split(' ')
        if((reps[1] == "seconds" || reps[1] == "second") && !exerciseTimer){
            setNewTimerLength(reps[0])
            secondsRemaining.value = timerSeconds.value
            exerciseTimer = true
        }else{
            setNewTimerLength("")
            secondsRemaining.value = timerSeconds.value
            if(exerciseTimer){
                startTimer()
                nextSet()
            }
            exerciseTimer = false
        }

        playSound("finish")
        timerState.value = State.Stopped
    }

    private fun playSound(type: String) {

        if(type == "finish"){
            val mp = MediaPlayer.create(getApplication<Application>().applicationContext, R.raw.bell)
            mp.start()

        }
        if(type == "tick"){
            val mp = MediaPlayer.create(getApplication<Application>().applicationContext, R.raw.tick)
            mp.start()
        }

    }

    private fun startTimer(){
        timerState.value = State.Running
        val milliSecs = secondsRemaining.value?.times(1000)!!
        timer = object : CountDownTimer(milliSecs,1000){
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining.value = millisUntilFinished / 1000
                if(secondsRemaining.value!! < 5L){
                    playSound("tick")
                }
            }
        }.start()
    }

    private fun setNewTimerLength(reps: String){
        if(reps.isNotEmpty()){
            timerSeconds.value = reps.toLong()
        }else{
            timerSeconds.value = if(setNumber == exercises[exerciseNumber].sets.toInt()){
                timeBetweenExercises
            }   else{
                timeBetweenSets
            }

        }

    }


}