package com.example.calisthenicsworkout.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.calisthenicsworkout.fragments.timer.CounterFragment

class PrefUtil {
    companion object{

        private const val CHOSEN_TRAINING_ID = "com.example.calisthenicsworkout.chosen_training_id"

        fun getTrainingId(context: Context): String?{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(CHOSEN_TRAINING_ID,"")
        }

        fun setTrainingId(trainingId: String, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(CHOSEN_TRAINING_ID,trainingId)
            editor.apply()
        }

        private const val LOAD_ONLINE_DATABASE = "com.example.calisthenicsworkout.load_online_database"

        fun getLoadSetting(context: Context): Boolean?{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(LOAD_ONLINE_DATABASE,false)
        }

        fun setLoadSettings(boolean: Boolean, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putBoolean(LOAD_ONLINE_DATABASE,boolean)
            editor.apply()
        }
    }
}