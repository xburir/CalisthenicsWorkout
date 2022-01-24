package com.example.calisthenicsworkout.viewmodels

import android.app.Application
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.database.entities.TrainingItem
import com.example.calisthenicsworkout.util.PictureUtil
import com.example.calisthenicsworkout.util.PrefUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TimerViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application) {


    var timeBetweenExercises = 0L
    var timeBetweenSets = 0L
    var trainingId =  MutableLiveData("")
    val training = MutableLiveData<Training>()
    var _training = Training("","","","",PictureUtil.getDefaultTrainingPic(),0,"0","")
    val trainingItems = mutableListOf<TrainingItem>()

    lateinit var item : ListIterator<TrainingItem>
    lateinit var nextItem: TrainingItem
    lateinit var currentItem: TrainingItem

    val exercises = arrayListOf<Exercise>()
    var setNumber = 0
    var exerciseNumber = 0

    var allExercisesFinished = false

    var vibrate = true
    var sound = true


    enum class State{
        Stopped,Paused,Running
    }
    lateinit var timer: CountDownTimer
    var timerSeconds = MutableLiveData(5L)
    var timerState = MutableLiveData(State.Stopped)
    var secondsRemaining = MutableLiveData(5L)
    var exerciseTimer = false


    fun loadExercises(trainingId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.getExercisesOfTrainingDirect(trainingId).forEach {    exercise ->
                    exercises.add(exercise)
                }
                exercises.sortBy { exercise -> exercise.order }
                trainingItems.add(TrainingItem("Prepare yourself","seconds",5))



            }
        }
    }

    fun prepareExercises() {
        if(_training.type == "circular"){

            for(i in 1.._training.numberOfSets.toInt()){
                exercises.forEach {
                    val reps = it.repetitions.split(' ')
                    trainingItems.add(TrainingItem(it.skillName,reps[1],reps[0].toInt()))
                }
            }
        }else{
            exercises.forEach { exer ->
                val reps = exer.repetitions.split(' ')
                for (i in 1..exer.sets.toInt()){
                    trainingItems.add(TrainingItem(exer.skillName,reps[1],reps[0].toInt()))
                }
            }
        }
        item = trainingItems.listIterator()
        nextItem = item.next()
        currentItem = nextItem
    }

    fun loadTraining(id: String,requireActivity: FragmentActivity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                _training = database.getTraining(id)
                requireActivity.runOnUiThread {
                    training.value = _training
                }

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
        var startNew = false
        if(item.hasNext()){
            if (exerciseTimer) {
                startNew = true
            } else {
                nextItem = item.next()
                if(currentItem.name != "Prepare yourself"){
                    nextSet()
                }
                if((nextItem.type == "seconds" || nextItem.type == "second")){
                    setNewTimerLength(nextItem.reps.toString())
                    exerciseTimer = true
                }else{
                    setNewTimerLength("")
                }
                secondsRemaining.value = timerSeconds.value
            }
        }else{
            allExercisesFinished = true
        }

        playSound("finish")

        timerState.value = State.Stopped

        if(startNew){
            exerciseTimer = false
            setNewTimerLength("")
            secondsRemaining.value = timerSeconds.value
            startTimer()
        }

        currentItem = nextItem


    }

    private fun nextSet() {
        if(_training.type == "circular"){
            exerciseNumber++
            if(exerciseNumber >= _training.numberOfExercises){
                exerciseNumber = 0
                setNumber++
            }
        }else{
            setNumber++
            if(  currentItem.name != nextItem.name ){
                exerciseNumber++
                setNumber = 0
            }
        }

    }

    private fun playSound(type: String) {
        if(sound){
            if(type == "finish"){
                val mp = MediaPlayer.create(getApplication<Application>().applicationContext, R.raw.bell)
                mp.start()

            }
            if(type == "tick"){
                val mp = MediaPlayer.create(getApplication<Application>().applicationContext, R.raw.tick)
                mp.start()
            }
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
            if(_training.type == "circular"){

                if(exerciseNumber == _training.numberOfExercises-1){
                    timerSeconds.value = timeBetweenSets
                }else{
                    timerSeconds.value = timeBetweenExercises
                }
            }else{
                timerSeconds.value = if(setNumber == exercises[exerciseNumber].sets.toInt()){
                    timeBetweenExercises
                }   else{
                    timeBetweenSets
                }
            }


        }

    }


}