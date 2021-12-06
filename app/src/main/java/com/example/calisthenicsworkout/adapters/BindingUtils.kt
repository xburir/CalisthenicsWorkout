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
                "Push up" -> R.drawable.push_up
                "Muscle up" -> R.drawable.muscle_up
                "Front lever " -> R.drawable.front_lever
                "Pull up" -> R.drawable.pull_up
                else -> R.drawable.ic_launcher_background
            }
        )
    }
}