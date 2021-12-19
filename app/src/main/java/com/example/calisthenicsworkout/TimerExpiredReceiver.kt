package com.example.calisthenicsworkout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.calisthenicsworkout.fragments.CounterFragment
import com.example.calisthenicsworkout.util.NotificationUtil
import com.example.calisthenicsworkout.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context)
        PrefUtil.setTimerState(CounterFragment.State.Stopped, context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}