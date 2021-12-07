package com.example.calisthenicsworkout.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.entities.Skill


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
        setImageResource(
            when (item.skillName) {
                "Dip" -> R.drawable.dip
                "Handstand" -> R.drawable.handstand
                "Push up" -> R.drawable.pushup
                "Muscle up" -> R.drawable.muscleup
                "Front lever " -> R.drawable.front
                "Pull up" -> R.drawable.pull_up
                "Back lever" -> R.drawable.backlever
                "Sit up" -> R.drawable.situp
                "Squat" -> R.drawable.squat
                "Pike Push up" -> R.drawable.pikepushup
                "Lsit" -> R.drawable.lsit
                "Planche" -> R.drawable.planche
                "Knee Push up" -> R.drawable.knee_pushup
                else -> R.drawable.nothing
            }
        )
    }
}