package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.util.PictureUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class TrainingViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application)  {
    lateinit var type: String

    val allSkills = database.getALlSkills()
    val key = getRandomString(20)

    val training = Training("undefined",ArrayList<String>(),key, FirebaseAuth.getInstance().currentUser!!.uid ,
        Uri.parse("android.resource://com.example.calisthenicsworkout/drawable/default_training_pic"),0,"","")
    val exerciseList = mutableListOf<Exercise>()
    val listToDisplay = MutableLiveData(exerciseList)
    val target = ArrayList<String>()

    val finished = MutableLiveData(false)

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }



    fun saveTraining(context: Context, imgUrl: String) {
        viewModelScope.launch {
            if(imgUrl.isNotEmpty()){
                val bmp = PictureUtil.getBitmapFromUri(Uri.parse(imgUrl), context)
                val savedImageUri = PictureUtil.saveBitmapToInternalStorage(bmp,context,training.id)
                training.image = savedImageUri
                FirebaseStorage.getInstance().reference.child("trainingImages").child("${training.id}.png").putFile(Uri.parse(imgUrl))
            }
            addTrainingToDatabase(training)
            exerciseList.forEach {
                addExerciseToDatabase(it)
            }
            saveTrainingToFirestore(training,exerciseList,context)
        }
    }

    suspend fun addTrainingToDatabase(training: Training) {
        withContext(Dispatchers.IO){
            database.insertTraining(training)
        }
    }

    suspend fun addExerciseToDatabase(exercise: Exercise){
        withContext(Dispatchers.IO){
            database.insertExercise(exercise)
        }
    }


    private fun saveTrainingToFirestore(training: Training, exerciseList: MutableList<Exercise>, context: Context) {
        val database = FirebaseFirestore.getInstance()
        val mappedTraining: MutableMap<String,Any> = HashMap()
        mappedTraining["name"] = training.name
        mappedTraining["owner"] = training.owner
        mappedTraining["numberOfExercises"] = training.numberOfExercises
        mappedTraining["target"] = training.target
        mappedTraining["type"] = training.type
        database.collection("trainings").document(training.id).set(mappedTraining)
            .addOnCompleteListener {
                finished.value = true
            }
            .addOnFailureListener {
                Toast.makeText(context,"Saving training to online database failed, upload it later with good internet connection",Toast.LENGTH_SHORT).show()
            }
        exerciseList.forEach{
            val mappedExercise: MutableMap<String,Any> = HashMap()
            mappedExercise["reps"] = it.repetitions
            mappedExercise["sets"] = it.sets
            mappedExercise["order"] = it.order
            mappedExercise["skillId"] = it.skillId
            mappedExercise["trainingId"] = it.trainingId
            database.collection("exercises").add(mappedExercise)
        }

    }

    fun addExerciseToTraining(nameOfSkill: String, numberOfSets: String, numberOfReps: String, context: Context,adapter: ExerciseListAdapter,activity: Activity) {

            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    var found = false
                    var exercise: Exercise
                    database.getALlSkillsDirect().forEach { skill ->
                        if(skill.skillName == nameOfSkill){
                            found = true
                            training.numberOfExercises++
                            exercise = if(skill.skillType == "reps"){
                                if(numberOfReps == "1"){ Exercise(key,skill.skillId ,numberOfSets, "$numberOfReps repetition", skill.skillImage,skill.skillName,training.numberOfExercises) }
                                else{ Exercise(key,skill.skillId ,numberOfSets,"$numberOfReps repetitions", skill.skillImage,skill.skillName,training.numberOfExercises)  }
                            }else{
                                if (numberOfReps == "1"){  Exercise(key,skill.skillId ,numberOfSets,  "$numberOfReps second", skill.skillImage,skill.skillName,training.numberOfExercises)  }
                                else{ Exercise(key,skill.skillId ,numberOfSets, "$numberOfReps seconds", skill.skillImage,skill.skillName,training.numberOfExercises) }
                            }
                            skill.target.forEach { trg ->
                                if(!target.contains(trg)){
                                    target.add(trg)
                                }

                            }
                            exerciseList.add(exercise)
                            activity.runOnUiThread {
                                listToDisplay.value = exerciseList
                            }
                        }
                        }
                    if(!found){
                        withContext(Main){
                            Toast.makeText(context,"Skill not found", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }

    }


}