package com.example.calisthenicsworkout.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.database.entities.TrainingItem


@BindingAdapter("skillNameFormatted")
fun TextView.setSkillNameFormatted(item: Skill?){
    item?.let{
        text = item.skillName
    }
}



@BindingAdapter("skillDescriptionFormatted")
fun TextView.setSkillDescriptionFormatted(item: Skill?){
    item?.let{
        text = item.skillDescription
    }
}

@BindingAdapter("skillImage")
fun ImageView.setSkillImage(item: Skill?){
    item?.let {
        setImageBitmap(it.skillImage)
    }
}


@BindingAdapter("trainingName")
fun TextView.setTrainingName(item: Training?){
    item?.let{
        text = item.name
    }
}


@BindingAdapter("trainingTarget")
fun TextView.setTrainingTarget(item: Training?){
    item?.let{
        text = item.target
    }
}

@BindingAdapter("trainingImage")
fun ImageView.setTrainingImage(item: Training?){
    item?.let {
        setImageURI(it.image)
    }
}

@BindingAdapter("exerciseName")
fun TextView.setExerciseName(item: Exercise?){
    item?.let{
        text = item.skillName
    }
}


@BindingAdapter("exerciseAmount")
fun TextView.setExerciseAmount(item: Exercise?){
    item?.let{
        text = item.sets+"x "+item.repetitions

    }
}

@BindingAdapter("skillImage")
fun ImageView.setSkillImage(item: Exercise?){
    item?.let {
        setImageBitmap(it.skillImage)
    }
}

@BindingAdapter("remainingExerciseName")
fun TextView.setRemainingExerciseName(item: TrainingItem?){
    item?.let {
        text = (it.name)
    }
}

@BindingAdapter("remainingExerciseReps")
fun TextView.setRemainingExerciseReps(item: TrainingItem?){
    item?.let {
        text = (it.reps.toString()+" "+it.type)
    }
}
