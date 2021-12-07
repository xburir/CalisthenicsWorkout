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
            when (item.skillId) {
                "bUtOavsF1sgr0e2X4lVc" -> R.drawable.dip
                "qUpBbiFy2b1Vj9l8F4Pk" -> R.drawable.handstand
                "mDrWpi7wfuxfW4fdNRDH" -> R.drawable.pushup
                "diNPnsIMXFD77XS27c47" -> R.drawable.muscleup
                "XvyQCdNMgkxoumtsrAVY" -> R.drawable.front
                "eZvOsXPA6xm78eH5pq4W" -> R.drawable.pull_up
                "SD2dc0gnxKi7hWPHxN3U" -> R.drawable.backlever
                "NXVQJbsy3rhb312tOW3E" -> R.drawable.situp
                "ZayZ4SIq1Iv1DNmbIVOS" -> R.drawable.squat
                "92DfStRxQd25WPujS74J" -> R.drawable.pikepushup
                "wJyxVbujrKQWhYFiWIqh" -> R.drawable.lsit
                "wcgFzEKmWHkGjjSjTJ0A" -> R.drawable.planche
                "RV6ZgL068YaRdfBkffTv" -> R.drawable.knee_pushup
                else -> R.drawable.nothing
            }
        )
    }
}