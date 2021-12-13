package com.example.calisthenicsworkout.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.Training


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
        text = "Targeted muscles: "+item.target
    }
}

@BindingAdapter("trainingImage")
fun ImageView.setSkillImage(item: Training?){
    item?.let {
        setImageBitmap(it.image)
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
        text = item.repetitions.toString()+"X"+item.sets.toString()
    }
}

@BindingAdapter("skillImage")
fun ImageView.setSkillImage(item: Exercise?){
    item?.let {
        setImageBitmap(it.skillImage)
    }
}
