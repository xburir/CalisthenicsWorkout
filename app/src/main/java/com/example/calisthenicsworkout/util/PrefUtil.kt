package com.example.calisthenicsworkout.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.calisthenicsworkout.fragments.CounterFragment

class PrefUtil {
    companion object{


        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.example.calisthenicsworkout.previous_timer_length"

        fun getPreviousTimerLengthSeconds(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long,context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.example.calisthenicsworkout.timer_state"

        fun getTimerState(context: Context): CounterFragment.State{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return CounterFragment.State.values()[ordinal]
        }

        fun setTimerState(state: CounterFragment.State, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID,ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID = "com.example.calisthenicsworkout.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long,context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID,seconds)
            editor.apply()
        }

        private const val ALARM_SET_TIME_ID = "com.example.calisthenicsworkout.backgrounded_time"

        fun getAlarmSetTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID,0)
        }

        fun setAlarmSetTime(time:Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID,time)
            editor.apply()
        }

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
    }
}