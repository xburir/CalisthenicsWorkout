package com.example.calisthenicsworkout.adapters

import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.entities.*
import org.w3c.dom.Text
import kotlin.math.floor
import kotlin.math.pow


@BindingAdapter("skillNameFormatted")
fun TextView.setSkillNameFormatted(item: Skill?){
    item?.let{
        text = item.skillName
    }
}


@BindingAdapter("skillDifficultyImage")
fun ImageView.skillDifficultyImage(item: Skill?){
    item?.let {
        when (item.difficulty){
            1 -> {
                setImageResource(R.drawable.one_star)
            }
            2 -> {
                setImageResource(R.drawable.two_stars)
            }
            3 -> {
                setImageResource(R.drawable.three_stars)
            }
            4 -> {
                setImageResource(R.drawable.four_stars)
            }
            5 -> {
                setImageResource(R.drawable.five_stars)
            }
        }
    }
}


@BindingAdapter("targetImage")
fun ImageView.setTargetImage(item: String?){
    item?.let {
        when(item){
            "abs" ->{
                setImageResource(R.drawable.abs)
            }
            "legs" ->{
                setImageResource(R.drawable.legs)
            }
            "chest" ->{
                setImageResource(R.drawable.chest)
            }
            "shoulders" ->{
                setImageResource(R.drawable.shoulder)
            }
            "back" ->{
                setImageResource(R.drawable.back)
            }
            "arms" ->{
                setImageResource(R.drawable.arm)
            }

        }
    }
}




@BindingAdapter("skillDescriptionFormatted")
fun TextView.setSkillDescriptionFormatted(item: Skill?){
    item?.let{
        text = item.skillDescription
    }
}

@BindingAdapter("skillImageOnSkillScreen")
fun ImageView.setSkillImageOnSkillScreen(item: Skill?){
    item?.let {
        setImageBitmap(it.skillImage)
        clipToOutline = true
        setBackgroundResource(R.drawable.constraint_layout_style)
    }
}

@BindingAdapter("skillImage")
fun ImageView.setSkillImage(item: Skill?){
    item?.let {
        setImageBitmap(it.skillImage)
        clipToOutline = true
        setBackgroundResource(R.drawable.round_edges)
    }
}


@BindingAdapter("trainingName")
fun TextView.setTrainingName(item: Training?){
    item?.let{
        text = item.name
    }
}




@BindingAdapter("trainingType")
fun TextView.setTrainingType(item: Training?){
    item?.let{
        text = item.type
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
        if(item.sets == "0"){
            text = item.repetitions
        }else{
            text = item.sets+"x "+item.repetitions
        }


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

@BindingAdapter("remainingExercisePoints")
fun TextView.setRemainingExercisePoints(item: TrainingItem?){
    item?.let {
        text = (it.points.toString()+" points")
    }
}


@BindingAdapter("userImage")
fun ImageView.setUserImage(item: User?){
    item?.let {
        setImageURI(item.userImage)
    }
}

@BindingAdapter("userName")
fun TextView.setUserName(item: User?){
    item?.let {
        text = item.userFullName
    }
}

@BindingAdapter("level")
fun TextView.setLevel(item: Int?){
    item?.let {
        text = "Level: ${getLevel(item)}"
    }
}

@BindingAdapter("levelProgress")
fun ProgressBar.setLevelProgress(item: Int?){
    item?.let{
        progress = item;
        max = nextLevelPoints(item)
    }
}

@BindingAdapter("pointsNeeded")
fun TextView.setPointsNeeded(item: Int?){
    item?.let{
        text = (nextLevelPoints(item)-item).toString() + " points needed to level "+ (getLevel(item)+1).toString()
    }
}

fun getLevel(points: Int):Int{
    var level = 0
    while (2.0.pow(level.toDouble()) <= points/1000 ){
        level++
    }
    return level
}

fun nextLevelPoints(item: Int): Int{
    return (2.0.pow(getLevel(item).toDouble())*1000).toInt()
}



